package org.c4sci.camera2opengl.example;

import android.opengl.GLES20;
import android.opengl.GLES31;
import android.view.SurfaceView;

import org.c4sci.camera2opengl.preview.PreviewImageProcessor;
import org.c4sci.camera2opengl.preview.PreviewImageBundle;

import java.util.Random;

public class TestPreviewImageProcessor implements PreviewImageProcessor {

    private SurfaceView outputViewLeft;
    private SurfaceView outputViewRight;

    public TestPreviewImageProcessor(SurfaceView output_view_left, SurfaceView output_view_right){
        super();
        outputViewLeft = output_view_left;
        outputViewRight = output_view_right;
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
            processor_bundle.setCurrentContext(outputViewLeft);

            GLES31.glViewport(0,0, outputViewLeft.getWidth(), outputViewLeft.getHeight());
            GLES31.glClearColor(0.2f, 0, 0, 0);
            GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT | GLES31.GL_STENCIL_BUFFER_BIT);

            //GLES31.
    }

    Random myRandom = new Random();

}
