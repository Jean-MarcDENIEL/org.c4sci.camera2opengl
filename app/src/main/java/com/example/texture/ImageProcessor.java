package com.example.texture;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

/**
 * All open gl code should be in the {@link #()} method
 */
public interface ImageProcessor{

    /**
     * This is a bundle of data to make some Open GL rendering.
     * Some of these data may be null. It is the responsibility of the {@link ImageProcessor} to check them.
     */
    public class ImageProcessorBundle{
        SurfaceTexture  inputSurfaceTexture;
        EGLDisplay      outputEglDisplay;
        EGLSurface      outputEglSurface;
        EGLContext      outputEglContext;
    };

    public abstract int leastMajorOpenGlVersion();
    public abstract int leastMinorOpenGlVersion();
    public abstract void processImage(ImageProcessorBundle processor_bundle);
}
