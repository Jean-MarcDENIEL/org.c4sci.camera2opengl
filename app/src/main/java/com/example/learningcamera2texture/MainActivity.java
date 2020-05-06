package com.example.learningcamera2texture;

import android.Manifest;
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
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.learningcamera2texture.utilities.ResolutionChooser;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;
import org.c4sci.threads.IParametrizedRunnable;
import org.c4sci.threads.ProgrammablePoolException;
import org.c4sci.threads.ProgrammablePoolRuntimeException;
import org.c4sci.threads.ProgrammablePoolThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


@EActivity(R.layout.activity_main)
@Fullscreen
@WindowFeature({Window.FEATURE_NO_TITLE})
public class MainActivity extends AppCompatActivity implements ILogable {

    private static final String MAIN_ACTIVITY_FLAG = "MainActivity";

    @ViewById(R.id.textureView)
    TextureView texturePreview;

    @ViewById(R.id.textViewSearching)
    TextView    textViewSearching;

    PreviewCameraToTexture  previewToTexture;

    @UiThread
    protected void endFocusingUI() {
        textViewSearching.setBackgroundColor(getResources().getColor(R.color.colorFocused));
    }

    @UiThread
    protected void beginFocusingUI() {
        textViewSearching.setBackgroundColor(getResources().getColor(R.color.colorFocusing));
    }

    @UiThread
    protected void setFocusText(String s) {
        textViewSearching.setText("Focus state: " + s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logD("onCreate()");
        super.onCreate(savedInstanceState);


    }

    Random random = new Random();

    private void processFocusedPreview(){
        //TODO here we had the treatment
        try {
            logD("Processing...");
            setFocusText("Processing began ...");
            Thread.sleep(random.nextInt(20));
            setFocusText("Processing done.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterViews
    protected void afterViews(){
        logD("afterViews()");
        previewToTexture = new PreviewCameraToTexture(
                () -> {
                    beginFocusingUI();
                    setFocusText("Start focusing...");
                },
                () -> {
                    endFocusingUI();
                    setFocusText("Focused !...");
                },
                () -> {
                    beginFocusingUI();
                    setFocusText("Focusing ...");
                },
                () -> processFocusedPreview(),
                () -> setFocusText("Skipped"),
                this,
                this
        );
        previewToTexture.afterViews(texturePreview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("onResume()");
        previewToTexture.onResume();
    }

    @Override
    protected void onPause() {

        logD("onPause()");
        super.onPause();
        previewToTexture.onPause();
    }

    @Override
    public String getLogName() {
        return MAIN_ACTIVITY_FLAG;
    }

    @Override
    public void logE(String error_msg){
        logD(error_msg);
        Toast.makeText(this, error_msg, Toast.LENGTH_LONG);
    }

    @Override
    public void logE(Throwable e_){
        logD(e_);
        Toast.makeText(this, e_.getMessage(), Toast.LENGTH_LONG);
    }
}
