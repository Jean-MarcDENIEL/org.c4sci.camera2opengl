package com.example.texture;

import android.graphics.SurfaceTexture;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

/**
 * All open gl code should be in the {@link #()} method
 */
public interface ImageProcessor{
    public abstract int leastMajorOpenGlVersion();
    public abstract int leastMinorOpenGlVersion();
    public abstract void processImage(SurfaceTexture inputSurfaceTexture,
                               EGLDisplay outputEglDisplay,
                               EGLSurface outputEglSurface);
}
