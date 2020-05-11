package org.c4sci.camera2opengl.preview;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.SurfaceView;

import org.c4sci.camera2opengl.preview.PreviewImageProcessor;

/**
 * This is a bundle of data to make some Open GL rendering.<br>
 * Some of these data may be null. It is the responsibility of the {@link PreviewImageProcessor} to check them.<br>
 * The bundle accesses the underlying EGL (setting context, swap ....). In addition it indicates the underlying used EGL.<br>
 * If something goes wrong, an unchecked {@link org.c4sci.camera2opengl.texture.RenderingRuntimeException} will be thrown.
 */
public interface PreviewImageBundle {
    public SurfaceTexture getInputSurfaceTexture();
    public EGLDisplay getOutputEglDisplay();
    public EGLSurface getOutputEglSurface();
    public EGLContext getOutputEglContext();

    /**
     * This is equivalent to the call to <a href="https://www.khronos.org/registry/EGL/sdk/docs/man/html/eglMakeCurrent.xhtml">eglMakeCurrent</a>
     * @param surface_to_draw_in The surface to draw Open GL in
     */
    public void setCurrentContext(SurfaceView surface_to_draw_in);

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
