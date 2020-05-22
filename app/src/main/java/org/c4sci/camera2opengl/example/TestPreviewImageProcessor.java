package org.c4sci.camera2opengl.example;

import android.app.Activity;
import android.opengl.GLES31;
import android.view.SurfaceView;
import android.widget.TextView;

import org.c4sci.camera2opengl.ILogger;
import org.c4sci.camera2opengl.glTools.GlUtilities;
import org.c4sci.camera2opengl.glTools.renderables.IRenderable;
import org.c4sci.camera2opengl.glTools.ShaderUtility;
import org.c4sci.camera2opengl.glTools.renderables.meshes.TriangleMesh;
import org.c4sci.camera2opengl.preview.PreviewImageProcessor;
import org.c4sci.camera2opengl.preview.PreviewImageBundle;

import java.nio.IntBuffer;
import java.util.Random;

public class TestPreviewImageProcessor implements PreviewImageProcessor , ILogger {

    private SurfaceView outputViewLeft;
    private TextView    outputMessage;
    private Activity    parentActivity;


    private int         identityShaderProgram = -1;
    private int         colorShaderProgram = -1;
    private IntBuffer   vertexArrayObjects = null;
    private IntBuffer   vertexBufferObjects = null;
    private int         triangleCount;
    private float       redLevel = 0;
    private float       dRedLevel = 0.005f;

    private boolean     resourcesAreUp = false;
    private TriangleMesh triangleMesh;


    public TestPreviewImageProcessor(SurfaceView output_view_left, TextView output_message, Activity parent_activity){
        super();
        outputViewLeft = output_view_left;
        outputMessage = output_message;
        parentActivity = parent_activity;
        triangleMesh = new TriangleMesh(
                new float[]{
                        -0.5f, 0, 0f, 1,
                        0.5f, 0, 0f, 1,
                        0, 0.5f, 0f,1},
                new float[]{
                        1, 0, 0, 1f,
                        0, 1, 0, 1f,
                        0, 0, 1, 1f},
                null,
                GLES31.GL_STATIC_DRAW);
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
        processor_bundle.setCurrentContext(outputViewLeft);

        setupOpenGlResources(processor_bundle);
        int _w = outputViewLeft.getWidth();
        int _h = outputViewLeft.getHeight();

        // this is necessary to make parts of the view independent
        GLES31.glEnable(GLES31.GL_SCISSOR_TEST);

        // We draw in the left half part of the view
        GLES31.glViewport(0,0, _w/2, _h);
        GLES31.glScissor(0,0,_w/2,_h);
        GlUtilities.ensureGles31Call("glViewport(0,0)", ()-> releaseOpenGlResources());
        GLES31.glClearColor(redLevel, 0, 0, 0);
        redLevel += dRedLevel;
        if (redLevel > 1f){
            redLevel = 0;
        }
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT | GLES31.GL_STENCIL_BUFFER_BIT);
        GlUtilities.ensureGles31Call("glClear", ()-> releaseOpenGlResources());
        int _left = renderModel();

        // We draw in the right half part of the view
        GLES31.glViewport(_w/2,0, _w/2, _h);
        GLES31.glScissor(_w/2,0,_w/2,_h);
        GlUtilities.ensureGles31Call("glViewport(w/2,0)", ()-> releaseOpenGlResources());
       // glClear is not limited by the viewport but by scissor.
        GLES31.glClearColor(0, redLevel, 0, 0);
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT | GLES31.GL_STENCIL_BUFFER_BIT);
        GlUtilities.ensureGles31Call("glClear", ()-> releaseOpenGlResources());
        int _right = renderModel();

        // waits until OpenGL rendering is finished.
        GLES31.glFinish();
    }

    private void showMessage(String s_) {
        parentActivity.runOnUiThread (new Runnable(){
            public void run(){
                outputMessage.append(s_);
            }
        });

    }

    private int renderModel(){
        // Draw the object that is made of triangles
        GLES31.glUseProgram(identityShaderProgram);
        GlUtilities.ensureGles31Call("glUseProgram(shaderProgram = " + identityShaderProgram +") ", ()-> releaseOpenGlResources());

        GLES31.glEnable(GLES31.GL_BLEND);
        GlUtilities.ensureGles31Call("glEnable(GL_BLEND)", ()-> releaseOpenGlResources());

        GLES31.glBlendColor(1.0f, 0.8f, 0.2f, 1f);
        GLES31.glBlendFunc(GLES31.GL_SRC_COLOR, GLES31.GL_ONE_MINUS_SRC_COLOR);

        triangleMesh.draw(identityShaderProgram, IRenderable.MeshStyle.FILLED);


        // Outline the objects in black
        GLES31.glDisable(GLES31.GL_BLEND);
        GLES31.glLineWidth(5);
        GLES31.glUseProgram(colorShaderProgram);
        GlUtilities.ensureGles31Call("glUseProgram(shaderProgram = " + colorShaderProgram +") ", ()-> releaseOpenGlResources());
        int _color_unif_index = GLES31.glGetUniformLocation(colorShaderProgram, ShaderUtility.ShaderAttributes.COLOR.attributeName());
        GlUtilities.assertGles31Call(_color_unif_index != -1, "glGetUniformLocation ( color )", ()->releaseOpenGlResources());
        GlUtilities.ensureGles31Call("glGetUniformLocation ( color )", ()->releaseOpenGlResources());
        GLES31.glUniform4f( _color_unif_index, 0, 0, 0, 1 );
        triangleMesh.draw(colorShaderProgram, IRenderable.MeshStyle.LINES);

        // Show objects vertices in white
        GLES31.glUniform4f( _color_unif_index, 1, 1, 1, 1 );
        triangleMesh.draw(colorShaderProgram, IRenderable.MeshStyle.POINTS);


        return -1;

    }

    @Override
    public void onResume() {

        logD("onResume");
    }

    private void setupOpenGlResources(PreviewImageBundle processor_bundle){
        if (resourcesAreUp){
            return;
        }
        identityShaderProgram = ShaderUtility.loadVertexAndFragmentShaders(
                ShaderUtility.IDENTITY_SHADER_VERTEX_CODE, ShaderUtility.IDENTITY_SHADER_FRAGMENT_CODE,
                ShaderUtility.IDENTITY_SHADER_ATTRIBUTES
        );
        colorShaderProgram = ShaderUtility.loadVertexAndFragmentShaders(ShaderUtility.COLOR_SHADER_VERTEX_CODE, ShaderUtility.IDENTITY_SHADER_FRAGMENT_CODE,
                ShaderUtility.IDENTITY_SHADER_ATTRIBUTES);

        logD("Identity Shader program = " + identityShaderProgram);
        logD("Color Shader program = " + colorShaderProgram);

        triangleMesh.setupOpenGlResources();

        resourcesAreUp = true;
    }

    @Override
    public void onPause() {
        logD("onPause");
        releaseOpenGlResources();
    }

    private void releaseOpenGlResources(){
        if (identityShaderProgram != -1) {
            GLES31.glDeleteProgram(identityShaderProgram);
            identityShaderProgram = -1;
        }
        if (colorShaderProgram != -1) {
            GLES31.glDeleteProgram(colorShaderProgram);
            colorShaderProgram = -1;
        }

        triangleMesh.releaseOpenGlResources();

        resourcesAreUp = false;
    }

    Random myRandom = new Random();

    @Override
    public String getLogName() {
        return "PreviewImageProcessor";
    }
}
