package org.c4sci.camera2opengl.preview;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import org.c4sci.camera2opengl.preview.PreviewImageProcessor;

/**
 * This is a bundle of data to make some Open GL rendering.<br>
 * Some of these data may be null. It is the responsibility of the {@link PreviewImageProcessor} to check them.<br>
 * The bundle accesses the underlying EGL (setting context, swap ....). In addition it indicates the underlying used EGL.
 */
public interface PreviewImageBundle {
    public SurfaceTexture getInputSurfaceTexture();
    public EGLDisplay getOutputEglDisplay();
    public EGLSurface getOutputEglSurface();
    public EGLContext getOutputEglContext();

    /**
     * Indicates the underlying used EGL
     * @return e.g 1 for EGL 1.4, 1.5...
     */
    public int getEGLMajorVersion();
    /**
     * Indicates the underlying used EGL
     * @return e.g 4 for EGL 1.4
     */
    public int getEGLMinorVersion();
};
