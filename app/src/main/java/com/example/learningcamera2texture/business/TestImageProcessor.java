package com.example.learningcamera2texture.business;

import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.view.SurfaceView;

import com.example.texture.ImageProcessor;
import com.example.texture.ImageProcessorBundle;

import java.util.Random;

public class TestImageProcessor implements ImageProcessor{
    @Override
    public int leastMajorOpenGlVersion() {
        return 3;
    }

    @Override
    public int leastMinorOpenGlVersion() {
        return 0;
    }

    @Override
    public void processImage(ImageProcessorBundle processor_bundle) {

            EGL14.eglMakeCurrent(processor_bundle.outputEglDisplay,
                    processor_bundle.outputEglSurface, processor_bundle.outputEglSurface,
                    processor_bundle.outputEglContext);
            GLES31.glClearColor(myRandom.nextFloat(), myRandom.nextFloat(), 1f, 0f);
            GLES31.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    Random myRandom = new Random();

}
