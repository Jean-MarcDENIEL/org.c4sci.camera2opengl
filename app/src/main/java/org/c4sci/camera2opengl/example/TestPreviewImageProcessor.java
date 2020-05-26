package org.c4sci.camera2opengl.example;

import android.app.Activity;
import android.opengl.GLES31;
import android.opengl.Matrix;
import android.view.SurfaceView;
import android.widget.TextView;

import org.c4sci.camera2opengl.ILogger;
import org.c4sci.camera2opengl.glTools.GlUtilities;
import org.c4sci.camera2opengl.glTools.renderables.IRenderable;
import org.c4sci.camera2opengl.glTools.renderables.meshes.AxisAlignedBoxMesh;
import org.c4sci.camera2opengl.glTools.renderables.shaders.AssembledShader;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderAttributes;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderCodeSnippet;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderUtility;
import org.c4sci.camera2opengl.glTools.renderables.shaders.stock.StockFragmentShaderSnippets;
import org.c4sci.camera2opengl.glTools.renderables.shaders.stock.StockVertexShaderSnippets;
import org.c4sci.camera2opengl.preview.PreviewImageProcessor;
import org.c4sci.camera2opengl.preview.PreviewImageBundle;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Random;

public class TestPreviewImageProcessor implements PreviewImageProcessor , ILogger {

    private SurfaceView outputView;
    private TextView    outputMessage;
    private Activity    parentActivity;

    private int         identityShaderProgram = -1;
    private int         colorShaderProgram = -1;

    private float       redLevel = 0;
    private float       dRedLevel = 0.005f;
    private float[]      mvpMatrix;

    private int         outputViewWidthPixel;
    private int         outputViewHeightPixel;

    private int         identityProgramMvpIndex;
    private int         colorProgramMvpIndex;
    private int         ambientProgramAmbientIndex;

    private int       animRotxDegree = 0;
    private int       animRotyDegree = 0;
    private int       animRotzDegree = 0;
    private int       animMaxDeltaRotDegree = 1;

    private float       animMinEyeDist = -1f;
    private float       animMaxEyeDist = 10;
    private float       animDeltaEyeDist = 0.1f;
    private float       animCurrentEyeDist = 1;

    private float[]     ambientColor = new float[]{
            1f, // scale
            3f, // near eye-dist effect
            0.1f, // min lighting factor
            1.0f}; // extinction power

    private boolean     resourcesAreUp = false;
    private IRenderable renderedMesh;


    public TestPreviewImageProcessor(SurfaceView output_view, TextView output_message, Activity parent_activity){
        super();
        outputView = output_view;
        outputMessage = output_message;
        parentActivity = parent_activity;
//        renderedMesh = new TriangleMesh(
//                new float[]{
//                        -0.5f, 0, 0, 1,
//                        0.5f, 0, 0, 1,
//                        0, 0.5f, 0,1},
//                new float[]{
//                        1, 0, 0, 1f,
//                        0, 1, 0, 1f,
//                        0, 0, 1, 1f},
//                null,
//                GLES31.GL_STATIC_DRAW);

        renderedMesh = new AxisAlignedBoxMesh(
                1f,1f,1f,
                new float[]{-0.5f,-0.5f,0.5f,1},
                new float[]{1,1,1, 1},
                true, GLES31.GL_STATIC_DRAW);
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
        processor_bundle.setCurrentContext(outputView);

        setupOpenGlResources(processor_bundle);

        // Compute model position and orientation
        float[] _model_movt = new float[16];
        Matrix.setIdentityM(_model_movt, 0);

        Matrix.translateM(_model_movt, 0, 0, 0, - animCurrentEyeDist);

        Matrix.rotateM(_model_movt, 0, animRotxDegree, 1, 0, 0);
        Matrix.rotateM(_model_movt, 0, animRotyDegree, 0, 1, 0);
        Matrix.rotateM(_model_movt, 0, animRotzDegree, 0, 0, 1);

        animRotxDegree = (animRotxDegree +animMaxDeltaRotDegree*1 )%360;
        animRotyDegree = (animRotyDegree +animMaxDeltaRotDegree*2 )%360;
        animRotzDegree = (animRotzDegree +animMaxDeltaRotDegree*3 )%360;

       animCurrentEyeDist += animDeltaEyeDist;
        if ((animCurrentEyeDist < animMinEyeDist)||(animCurrentEyeDist > animMaxEyeDist)){
            animDeltaEyeDist *= -1f;
        }

        float[] _mvp = new float[16];
        Matrix.multiplyMM(_mvp, 0, mvpMatrix, 0, _model_movt, 0);


        // backface culling
        GLES31.glCullFace(GLES31.GL_BACK);
        GLES31.glEnable(GLES31.GL_CULL_FACE);

        // this is necessary to make parts of the view independent
        GLES31.glEnable(GLES31.GL_SCISSOR_TEST);

        GLES31.glEnable(GLES31.GL_DEPTH_TEST);
        GLES31.glDepthFunc(GLES31.GL_LESS);

        // We draw in the left half part of the view
        GLES31.glViewport(0,0, outputViewWidthPixel/2, outputViewHeightPixel);
        GLES31.glScissor(0,0,outputViewWidthPixel/2, outputViewHeightPixel);
        GlUtilities.ensureGles31Call("glViewport(0,0)", ()-> releaseOpenGlResources());
        GLES31.glClearColor(0.1f, 0, 0, 0);
        redLevel += dRedLevel;
        if (redLevel > 1f){
            redLevel = 1;
            dRedLevel *= -1f;
        }
        if (redLevel <0f){
            redLevel = 0;
            dRedLevel *= -1f;
        }
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT ); //| GLES31.GL_STENCIL_BUFFER_BIT);
        GlUtilities.ensureGles31Call("glClear", ()-> releaseOpenGlResources());
        int _left = renderModel(_mvp);

