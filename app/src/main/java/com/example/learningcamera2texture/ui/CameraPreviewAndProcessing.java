package com.example.learningcamera2texture.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.learningcamera2texture.ILogger;
import com.example.learningcamera2texture.utilities.ResolutionChoice;
import com.example.texture.ImageProcessor;
import com.example.texture.RendererFromToSurfaceTextureThread;

import org.c4sci.threads.ProgrammableThread;

import java.util.Arrays;

//TODO
// Verify all the risks given by Android Studio warnings in this code (NullPointerException ...)

public class CameraPreviewAndProcessing implements ILogger{
    private static final int REQUEST_CAMERA_PERMISSION = 200; // just >0

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private HandlerThread backgroundThread;
    private Handler backgroundThreadHandler;

    private CameraDevice            cameraDevice;
    private CameraCaptureSession    cameraCaptureSession;
    private int                     sensorRotationDegree;

    protected TextureView           inputTexturePreview;
    protected Size                  inputTextureBufferSize;
    protected SurfaceTexture        inputSurfaceTexture;
    protected SurfaceView           outputSurfaceView;

    private CaptureRequest.Builder  previewRequestBuilder;

    protected ImageProcessor        imageProcessor;
    protected RendererFromToSurfaceTextureThread imageRenderer;

    private Runnable startFocusingUi;
    private Runnable focusedUi;
    private Runnable focusingUi;
    private Runnable skippedUi;
    private PreviewSessionCallBack cameraPreviewSessionCallBack;

    private Activity rootActivity;


    public CameraPreviewAndProcessing(
            final SurfaceView   output_surface_view,
            final Runnable start_focusing_ui,
            final Runnable focused_ui,
            final Runnable focusing_ui,
            final Runnable skipped_ui,
            final ILogger log_source,
            Activity       root_activity,
            ImageProcessor image_processor){

        outputSurfaceView = output_surface_view;

        startFocusingUi = start_focusing_ui;
        focusedUi =         focused_ui;
        focusingUi =        focusing_ui;
        skippedUi =         skipped_ui;

        rootActivity =      root_activity;

        imageProcessor = image_processor;

        cameraPreviewSessionCallBack =
                new PreviewSessionCallBack(
                        () -> startFocusingUi.run(),
                        () -> {
                            focusedUi.run();
                            if (!imageRenderer.doRenderThreaded(inputSurfaceTexture, ProgrammableThread.ThreadPolicy.SKIP_PENDING)) {
                                skippedUi.run();
                            }
                        },
                        () -> focusingUi.run());



    }

