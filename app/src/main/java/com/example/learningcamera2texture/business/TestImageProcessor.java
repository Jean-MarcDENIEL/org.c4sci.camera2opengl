package com.example.learningcamera2texture.business;

import android.graphics.SurfaceTexture;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import com.example.texture.ImageProcessor;

import java.util.Random;

public class TestImageProcessor implements ImageProcessor {
    @Override
    public int leastMajorOpenGlVersion() {
        return 3;
    }

    @Override
    public int leastMinorOpenGlVersion() {
        return 0;
    }

    @Override
    public void processImage(SurfaceTexture inputSurfaceTexture, EGLDisplay outputEglDisplay, EGLSurface outputEglSurface) {
        try {
            Thread.sleep(new Random().nextInt(15));
        } catch (InterruptedException _e) {
            throw new RuntimeException(_e);
        }
    }

}
