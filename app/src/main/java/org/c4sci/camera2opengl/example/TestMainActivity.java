package org.c4sci.camera2opengl.example;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.c4sci.camera2opengl.ILogger;
import org.c4sci.camera2opengl.R;
import org.c4sci.camera2opengl.preview.PreviewImageProcessor;
import org.c4sci.camera2opengl.preview.camera2implementation.Camera2PreviewToProcessing;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;


@EActivity(R.layout.activity_main)
@Fullscreen
@WindowFeature({Window.FEATURE_NO_TITLE})
public class TestMainActivity extends AppCompatActivity implements ILogger {

    private static final String MAIN_ACTIVITY_FLAG = "MainActivity";

    @ViewById(R.id.inputTextureView)
    protected TextureView texturePreview;

    @ViewById(R.id.outputSurfaceViewLeft)
    protected SurfaceView outputSurfaceViewLeft;

    @ViewById(R.id.outputSurfaceViewRight)
    protected SurfaceView outputSurfaceViewRight;

    @ViewById(R.id.textViewSearching)
    protected TextView    textViewSearching;

    private Camera2PreviewToProcessing previewToTexture;
    private PreviewImageProcessor previewImageProcessor;

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


    }

    @AfterViews
    protected void afterViews(){
        logD("afterViews()");

        previewImageProcessor = new TestPreviewImageProcessor(outputSurfaceViewLeft, outputSurfaceViewRight);

        previewToTexture = new Camera2PreviewToProcessing(
                new SurfaceView[]{outputSurfaceViewLeft, outputSurfaceViewRight},
                () -> {
                    beginFocusingUI();
                    setFocusText(getString(R.string.start_focusing));
                },
                () -> {
                    endFocusingUI();
                    setFocusText(getString(R.string.focused));

                    // TODO

                },
                () -> {
                    beginFocusingUI();
                    setFocusText(getString(R.string.focusing));
                },
                () -> {
                    setFocusText(getString(R.string.skipped));
                    logD("skipped");
                },
                this,
                previewImageProcessor);

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