        // We draw in the right half part of the view
        GLES31.glViewport(outputViewWidthPixel/2,0, outputViewWidthPixel/2, outputViewHeightPixel);
        GLES31.glScissor(outputViewWidthPixel/2,0,outputViewWidthPixel/2,outputViewHeightPixel);
        GlUtilities.ensureGles31Call("glViewport(w/2,0)", ()-> releaseOpenGlResources());

       // glClear is not limited by the viewport but by scissor.
        GLES31.glClearColor(0, 0.5f, 0, 0);
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT | GLES31.GL_STENCIL_BUFFER_BIT);
        GlUtilities.ensureGles31Call("glClear", ()-> releaseOpenGlResources());
        int _right = renderModel(_mvp);

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

    private int renderModel(float[] mvp_mat){

        // Draw the object
        GLES31.glUseProgram(identityShaderProgram);
        GlUtilities.ensureGles31Call("glUseProgram(shaderProgram = " + identityShaderProgram +") ", ()-> releaseOpenGlResources());


        GLES31.glEnable(GLES31.GL_BLEND);
        GlUtilities.ensureGles31Call("glEnable(GL_BLEND)", ()-> releaseOpenGlResources());

        GLES31.glBlendColor(1.0f, 0.8f, 0.2f, 1f);
        GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ONE_MINUS_SRC_ALPHA);



        GLES31.glUniformMatrix4fv(identityProgramMvpIndex, 1, false, FloatBuffer.wrap(mvp_mat));
        renderedMesh.draw(identityShaderProgram, IRenderable.MeshStyle.FILLED);

        GLES31.glUniform4fv(ambientProgramAmbientIndex, 1, ambientColor, 0);
        GlUtilities.ensureGles31Call("glUniform4f(ambientProgramAmbientIndex)", ()-> releaseOpenGlResources());


        // Outline the objects in black
        GLES31.glDisable(GLES31.GL_BLEND);
        GLES31.glLineWidth(5);
        GLES31.glUseProgram(colorShaderProgram);
        GlUtilities.ensureGles31Call("glUseProgram(shaderProgram = " + colorShaderProgram +") ", ()-> releaseOpenGlResources());
        int _color_unif_index = GLES31.glGetUniformLocation(colorShaderProgram, ShaderAttributes.COLOR.toString());
        GlUtilities.assertGles31Call(_color_unif_index != -1, "glGetUniformLocation ( color )", ()->releaseOpenGlResources());
        GlUtilities.ensureGles31Call("glGetUniformLocation ( color )", ()->releaseOpenGlResources());
        GLES31.glUniform4f( _color_unif_index, 0, 0, 0, 1 );


        GLES31.glUniformMatrix4fv(colorProgramMvpIndex, 1, false, FloatBuffer.wrap(mvp_mat));

        renderedMesh.draw(colorShaderProgram, IRenderable.MeshStyle.LINES);

        // Show objects vertices in white
        GLES31.glUniform4fv( _color_unif_index, 1, ambientColor, 0);

        GLES31.glUseProgram(identityShaderProgram);
        GlUtilities.ensureGles31Call("glUseProgram(shaderProgram = " + identityShaderProgram +") ", ()-> releaseOpenGlResources());
        GLES31.glLineWidth(30);
        renderedMesh.draw(identityShaderProgram, IRenderable.MeshStyle.POINTS);


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
        identityShaderProgram = ShaderUtility.makeProgramFromShaders(
                AssembledShader.assembleShaders(Arrays.asList( new ShaderCodeSnippet[]{
                        StockVertexShaderSnippets.INTERPOLATED_COLOR_CODE,
                        StockVertexShaderSnippets.MODEL_VIEW_PROJECTION_VERTEX_CODE,
                        StockVertexShaderSnippets.EYE_VERTEX_CODE
                })),
                AssembledShader.assembleShaders(Arrays.asList(new ShaderCodeSnippet[]{
                        StockFragmentShaderSnippets.AMBIENT_LIGHT_CODE
                })));

        colorShaderProgram = ShaderUtility.makeProgramFromShaders(
                AssembledShader.assembleShaders(Arrays.asList(new ShaderCodeSnippet[]{
                        StockVertexShaderSnippets.UNICOLOR_CODE,
                        StockVertexShaderSnippets.MODEL_VIEW_PROJECTION_VERTEX_CODE
                })),
                AssembledShader.assembleShaders(Arrays.asList(new ShaderCodeSnippet[]{
                        StockFragmentShaderSnippets.IDENTITY_FRAGMENT_CODE
                }))
        );

        logD("Identity Shader program = " + identityShaderProgram);
        logD("Color Shader program = " + colorShaderProgram);

        renderedMesh.setupOpenGlResources();

        outputViewWidthPixel = outputView.getWidth();
        outputViewHeightPixel = outputView.getHeight();

        float[] _view_matrix = new float[16];
        Matrix.setLookAtM(_view_matrix, 0,
                0, 0, 2,
                0, 0 ,0,
                0, 1, 0
        );

        float[] _projection_matrix = new float[16];

        float _aspect_ratio = (float)outputViewWidthPixel/2f/(float)outputViewHeightPixel;
        logD(" WIDTH = " + outputViewWidthPixel);
        logD("HEIGHT = " + outputViewHeightPixel);
        logD("Aspect ratio = " + _aspect_ratio);

        Matrix.perspectiveM(_projection_matrix, 0, 45, _aspect_ratio, 0.1f, 100);

        mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, _projection_matrix, 0, _view_matrix, 0);

        identityProgramMvpIndex = GLES31.glGetUniformLocation(identityShaderProgram, ShaderAttributes.MVP.toString());
        GlUtilities.assertGles31Call(identityProgramMvpIndex != -1, "glGetUniformLocation ( mvp )", ()->releaseOpenGlResources());
        GlUtilities.ensureGles31Call("glGetUniformLocation ( mvp )", ()->releaseOpenGlResources());

        colorProgramMvpIndex = GLES31.glGetUniformLocation(colorShaderProgram, ShaderAttributes.MVP.toString());
        GlUtilities.assertGles31Call(colorProgramMvpIndex != -1, "glGetUniformLocation ( mvp )", ()->releaseOpenGlResources());
        GlUtilities.ensureGles31Call("glGetUniformLocation ( mvp )", ()->releaseOpenGlResources());

        ambientProgramAmbientIndex = GLES31.glGetUniformLocation(identityShaderProgram, ShaderAttributes.AMBIENT.toString());
        GlUtilities.assertGles31Call(ambientProgramAmbientIndex != -1, "glGetUniformLocation ( ambient )", ()->releaseOpenGlResources());
        GlUtilities.ensureGles31Call("glGetUniformLocation ( ambient )", ()->releaseOpenGlResources());

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

        renderedMesh.releaseOpenGlResources();

        resourcesAreUp = false;
    }

    Random myRandom = new Random();

    @Override
    public String getLogName() {
        return "PreviewImageProcessor";
    }
}
