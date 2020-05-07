package com.example.learningcamera2texture.ui;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learningcamera2texture.ILogger;
import com.example.learningcamera2texture.R;
import com.example.texture.RendererFromToSurfaceTextureThread;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;


@EActivity(R.layout.activity_main)
@Fullscreen
@WindowFeature({Window.FEATURE_NO_TITLE})
public class MainActivity extends AppCompatActivity implements ILogger {

    private static final String MAIN_ACTIVITY_FLAG = "MainActivity";

    @ViewById(R.id.textureView)
    protected TextureView texturePreview;

    @ViewById(R.id.textViewSearching)
    protected TextView    textViewSearching;

    private CameraPreviewToTexture previewToTexture;
    private RendererFromToSurfaceTextureThread  renderer;

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
        textViewSearching.setText(s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logD("onCreate()");
        super.onCreate(savedInstanceState);



        previewToTexture = new CameraPreviewToTexture(
                () -> {
                    beginFocusingUI();
                    setFocusText(getString(R.string.start_focusing));
                },
                () -> {
                    endFocusingUI();
                    setFocusText(getString(R.string.focused));
                    // TODO
                    // processImage(this.texturePreview, this.previewToTexture.surfaceTexture);
                },
                () -> {
                    beginFocusingUI();
                    setFocusText(getString(R.string.focusing));
                },
                () -> setFocusText(getString(R.string.skipped)),
                this,
                this
        ) {
            @Override
            protected void processOnFocused() {
                setFocusText("Processed");
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };


    }

    private void processImage(TextureView input_texture_preview, SurfaceTexture output_surface_texture) {
        //TODO
//        RendererFromToSurfaceTextureThread _renderer = new RendererFromToSurfaceTextureThread(input_texture_preview, output_surface_texture);
//        _renderer.setupContext();
        //_renderer.drawImage();
    }

    @AfterViews
    protected void afterViews(){
        logD("afterViews()");
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
        Toast.makeText(this, error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void logE(Throwable e_){
        logD(e_);
        Toast.makeText(this, e_.getMessage(), Toast.LENGTH_LONG).show();
    }
}
