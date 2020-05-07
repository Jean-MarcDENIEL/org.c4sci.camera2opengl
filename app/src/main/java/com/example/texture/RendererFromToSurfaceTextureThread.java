package com.example.texture;

import android.graphics.SurfaceTexture;

import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.EGL14;
import android.view.TextureView;

import org.c4sci.threads.ProgrammableThread;


/**
 * This class is intended at working with a SurfaceTexture using OpenGL ES 3+ and EGL 1.4. <br>
 * All calls to this class {@link #submitTask(Runnable, ThreadPolicy)}  will be processed in the same {@link Thread},
 * to avoid calls on different threads or even the UI thread.<br>
 * All calls made during the thread is working will be skipped or will be waiting for the thread to be ready.
 */
public class RendererFromToSurfaceTextureThread extends ProgrammableThread{
    private TextureView     outputTextureView;
    private SurfaceTexture  inputSurfaceTexture;
    private int             openGlMajorLeastVersion;
    private int             openGlMinorLeastVersion;

    private EGLDisplay      outputEglDisplay;
    private EGLSurface      outputEglSurface;



    /**
     * Creates a thread capable of using a {@link TextureView} as input and a {@link SurfaceTexture} as output.
     * @param input_texture_view            The {@link TextureView} which texture can used retrieved
     * @param output_surface_texture        The {@link android.view.Surface} to output rendering
     * @param opengl_major_least_version    The "at least" major version of openGL you want to use (e.g. 2 for openGL 2+ or 3 for opengl 3). Must be at least 2.
     * @param opengl_minor_least_version    The "at least" minor version of openGL you want to use (e.g 1 for openGL X.1)
     */
    public RendererFromToSurfaceTextureThread(final TextureView input_texture_view,
                                              final SurfaceTexture output_surface_texture,
                                              final int opengl_major_least_version,
                                              final int opengl_minor_least_version){
        outputTextureView =         input_texture_view;
        inputSurfaceTexture =       output_surface_texture;
        openGlMajorLeastVersion =   opengl_major_least_version;
        openGlMinorLeastVersion =   opengl_minor_least_version;

    }

    public boolean setupContext(ThreadPolicy thread_policy){
        return submitTask(() -> setupContextThreaded(), thread_policy);
    }

    public boolean drawImage(ThreadPolicy thread_policy){
        return submitTask(() -> drawImageThreaded(), thread_policy);
    }

    public void setupContextThreaded(){
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

    public void drawImageThreaded(){
        if (!EGL14.eglSwapBuffers(outputEglDisplay, outputEglSurface)){
            throw new RenderingRuntimeException("eglSwapBuffers : " + RenderingRuntimeException.translateEgl14Error(EGL14.eglGetError()));
        }
    }

}
