package org.c4sci.camera2opengl.example;

import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.view.SurfaceView;

import org.c4sci.camera2opengl.preview.PreviewImageProcessor;
import org.c4sci.camera2opengl.preview.PreviewImageBundle;

import java.util.Random;

public class TestImageProcessor implements PreviewImageProcessor {

    private SurfaceView outputView;

    public TestImageProcessor(SurfaceView output_view){
        super();
        outputView = output_view;
    }

    @Override
    public int leastMajorOpenGlVersion() {
        return 3;
    }

    @Override
    public int leastMinorOpenGlVersion() {
        return 0;
    }

    @Override
    public void processPreviewImage(PreviewImageBundle processor_bundle) {

//            EGL14.eglMakeCurrent(processor_bundle.getOutputEglDisplay(),
//                    processor_bundle.getOutputEglSurface(), processor_bundle.getOutputEglSurface(),
//                    processor_bundle.getOutputEglContext());
            processor_bundle.setCurrentContext(outputView);
            GLES31.glClearColor(myRandom.nextFloat(), myRandom.nextFloat(), 1f, 0f);
            GLES31.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    Random myRandom = new Random();

}
