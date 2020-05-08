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
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.learningcamera2texture.ILogger;
import com.example.learningcamera2texture.utilities.ResolutionChoice;
import com.example.texture.ImageProcessor;
import com.example.texture.RendererFromToSurfaceTextureThread;

import org.c4sci.threads.ProgrammablePoolThread;
import org.c4sci.threads.ProgrammableThread;

import java.util.Arrays;

public class CameraPreviewToTexture {
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

    protected TextureView           texturePreview;
    protected Size                  textureBufferSize;
    protected SurfaceTexture        surfaceTexture;
    private CaptureRequest.Builder  previewRequestBuilder;

    protected RendererFromToSurfaceTextureThread imageRenderer;
    protected ImageProcessor imageProcessor;

    private Runnable startFocusingUi;
    private Runnable focusedUi;
    private Runnable focusingUi;
    private Runnable skippedUi;
    private PreviewSessionCallBack cameraPreviewSessionCallBack;

    private ILogger logSource;
    private Activity rootActivity;


    public CameraPreviewToTexture(
            final Runnable start_focusing_ui,
            final Runnable focused_ui,
            final Runnable focusing_ui,
            final Runnable skipped_ui,
            final ILogger log_source,
            Activity       root_activity,
            ImageProcessor image_processor){
        startFocusingUi = start_focusing_ui;
        focusedUi =         focused_ui;
        focusingUi =        focusing_ui;
        skippedUi =         skipped_ui;

        logSource =         log_source;
        rootActivity =      root_activity;

        cameraPreviewSessionCallBack =
                new PreviewSessionCallBack(
                        () -> startFocusingUi.run(),
                        () -> {
                            focusedUi.run();
                            if (!imageRenderer.doRender(ProgrammableThread.ThreadPolicy.SKIP_PENDING)) {
                                skippedUi.run();
                            }
                        },
                        () -> focusingUi.run());

        imageProcessor = image_processor;

        imageRenderer = new RendererFromToSurfaceTextureThread(
                texturePreview, surfaceTexture,
                image_processor.leastMajorOpenGlVersion(), image_processor.leastMinorOpenGlVersion()) {
            @Override
            public String getLogName() {
                return "MyRendererFromToSurfaceTextureThread";
            }

            @Override
            public void doRender() {
                // TODO
                //imageProcessor.processImage(inputSurfaceTexture, outputEglDisplay, outputEglSurface);
            }
        };
        imageRenderer.start();

    }

