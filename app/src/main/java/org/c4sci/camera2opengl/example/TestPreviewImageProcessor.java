package org.c4sci.camera2opengl.example;

import android.opengl.GLES20;
import android.opengl.GLES31;
import android.view.SurfaceView;

import org.c4sci.camera2opengl.glTools.GlUtilities;
import org.c4sci.camera2opengl.glTools.ShaderUtility;
import org.c4sci.camera2opengl.preview.PreviewImageProcessor;
import org.c4sci.camera2opengl.preview.PreviewImageBundle;

import java.nio.IntBuffer;
import java.util.Random;

public class TestPreviewImageProcessor implements PreviewImageProcessor {

    private SurfaceView outputViewLeft;
    private SurfaceView outputViewRight;
    private int         shaderProgram;
    private IntBuffer   vertexArrayObject;
    private int         vertexBuffer;

    public TestPreviewImageProcessor(SurfaceView output_view_left, SurfaceView output_view_right){
        super();
        outputViewLeft = output_view_left;
        outputViewRight = output_view_right;
    }

    @Override
    public int leastMajorOpenGlVersion() {
        return 3;
    }

    @Override
    public int leastMinorOpenGlVersion() {
        return 0;
    }

    @Override
    public void processPreviewImage(PreviewImageBundle processor_bundle) {

//            EGL14.eglMakeCurrent(processor_bundle.getOutputEglDisplay(),
//                    processor_bundle.getOutputEglSurface(), processor_bundle.getOutputEglSurface(),
//                    processor_bundle.getOutputEglContext());
            processor_bundle.setCurrentContext(outputViewLeft);

            GLES31.glViewport(0,0, outputViewLeft.getWidth(), outputViewLeft.getHeight());
            GLES31.glClearColor(0.2f, 0, 0, 0);
            GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT | GLES31.GL_STENCIL_BUFFER_BIT);

            GLES31.glUseProgram(shaderProgram);
    }

    @Override
    public void onResume() {
        shaderProgram = ShaderUtility.loadVertexAndFragmentShaders(
                ShaderUtility.IDENTITY_SHADER_VERTEX_CODE, ShaderUtility.IDENTITY_SHADER_FRAGMENT_CODE,
                ShaderUtility.IDENTITY_SHADER_ATTRIBUTES
        );

        // First create and use a Vertex Buffer Array (VAO)

        vertexArrayObject = IntBuffer.allocate(2);
        GLES31.glGenVertexArrays(1, vertexArrayObject);
        GlUtilities.ensureGles31Call("glGenVertexArrays(1, vertexArrayObject)", ()-> releaseOpenGlResources());
        GLES31.glBindVertexArray(vertexArrayObject.get(0));
        GlUtilities.ensureGles31Call("glBindVertexArray(vertexArrayObject.get(0))", ()-> releaseOpenGlResources());

    }

    @Override
    public void onPause() {
        releaseOpenGlResources();
    }

    private void releaseOpenGlResources(){
        GLES31.glDeleteProgram(shaderProgram);
        GLES31.glDeleteVertexArrays(1, vertexArrayObject);
    }

    Random myRandom = new Random();

}
