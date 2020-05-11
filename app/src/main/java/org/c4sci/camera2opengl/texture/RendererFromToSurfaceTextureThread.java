package org.c4sci.camera2opengl.texture;

import android.graphics.SurfaceTexture;

import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.EGL14;
import android.view.SurfaceView;
import android.view.TextureView;

import org.c4sci.camera2opengl.ILogger;

import org.c4sci.camera2opengl.preview.PreviewImageBundle;
import org.c4sci.camera2opengl.preview.PreviewImageProcessor;
import org.c4sci.threads.ProgrammableThread;


/**
 * This class is intended at working with a SurfaceTexture using OpenGL ES 3+ and EGL 1.4. <br>
 * All calls to this class {@link #submitTask(Runnable, ThreadPolicy)}  will be processed in the same {@link Thread},
 * to avoid calls on different threads or even the UI thread.<br>
 * All calls made during the thread is working will be skipped or will be waiting for the thread to be ready.
 */
public abstract class RendererFromToSurfaceTextureThread extends ProgrammableThread implements ILogger, PreviewImageBundle {
    private TextureView inputTextureView;
    private SurfaceTexture inputSurfaceTexture;

    private SurfaceView outputSurfaceView;
    private PreviewImageProcessor previewImageProcessor;

    private EGLDisplay outputEglDisplay = null;
    private EGLSurface outputEglSurface = null;
    private EGLContext outputEglContext = null;
    private EGLConfig outputEglConfig = null;

    /**
     * Creates a thread capable of using a {@link TextureView} as input and a {@link SurfaceTexture}s as output
     * to be processed by a {@link PreviewImageProcessor}
     *
     * @param input_texture_view  The {@link TextureView} which texture can used retrieved
     * @param output_surface_view The {@link android.view.Surface} to output rendering
     * @param image_processor     The process to make the images from a bundle given by the renderer
     */
    public RendererFromToSurfaceTextureThread(final TextureView input_texture_view,
                                              final SurfaceView output_surface_view,
                                              PreviewImageProcessor image_processor) {
        inputTextureView = input_texture_view;
        outputSurfaceView = output_surface_view;
        previewImageProcessor = image_processor;
    }

    /**
     * Should be called in the user onResume() method
     */
    public void onResume() {
        logD("onResume");
        //submitTask(() -> setupContextThreaded(), ThreadPolicy.WAIT_PENDING);
    }

    /**
     * Should be called in the user onPause() method
     */
    public void onPause(){
        logD("onPause");
        submitTask(() -> giveupContextThreaded(), ThreadPolicy.WAIT_PENDING);
    }

    /**
     * Renders images by the {@link PreviewImageProcessor} using a {@link SurfaceTexture}
     * @param surface_texture
     * @param thread_policy
     * @return
     */
    public boolean doRender(SurfaceTexture surface_texture, ThreadPolicy thread_policy) {
        return submitTask(() -> {
            inputSurfaceTexture = surface_texture;
            setupContextThreaded();
            doRenderThreaded();
            drawImageThreaded();
        }, thread_policy);
    }

    private boolean glResourcesAreAllocated() {
        return  outputEglDisplay != null ||
                outputEglSurface != null ||
                outputEglContext != null ||
                outputEglConfig != null;
    }

    /**
     * This method is to be called by {@link #doRender(SurfaceTexture, ThreadPolicy)}  only
     */
    private void doRenderThreaded() {
        previewImageProcessor.processPreviewImage(this);
    }


    public void updateInputSurfaceTexture(SurfaceTexture input_surface_texture) {
        inputSurfaceTexture =input_surface_texture;
    }

    @Override
    public SurfaceTexture getInputSurfaceTexture() {
        return inputSurfaceTexture;
    }

    @Override
    public EGLDisplay getOutputEglDisplay() {
        return outputEglDisplay;
    }

    @Override
    public EGLSurface getOutputEglSurface() {
        return outputEglSurface;
    }

    @Override
    public EGLContext getOutputEglContext() {
        return outputEglContext;
    }

