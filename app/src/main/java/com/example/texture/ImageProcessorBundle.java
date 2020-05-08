package com.example.texture;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

/**
 * This is a bundle of data to make some Open GL rendering.
 * Some of these data may be null. It is the responsibility of the {@link ImageProcessor} to check them.
 */
public class ImageProcessorBundle {
    public SurfaceTexture inputSurfaceTexture;
    public EGLDisplay outputEglDisplay;
    public EGLSurface outputEglSurface;
    public EGLContext outputEglContext;
};
