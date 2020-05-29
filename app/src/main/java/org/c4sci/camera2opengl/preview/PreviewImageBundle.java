package org.c4sci.camera2opengl.preview;

import android.graphics.SurfaceTexture;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.SurfaceView;

import org.c4sci.camera2opengl.RenderingRuntimeException;

/**
 * This class allows Open GL rendering on {@link SurfaceView}s through EGL data.<br>
 * The getters may return null. It is the responsibility of the user to check them.<br>
 * The bundle accesses the underlying EGL library (setting context, swap ....). In addition it indicates the underlying used EGL.<br>
 * If something goes wrong, an unchecked {@link RenderingRuntimeException} will be thrown.
 */
public interface PreviewImageBundle {
    public SurfaceTexture getInputSurfaceTexture();
    public EGLDisplay getOutputEglDisplay();

    public void attachToTexture(int texture_id);

    /**
     * @param output_surface a surface to draw in
     * @return the corresponding {@link EGLSurface} or null if output_surface is unkwon
     */
    public EGLSurface getOutputEglSurface(SurfaceView output_surface);

    /**
     * @param output_surface a surface to draw in
     * @return the corresping {@link EGLSurface} or null if output_surface is unknown
     */
    public EGLContext getOutputEglContext(SurfaceView output_surface);

    /**
     * @param output_surface a surface to draw in
     * @return the corresponding {@link EGLConfig} or null if output_surface is unknown
     */
    public EGLConfig  getOutputEGLConfig(SurfaceView output_surface);

    /**
     * This is equivalent to the call to <a href="https://www.khronos.org/registry/EGL/sdk/docs/man/html/eglMakeCurrent.xhtml">eglMakeCurrent</a>
     * @param surface_to_draw_in The surface to draw Open GL in. The {@link PreviewImageProcessor} will use the proper display, context... tThe input and output {@link EGLSurface}s willl be the same.
     */
    public void setCurrentContext(SurfaceView surface_to_draw_in);

    /**
     * Indicates the underlying used EGL major version
     * @return e.g 1 for EGL 1.4, 1.5...
     */
    public int getEGLMajorVersion();
    /**
     * Indicates the underlying used EGL minor version
     * @return e.g 4 for EGL 1.4
     */
    public int getEGLMinorVersion();

    public int getCurrentWidthPixel();
    public int getCurrentHeightPixel();
};