    @Override
    public void setCurrentContext(SurfaceView surface_to_draw_in) {
        ensureEglMethod(EGL14.eglMakeCurrent(getOutputEglDisplay(),
                getOutputEglSurface(), getOutputEglSurface(),
                getOutputEglContext()), "eglMakeCurrent");
    }

    @Override
    public int getEGLMajorVersion() {
        return 1;
    }

    @Override
    public int getEGLMinorVersion() {
        return 4;
    }

    private void setupContextThreaded(){
        if (glResourcesAreAllocated()){
            return;
        }

        // Gets the default display
        ensureEglMethod((outputEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY))!= EGL14.EGL_NO_DISPLAY, "eglGetDisplay");

        // Init EGL
        int[] _major_egl = new int[1];
        int[] _minor_egl = new int[1];
        ensureEglMethod(EGL14.eglInitialize(outputEglDisplay, _major_egl, 0,
                _minor_egl, 0), "eglInitialize");
        logD("EGL implementation : " + _major_egl[0]+"."+_minor_egl[0]);
        //TODO
        // give up if EGL < 1.4

        // Tells EGL to bind to Open GL ES
        ensureEglMethod(EGL14.eglBindAPI(EGL14.EGL_OPENGL_ES_API), "eglBindAPI");


        // Gets a working configuration
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
        ensureEglMethod(
                EGL14.eglChooseConfig(outputEglDisplay,_attrib_list, 0, _configs, 0,
                        _configs.length, _configs_count, 0), "eglChoosConfig");
        ensureEglMethod(_configs_count[0] != 0, "eglChooseConfig : no config available");
        outputEglConfig = _configs[0];

        // Gets a context
        ensureEglMethod((outputEglContext = EGL14.eglCreateContext(outputEglDisplay, outputEglConfig, EGL14.EGL_NO_CONTEXT,
                new int[] {
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, previewImageProcessor.leastMajorOpenGlVersion(),
                        EGL14.EGL_NONE}, 0))!= EGL14.EGL_NO_CONTEXT,
                "eglCreateContext");
        logD("Context = " + outputEglContext);

        logD("outputSurfaceView = " + outputSurfaceView);

        ensureEglMethod((outputEglSurface = EGL14.eglCreateWindowSurface(outputEglDisplay,
                outputEglConfig, outputSurfaceView, new int[]{EGL14.EGL_NONE}, 0)) != EGL14.EGL_NO_SURFACE,
                "eglCreateWindowsSurface -> outputSurface");

    }

    private static void ensureEglMethod(boolean method_result, String method_name){
        if (!method_result){
            throw new RenderingRuntimeException(method_name + " failed with " + RenderingRuntimeException.translateEgl14Error(EGL14.eglGetError()));
        }
    }

    private void drawImageThreaded(){
        ensureEglMethod(EGL14.eglMakeCurrent(outputEglDisplay, outputEglSurface, outputEglSurface, outputEglContext), "eglMAkeCurrent");
        ensureEglMethod(EGL14.eglSwapBuffers(outputEglDisplay, outputEglSurface), "eglSwapBuffers");
    }

    private void giveupContextThreaded(){
        if (glResourcesAreAllocated()){
            if (outputEglDisplay != null) {
                ensureEglMethod(EGL14.eglMakeCurrent(outputEglDisplay,
                        EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT), "eglMakeCurrent");
                if (outputEglSurface != null) {
                    ensureEglMethod(EGL14.eglDestroySurface(outputEglDisplay, outputEglSurface), "eglDestroySurface");
                    outputEglSurface = null;
                }
                if (outputEglContext != null) {
                    ensureEglMethod(EGL14.eglDestroyContext(outputEglDisplay, outputEglContext), "eglDestroyContext");
                    outputEglContext = null;
                }
                outputEglDisplay = null;
            }
            outputEglConfig = null;
        }
        else{
            logD("giveupContextThreaded : resources are already free");
        }
    }

}
