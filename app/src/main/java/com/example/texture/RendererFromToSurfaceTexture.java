package com.example.texture;

import android.graphics.SurfaceTexture;

import android.opengl.EGL15;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.EGL14;
import android.view.SurfaceView;
import android.view.TextureView;

import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;


/**
 * This class is intended at working with a SurfaceTexture using OpenGL ES 3.1 and EGL 1.4. <br>
 * All calls to this class methods must be done in the same {@link Thread} that calls the constructor or {@link #setupContext()},
 * or the thread passed as parameters of the {@link #setupContext(Thread)} method or constructor. This thread must never be the UI thread.<br><br>
 * If rendering methods are called outside this thread, a {@link RenderingRuntimeException} unchecked exception will be raised.
 */
public class RendererFromToSurfaceTexture {
    private TextureView     outputTextureView;
    private SurfaceTexture inputSurfaceTexture;
    private Thread          renderingThread;

    private EGLDisplay outputEglDisplay;
    private EGLSurface outputEglSurface;


    public RendererFromToSurfaceTexture(final TextureView input_texture_view, final SurfaceTexture output_surface_texture){
        outputTextureView =     input_texture_view;
        inputSurfaceTexture =   output_surface_texture;
        renderingThread =       Thread.currentThread();
    }

    public RendererFromToSurfaceTexture(final TextureView input_texture_view, final SurfaceTexture output_surface_texture, Thread rendering_thread){
        outputTextureView =     input_texture_view;
        inputSurfaceTexture =   output_surface_texture;
        renderingThread =       rendering_thread;
    }

    public void setupContext(final Thread rendering_thread){
        renderingThread = rendering_thread;

        outputEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (outputEglDisplay == EGL14.EGL_NO_DISPLAY){
            throw new RenderingRuntimeException("eglGetDisplay : " + RenderingRuntimeException.translateEgl14Error(EGL14.eglGetError()));
        }

        EGLConfig[] _configs = new EGLConfig[1];
        int[] _configs_count = new int[1];
        // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int[] _attrib_list = {
                EGL14.EGL_COLOR_BUFFER_TYPE,    EGL14.EGL_RGB_BUFFER,
                // 8 bits RGB
                EGL14.EGL_RED_SIZE,             8,
                EGL14.EGL_GREEN_SIZE,           8,
                EGL14.EGL_BLUE_SIZE,            8,
                //If this value is zero, color buffers with the smallest alpha component size are preferred.
                // Otherwise, color buffers with the largest alpha component of at least the specified size are preferred.
                EGL14.EGL_ALPHA_SIZE,           0,
                // 16 bits depth
                EGL14.EGL_DEPTH_SIZE, 16,
                //EGL14.EGL_STENCIL_SIZE, 8,
                // we want the device to render at least OpenGL ES 3
                EGL14.EGL_RENDERABLE_TYPE,      EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                //EGLExt.EGL_CONTEXT_MAJOR_VERSION_KHR, 3,
                EGL14.EGL_NONE
        };

        if (!EGL14.eglChooseConfig(outputEglDisplay,_attrib_list, 0, _configs, 0, _configs.length, _configs_count, 0)) {
            throw new RenderingRuntimeException("eglGetConfigs : " + RenderingRuntimeException.translateEgl14Error(EGL14.eglGetError()));
        }
        System.out.println("Configs : " + _configs_count[0]);
        for (int _i=0; _i<_configs_count[0]; _i++){
            System.out.println("  " + _configs[_i].getNativeHandle());
        }

        EGLContext outputEglContext = EGL14.eglCreateContext(outputEglDisplay, _configs[0], EGL14.EGL_NO_CONTEXT,
                new int[] {EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE}, 0);
        if (outputEglContext == EGL14.EGL_NO_CONTEXT){
            throw new RenderingRuntimeException("No context : " + RenderingRuntimeException.translateEgl14Error(EGL14.eglGetError()));
        }
        System.out.println("Context = " + outputEglContext);

//
//        outputEglSurface = EGL14.eglCreateWindowSurface(outputEglDisplay, _configs[0], outputTextureView.getSurfaceTexture(), _attribs, _offset);
//        if (outputEglSurface == EGL14.EGL_NO_SURFACE){
//            throw new RenderingRuntimeException("eglCreateWindowSurface : " + RenderingRuntimeException.translateEgl14Error(EGL14.eglGetError()));
//        }
    }

    /**
     * Setup the rendering context to apply in the thread given in constructor {@link #RendererFromToSurfaceTexture(TextureView, SurfaceTexture, Thread)}
     * Associates the {@link SurfaceTexture} given in the constructor with a new GL Context.
     */
    public void setupContext(){
        setupContext(renderingThread);
    }

    public void drawImage(){
        ensureThread();
        if (!EGL14.eglSwapBuffers(outputEglDisplay, outputEglSurface)){
            throw new RenderingRuntimeException("eglSwapBuffers : " + RenderingRuntimeException.translateEgl14Error(EGL14.eglGetError()));
        }
    }

    private void ensureThread(){
        Thread _current = Thread.currentThread();

        if (_current != renderingThread){
            throw new RenderingRuntimeException("Rendering must not occur in the thread  " +
                    _current.getName() + " but in  " + renderingThread.getName() + " : \n" +
                    RenderingRuntimeException.convertThreadTrace(_current));
        }
    }
}