    public void afterViews(TextureView texture_view){
        inputTexturePreview =    texture_view;

        inputTexturePreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface_, int surface_width_, int surface_height_) {
                logD("onSurfaceTextureAvailable :");
                logD("    surface_ = " + surface_);
                logD("   instance of SurfaceTexture ?" + (surface_ instanceof SurfaceTexture));
                inputSurfaceTexture = surface_;
                setupCamera(surface_width_, surface_height_);
                setupTextureBufferToPreviewTransform();
                imageRenderer.updateInputSurfaceTexture(surface_);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface_, int width_, int height_) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface_) {
                inputSurfaceTexture = null;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface_) {

            }
        });

        imageRenderer = new RendererFromToSurfaceTextureThread(
                inputTexturePreview, outputSurfaceView,imageProcessor) {
            @Override
            public String getLogName() {
                return "MyRendererFromToSurfaceTextureThread";
            }
        };
        imageRenderer.start();
    }

    // returns true if the sensor axis correspond to the view axis
    private boolean sensorIsAlignedWidthView(){
        int _view_rotation = rootActivity.getWindowManager().getDefaultDisplay().getRotation();
        return
                // portrait mode and sensor tilt = 0 or 180°
                ((_view_rotation == Surface.ROTATION_0 || _view_rotation == Surface.ROTATION_180)&&
                        (sensorRotationDegree == 0 || sensorRotationDegree == 180)) ||
                        ((_view_rotation == Surface.ROTATION_90 || _view_rotation == Surface.ROTATION_270) &&
                                (sensorRotationDegree == 90 || sensorRotationDegree == 270)) ;
    }


    private void setupCamera(int surface_width_px, int surface_height_px){
        logD("setupCamera(" + surface_width_px + " , " + surface_height_px + ")");
        try {

            if (ActivityCompat.checkSelfPermission(rootActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                logE("Not allowed to use camera");
                ActivityCompat.requestPermissions(rootActivity,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
            else {
                CameraManager _camera_manager = (CameraManager) (rootActivity.getSystemService(Activity.CAMERA_SERVICE));
                String[] _cameras = _camera_manager.getCameraIdList();
                for (String _cam_id : _cameras) {
                    logD("Camera : " + _cam_id);
                    //logCameraCharacteristics(_camera_manager.getCameraCharacteristics(_cam_id));
                }
                String _camera_id = getBackwardCamera(_cameras, _camera_manager);
                if (_camera_id == null) {
                    logE("No camera available");
                }
                else {
                    logD("Useful camera is " + _camera_id);
                    CameraCharacteristics _characs = _camera_manager.getCameraCharacteristics(_camera_id);
                    sensorRotationDegree = _characs.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    StreamConfigurationMap _map = _characs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] _available_camera_resolutions = _map.getOutputSizes(SurfaceTexture.class);
                    if (_available_camera_resolutions.length == 0){
                        logE("No dimension compatible with TextureSurface");
                    }
                    else{
                        int _rotation = rootActivity.getWindowManager().getDefaultDisplay().getRotation();
                        inputTextureBufferSize = ResolutionChoice.
                                chooseOptimalCaptureDefinition(
                                        _available_camera_resolutions,
                                        surface_width_px, surface_height_px,
                                        sensorIsAlignedWidthView());

                        logD("    texture buffer size = " + inputTextureBufferSize.getWidth() +" * " + inputTextureBufferSize.getHeight());
                        _camera_manager.openCamera(_camera_id, new CameraDevice.StateCallback() {
                            @Override
                            public void onOpened(@NonNull CameraDevice camera_) {
                                logD("Device opened");
                                cameraDevice = camera_;
                                setupPreview();
                            }

                            @Override
                            public void onDisconnected(@NonNull CameraDevice camera_) {
                                logD("Device disconnected");
                                if (cameraDevice != null) {
                                    cameraDevice.close();
                                    cameraDevice = null;
                                }
                            }

                            @Override
                            public void onError(@NonNull CameraDevice camera, int error) {
                                logD("Device error");
                                if (cameraDevice != null) {
                                    cameraDevice.close();
                                    cameraDevice = null;
                                }
                            }
                        }, null);
                    }
                }
            }
        } catch (CameraAccessException e_) {
            logE(e_);
        }
    }

    private void setupPreview(){
        logD("setupPreview()");
        // surfaceTexture is initialized by call to listener
        if (inputSurfaceTexture == null) {
            logE("Surface texture is null");
        }
        else {
            if (inputTextureBufferSize != null) {
                logD("   texture dimensions = " + inputTextureBufferSize.getWidth() + " * " + inputTextureBufferSize.getHeight());
                inputSurfaceTexture.setDefaultBufferSize(inputTextureBufferSize.getWidth(), inputTextureBufferSize.getHeight());
            }
            else{
                logE("   texture dimensions is null");
            }
            Surface _preview_surface = new Surface(inputSurfaceTexture);
            try {
                previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(_preview_surface);
                //previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
                cameraDevice.createCaptureSession(
                        Arrays.asList(_preview_surface),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session_) {
                                if (cameraDevice != null){
                                    cameraCaptureSession = session_;
                                    launchPreview();
                                }
                            }
                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session_) {
                                logE("Capture session : configuration failed");
                            }
                        },
                        null);
            } catch (CameraAccessException e_) {
                logE(e_);
            }
        }
    }

    private void launchPreview() {
        logD("launchPreview()");
        if (cameraDevice ==null){
            logE("Cannot update preview with null camera device");
        }
        else{
            try {
                cameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), cameraPreviewSessionCallBack, backgroundThreadHandler);
            } catch (CameraAccessException _e) {
                logE(_e);
            }
        }
    }

    private String getBackwardCamera(String[] available_cameras, CameraManager camera_manager) {
        String _res = null;
        int _max_res = 0;
        for (String _cam : available_cameras){
            try {
                CameraCharacteristics _characs = camera_manager.getCameraCharacteristics(_cam);
                if (_characs.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
                    Rect _rect = _characs.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    int _resolution = _rect.width() * _rect.height();
                    if (_resolution > _max_res){
                        _res = _cam;
                        _max_res = _resolution;
                    }
                }
            } catch (CameraAccessException _e) {
                logD(_e);
            }
        }
        return _res;
    }

    /**
     * Should be called in the UI onResume() method
     */
    public void onResume(){
        if (inputTexturePreview.isAvailable()){
            logD("   texturePreview is Available");
            setupCamera(inputTexturePreview.getWidth(), inputTexturePreview.getHeight());
            setupTextureBufferToPreviewTransform();
        }
        else{
            logD("   texturePreview is not Available");
        }
        startBackgroundThread();

        imageRenderer.onResume();
    }

    /**
     * Should be called in the UI onPause() method
     */
    public void onPause(){
        stopBackgroundThread();
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        imageRenderer.onPause();
    }

    private void startBackgroundThread() {
        logD("startBackgroundThread()");
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundThreadHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        logD("stopBackgroundThread()");
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundThreadHandler = null;
        } catch (InterruptedException _e) {
            logD(_e);
        }
    }

    private void setupTextureBufferToPreviewTransform() {
        logD("setupTextureBufferToPreviewTransform()");
        if (inputTextureBufferSize == null){
            logD("preview dimension unset, not orientating the preview");
            return;
        }
        if (inputTexturePreview == null){
            logD("textureView not set, not orientating the preview");
        }
        Matrix _matrix = new Matrix();
        int _rotation_index = rootActivity.getWindowManager().getDefaultDisplay().getRotation();


        if (sensorIsAlignedWidthView()){
            logD("  sensor aligned with view (Landscape transform)");
            int _preview_width = inputTexturePreview.getWidth();
            int _preview_height = inputTexturePreview.getHeight();

            RectF _preview_rect = new RectF(0, 0, _preview_width, _preview_height);
            RectF _buffer_rect = new RectF(0, 0, inputTextureBufferSize.getHeight(), inputTextureBufferSize.getWidth());
            logD("    Preview = " + _preview_rect.width() + " * " + _preview_rect.height());
            logD("    Buffer  = " + _buffer_rect.width() + " * " + _buffer_rect.height());

            float _preview_center_x = _preview_rect.centerX();
            float _preview_center_y = _preview_rect.centerY();

            _buffer_rect.offset(_preview_center_x - _buffer_rect.centerX(), _preview_center_y - _buffer_rect.centerY());
            _matrix.setRectToRect(_preview_rect, _buffer_rect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) _preview_height / inputTextureBufferSize.getHeight(),
                    (float) _preview_width / inputTextureBufferSize.getWidth());
            _matrix.postScale(scale, scale, _preview_center_x, _preview_center_y);
            logD("    scale = " + scale);

            // it is necessary to rotate as there is always a rotation from sensor to preview (except NEXUS it seems)

            _matrix.postRotate(90 * (_rotation_index - 2), _preview_center_x, _preview_center_y);

            float _preview_ratio =  (float)_preview_height / _preview_width;
            float _buffer_ratio = (float) inputTextureBufferSize.getHeight() / inputTextureBufferSize.getWidth();
            float _correction_ratio = _preview_ratio / _buffer_ratio;
            logD("    correction ratio = " + _correction_ratio);
            _matrix.postScale(_correction_ratio, 1f);
        }
        else{
            logD("  sensor tilted from view (Portrait transform)");

            RectF   _buffer_rectangle = new RectF(0,0, inputTextureBufferSize.getHeight(), inputTextureBufferSize.getWidth());
            RectF   _preview_rectangle = new RectF(0,0, inputTexturePreview.getWidth(), inputTexturePreview.getHeight());
            logD("    Preview = " + _preview_rectangle.width() + " * " + _preview_rectangle.height());
            logD("    Buffer  = " + _buffer_rectangle.width() + " * " + _buffer_rectangle.height());

            //_matrix.setRectToRect(_buffer_rectangle, _preview_rectangle, Matrix.ScaleToFit.FILL);

            float _preview_ratio = _preview_rectangle.width() / _preview_rectangle.height();
            float _buffer_ratio = _buffer_rectangle.width() / _buffer_rectangle.height();

            float _scale;
            if (_preview_ratio > _buffer_ratio){
                logD("    buffer is narrower than preview is: scale on height");
                _scale = _preview_rectangle.height() / _buffer_rectangle.height();
            }
            else{
                logD("    buffer is wider than preview is : scale on width");
                _scale = _preview_rectangle.width() / _buffer_rectangle.width();
            }

            float _ratio_correction = _preview_ratio / _buffer_ratio;
            logD("    Scale = " + _scale);
            logD("    Ratio correction = " + _ratio_correction);
            //_scale = 0.9f;
            _matrix.setScale(1f, _ratio_correction);
            //_matrix.postScale(_scale, _scale);

        }
        inputTexturePreview.setTransform(_matrix);
    }


    @Override
    public String getLogName() {
        return "CameraPreviewToTexture";
    }

    @Override
    public void logE(String error_msg){
        ILogger.super.logE(error_msg);
        throw new RuntimeException(error_msg);
    }
    
    @Override
    public void logE(Throwable e_){
        ILogger.super.logE(e_);
        throw new RuntimeException(e_.getMessage(), e_);
    }
}