    public void afterViews(TextureView texture_view){
        texturePreview =    texture_view;

        texturePreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface_, int surface_width_, int surface_height_) {
                surfaceTexture = surface_;
                setupCamera(surface_width_, surface_height_);
                setupTextureBufferToPreviewTransform();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface_, int width_, int height_) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface_) {
                surfaceTexture = null;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface_) {

            }
        });


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
        logSource.logD("setupCamera(" + surface_width_px + " , " + surface_height_px + ")");
        try {

            if (ActivityCompat.checkSelfPermission(rootActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                logSource.logE("Not allowed to use camera");
                ActivityCompat.requestPermissions(rootActivity,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
            else {
                CameraManager _camera_manager = (CameraManager) (rootActivity.getSystemService(Activity.CAMERA_SERVICE));
                String[] _cameras = _camera_manager.getCameraIdList();
                for (String _cam_id : _cameras) {
                    logSource.logD("Camera : " + _cam_id);
                    //logCameraCharacteristics(_camera_manager.getCameraCharacteristics(_cam_id));
                }
                String _camera_id = getBackwardCamera(_cameras, _camera_manager);
                if (_camera_id == null) {
                    logSource.logE("No camera available");
                }
                else {
                    logSource.logD("Useful camera is " + _camera_id);
                    CameraCharacteristics _characs = _camera_manager.getCameraCharacteristics(_camera_id);
                    sensorRotationDegree = _characs.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    StreamConfigurationMap _map = _characs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] _available_camera_resolutions = _map.getOutputSizes(SurfaceTexture.class);
                    if (_available_camera_resolutions.length == 0){
                        logSource.logE("No dimension compatible with TextureSurface");
                    }
                    else{
                        int _rotation = rootActivity.getWindowManager().getDefaultDisplay().getRotation();
                        textureBufferSize = ResolutionChoice.
                                chooseOptimalCaptureDefinition(
                                        _available_camera_resolutions,
                                        surface_width_px, surface_height_px,
                                        sensorIsAlignedWidthView());

                        logSource.logD("    texture buffer size = " + textureBufferSize.getWidth() +" * " + textureBufferSize.getHeight());
                        _camera_manager.openCamera(_camera_id, new CameraDevice.StateCallback() {
                            @Override
                            public void onOpened(@NonNull CameraDevice camera_) {
                                logSource.logD("Device opened");
                                cameraDevice = camera_;
                                setupPreview();
                            }

                            @Override
                            public void onDisconnected(@NonNull CameraDevice camera_) {
                                logSource.logD("Device disconnected");
                                if (cameraDevice != null) {
                                    cameraDevice.close();
                                    cameraDevice = null;
                                }
                            }

                            @Override
                            public void onError(@NonNull CameraDevice camera, int error) {
                                logSource.logD("Device error");
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
            logSource.logE(e_);
        }
    }

    private void setupPreview(){
        logSource.logD("setupPreview()");
        // surfaceTexture is initialized by call to listener
        if (surfaceTexture == null) {
            logSource.logE("Surface texture is null");
        }
        else {
            if (textureBufferSize != null) {
                logSource.logD("   texture dimensions = " + textureBufferSize.getWidth() + " * " + textureBufferSize.getHeight());
                surfaceTexture.setDefaultBufferSize(textureBufferSize.getWidth(), textureBufferSize.getHeight());
            }
            else{
                logSource.logE("   texture dimensions is null");
            }
            Surface _preview_surface = new Surface(surfaceTexture);
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
                                logSource.logE("Capture session : configuration failed");
                            }
                        },
                        null);
            } catch (CameraAccessException e_) {
                logSource.logE(e_);
            }
        }
    }

    private void launchPreview() {
        logSource.logD("launchPreview()");
        if (cameraDevice ==null){
            logSource.logE("Cannot update preview with null camera device");
        }
        else{
            try {
                cameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), cameraPreviewSessionCallBack, backgroundThreadHandler);
            } catch (CameraAccessException _e) {
                logSource.logE(_e);
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
                logSource.logD(_e);
            }
        }
        return _res;
    }

    public void onResume(){
        if (texturePreview.isAvailable()){
            logSource.logD("   texturePreivew is Available");
            setupCamera(texturePreview.getWidth(), texturePreview.getHeight());
            setupTextureBufferToPreviewTransform();
            imageRenderer.setupContext(ProgrammableThread.ThreadPolicy.WAIT_PENDING);
        }
        else{
            logSource.logD("   texturePreview is not Available");
        }
        startBackgroundThread();
    }

    public void onPause(){
        stopBackgroundThread();
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void startBackgroundThread() {
        logSource.logD("startBackgroundThread()");
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundThreadHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        logSource.logD("stopBackgroundThread()");
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundThreadHandler = null;
        } catch (InterruptedException _e) {
            logSource.logD(_e);
        }
    }

    private void setupTextureBufferToPreviewTransform() {
        logSource.logD("setupTextureBufferToPreviewTransform()");
        if (textureBufferSize == null){
            logSource.logD("preview dimension unset, not orientating the preview");
            return;
        }
        if (texturePreview == null){
            logSource.logD("textureView not set, not orientating the preview");
        }
        Matrix _matrix = new Matrix();
        int _rotation_index = rootActivity.getWindowManager().getDefaultDisplay().getRotation();


        if (sensorIsAlignedWidthView()){
            logSource.logD("  sensor aligned with view (Landscape transform)");
            int _preview_width = texturePreview.getWidth();
            int _preview_height = texturePreview.getHeight();

            RectF _preview_rect = new RectF(0, 0, _preview_width, _preview_height);
            RectF _buffer_rect = new RectF(0, 0, textureBufferSize.getHeight(), textureBufferSize.getWidth());
            logSource.logD("    Preview = " + _preview_rect.width() + " * " + _preview_rect.height());
            logSource.logD("    Buffer  = " + _buffer_rect.width() + " * " + _buffer_rect.height());

            float _preview_center_x = _preview_rect.centerX();
            float _preview_center_y = _preview_rect.centerY();

            _buffer_rect.offset(_preview_center_x - _buffer_rect.centerX(), _preview_center_y - _buffer_rect.centerY());
            _matrix.setRectToRect(_preview_rect, _buffer_rect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) _preview_height / textureBufferSize.getHeight(),
                    (float) _preview_width / textureBufferSize.getWidth());
            _matrix.postScale(scale, scale, _preview_center_x, _preview_center_y);
            logSource.logD("    scale = " + scale);

            // it is necessary to rotate as there is always a rotation from sensor to preview (except NEXUS it seems)

            _matrix.postRotate(90 * (_rotation_index - 2), _preview_center_x, _preview_center_y);

            float _preview_ratio =  (float)_preview_height / _preview_width;
            float _buffer_ratio = (float)textureBufferSize.getHeight() / textureBufferSize.getWidth();
            float _correction_ratio = _preview_ratio / _buffer_ratio;
            logSource.logD("    correction ratio = " + _correction_ratio);
            _matrix.postScale(_correction_ratio, 1f);
        }
        else{
            logSource.logD("  sensor tilted from view (Portrait transform)");

            RectF   _buffer_rectangle = new RectF(0,0, textureBufferSize.getHeight(), textureBufferSize.getWidth());
            RectF   _preview_rectangle = new RectF(0,0, texturePreview.getWidth(), texturePreview.getHeight());
            logSource.logD("    Preview = " + _preview_rectangle.width() + " * " + _preview_rectangle.height());
            logSource.logD("    Buffer  = " + _buffer_rectangle.width() + " * " + _buffer_rectangle.height());

            //_matrix.setRectToRect(_buffer_rectangle, _preview_rectangle, Matrix.ScaleToFit.FILL);

            float _preview_ratio = _preview_rectangle.width() / _preview_rectangle.height();
            float _buffer_ratio = _buffer_rectangle.width() / _buffer_rectangle.height();

            float _scale;
            if (_preview_ratio > _buffer_ratio){
                logSource.logD("    buffer is narrower than preview is: scale on height");
                _scale = _preview_rectangle.height() / _buffer_rectangle.height();
            }
            else{
                logSource.logD("    buffer is wider than preview is : scale on width");
                _scale = _preview_rectangle.width() / _buffer_rectangle.width();
            }

            float _ratio_correction = _preview_ratio / _buffer_ratio;
            logSource.logD("    Scale = " + _scale);
            logSource.logD("    Ratio correction = " + _ratio_correction);
            //_scale = 0.9f;
            _matrix.setScale(1f, _ratio_correction);
            //_matrix.postScale(_scale, _scale);

        }
        texturePreview.setTransform(_matrix);
    }



}
