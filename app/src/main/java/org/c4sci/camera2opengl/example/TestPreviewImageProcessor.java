package org.c4sci.camera2opengl.example;

import android.opengl.GLES31;
import android.view.SurfaceView;

import org.c4sci.camera2opengl.ILogger;
import org.c4sci.camera2opengl.glTools.GlUtilities;
import org.c4sci.camera2opengl.glTools.ShaderUtility;
import org.c4sci.camera2opengl.preview.PreviewImageProcessor;
import org.c4sci.camera2opengl.preview.PreviewImageBundle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

public class TestPreviewImageProcessor implements PreviewImageProcessor , ILogger {

    private SurfaceView outputViewLeft;
    private SurfaceView outputViewRight;

    private int         shaderProgram = -1;
    private IntBuffer   vertexArrayObjects = null;
    private IntBuffer   vertexBufferObjects = null;
    private int         triangleCount;
    private float       redLevel = 0;
    private float       dRedLevel = 0.02f;

    private boolean     resourcesAreUp = false;

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
        processor_bundle.setCurrentContext(outputViewLeft);

        setupOpenGlResources(processor_bundle);

        GLES31.glViewport(0,0, outputViewLeft.getWidth(), outputViewLeft.getHeight());
        GlUtilities.ensureGles31Call("glViewport", ()-> releaseOpenGlResources());

        GLES31.glClearColor(redLevel, 0, 0, 0);
        redLevel += dRedLevel;
        if (redLevel > 1f){
            redLevel = 0;
        }
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT | GLES31.GL_STENCIL_BUFFER_BIT);
        GlUtilities.ensureGles31Call("glClear", ()-> releaseOpenGlResources());

//        GLES31.glDisable(GLES31.GL_CULL_FACE);
//        GLES31.glDisable(GLES31.GL_DEPTH_TEST);

        GLES31.glUseProgram(shaderProgram);
        GlUtilities.ensureGles31Call("glUseProgram(shaderProgram = " + shaderProgram +") ", ()-> releaseOpenGlResources());

        // Tells openGL we are working with object 0
        GLES31.glBindVertexArray(vertexArrayObjects.get(0));
        GlUtilities.ensureGles31Call("glBindVertexArray(vertexArrayObjects.get(0)", ()->releaseOpenGlResources());


        IntBuffer _queries = IntBuffer.allocate(4);
        GLES31.glGenQueries(_queries.capacity(), _queries);
        GlUtilities.ensureGles31Call("glGenQueries(_queries.capacity(), _queries)", ()->releaseOpenGlResources());

        // begin counting the samples passed
        GLES31.glBeginQuery(GLES31.GL_ANY_SAMPLES_PASSED, _queries.get(0));
        GlUtilities.ensureGles31Call("glBeginQuery(GLES31.GL_ANY_SAMPLES_PASSED, _queries.get(0));", ()->releaseOpenGlResources());

        // Draw the object that is made of triangles
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, triangleCount*3);
        GlUtilities.ensureGles31Call("glDrawArrays(GLES31.GL_TRIANGLES, 0, triangleCount)", ()->releaseOpenGlResources());

        // Stops counting the samples passed
        GLES31.glEndQuery(GLES31.GL_ANY_SAMPLES_PASSED);
        GlUtilities.ensureGles31Call("glEndQuery(_queries.get(0))", ()->releaseOpenGlResources());

        IntBuffer _querie_result = IntBuffer.allocate(1);
        GLES31.glGetQueryObjectuiv(_queries.get(0), GLES31.GL_QUERY_RESULT, _querie_result);
        logD("Passed samples: " + _querie_result.get(0));

        GLES31.glDeleteQueries(_queries.capacity(), _queries);

    }

    @Override
    public void onResume() {

        logD("onResume");
    }

    private void setupOpenGlResources(PreviewImageBundle processor_bundle){
        if (resourcesAreUp){
            return;
        }
        shaderProgram = ShaderUtility.loadVertexAndFragmentShaders(
                ShaderUtility.IDENTITY_SHADER_VERTEX_CODE, ShaderUtility.IDENTITY_SHADER_FRAGMENT_CODE,
                ShaderUtility.IDENTITY_SHADER_ATTRIBUTES
        );
        logD("   Shader program = " + shaderProgram);

        // First create Vertex Buffer Arrays (VAO) to manage Vertex Buffer Objects (VBO)
        vertexArrayObjects = IntBuffer.allocate(2);
        GLES31.glGenVertexArrays(vertexArrayObjects.capacity(), vertexArrayObjects);
        GlUtilities.ensureGles31Call("glGenVertexArrays(1, vertexArrayObject)", ()-> releaseOpenGlResources());

        // We use the first VAO : it will store the succeeding VBO calls
        GLES31.glBindVertexArray(vertexArrayObjects.get(0));
        GlUtilities.ensureGles31Call("glBindVertexArray(vertexArrayObject.get(0))", ()-> releaseOpenGlResources());

        // Creates the Vertex Buffer Objects :
        // 0 : vertices coordinates
        // 1 : vertices colors
        // 2 : vertex indices
        vertexBufferObjects = IntBuffer.allocate(3);
        GLES31.glGenBuffers(vertexBufferObjects.capacity(), vertexBufferObjects);

        Runnable _release1  = () -> releaseOpenGlResources();
        GlUtilities.ensureGles31Call("glGenBuffers(3, vertexBufferObjects)",
                _release1);

        // Binds buffer 0 to coordinates, fills it with vertices coordinates and tells OpenGL what this buffer is

        // Indicates the variable bound with the buffer : vVertex
        int _vertex_loc = GLES31.glGetAttribLocation(shaderProgram, "vVertex");
        logD("  Vertex location = " + _vertex_loc);
        GlUtilities.ensureGles31Call("glGetAttribLocation(shaderProgram, vVertex)", ()->releaseOpenGlResources());
        GLES31.glEnableVertexAttribArray(_vertex_loc);
        GlUtilities.ensureGles31Call("glEnableVertexAttribArray(_vertex_loc)", ()->releaseOpenGlResources());
        // Tells OpenGL how to use this coordinates buffer

        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vertexBufferObjects.get(0));
        GlUtilities.ensureGles31Call("glBindBuffer(GLES31.GL_ARRAY_BUFFER, vertexBufferObjects.get(0)",
                _release1);
        // Fills the buffer with coordinates
        float[] _vertices = new float[]{
                -0.9f, 0, 0f, 1,
                0.9f, 0, 0f, 1,
                0, 0.9f, 0f,1
        };
        ByteBuffer _vertices_bytes = ByteBuffer.allocateDirect(_vertices.length * 4);
        _vertices_bytes.order(ByteOrder.nativeOrder());
        FloatBuffer _vertices_floats = _vertices_bytes.asFloatBuffer();
        _vertices_floats.put(_vertices);
        _vertices_floats.position(0);

        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, _vertices.length * 4, _vertices_bytes, GLES31.GL_STATIC_DRAW);
        GlUtilities.ensureGles31Call("glBufferData( vertices )", _release1);

        GLES31.glVertexAttribPointer(
                _vertex_loc,        // vertex index
                4,             // x y z w per vertex
                GLES31.GL_FLOAT,    // coordinates are floats
                false,    // fixed point float are not normalized
                0,            // 0 bytes between 2 vertex data : data are tightly packed
                0               // 0 offset
        );
        GlUtilities.ensureGles31Call("glVertexAttribPointer(...)", _release1);

        // Binds buffer 1 to colors, fills it and tells OpenGL how to read it
        // bind the buffer with colors : vColor
        int _color_loc = GLES31.glGetAttribLocation(shaderProgram, "vColor");
        logD("   Color location = " + _color_loc);
        GlUtilities.ensureGles31Call("glGetAttribLocation(shaderProgram, vColor)", ()->releaseOpenGlResources());
        GLES31.glEnableVertexAttribArray(_color_loc);
        GlUtilities.ensureGles31Call("glEnableVertexAttribArray(_color_loc)", ()->releaseOpenGlResources());

        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vertexBufferObjects.get(1));
        GlUtilities.ensureGles31Call("glBindBuffer(GLES31.GL_ARRAY_BUFFER, vertexBufferObjects.get(1)",
                _release1);
        // Fill the buffer with color (RGBA)
        float[] _colors = new float[]{
                1, 0, 0, 1f,
                0, 1, 0, 1f,
                0, 0, 1, 1f
        };
        ByteBuffer _colors_bytes = ByteBuffer.allocateDirect(_colors.length * 4);
        _colors_bytes.order(ByteOrder.nativeOrder());
        FloatBuffer _colors_float = _colors_bytes.asFloatBuffer();
        _colors_float.put(_colors);
        _colors_float.position(0);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, _colors.length * 4, _colors_bytes, GLES31.GL_STATIC_DRAW);
        GlUtilities.ensureGles31Call("glBufferData( colors )", _release1);

        // Tells OpenGL how to use this color buffer
        GLES31.glVertexAttribPointer(
                _color_loc,         // color index
                4,             // r g b a per vertex
                GLES31.GL_FLOAT,    // coordinates are floats
                false,    // fixed point float are not normalized
                0,            // 0 bytes between 2 vertex data : data are tightly packed
                0             // 0 offset
        );

        // Binds buffer 2 to vertex indices, fills it
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, vertexBufferObjects.get(2));
        GlUtilities.ensureGles31Call("glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, vertexBufferObjects.get(2))", _release1);
        // Fills with indices
        short[] _indices = new short[]{ 0, 1, 2};
        ByteBuffer _indices_bytes = ByteBuffer.allocateDirect(_indices.length * 2);
        _indices_bytes.order(ByteOrder.nativeOrder());
        ShortBuffer _indices_short = _indices_bytes.asShortBuffer();
        _indices_short.put(_indices);
        _indices_short.position(0);
        GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER, _indices.length * 2, _indices_bytes, GLES31.GL_STATIC_DRAW);
        GlUtilities.ensureGles31Call("glBufferData( indices )", _release1);
        triangleCount = _indices.length / 3;

        resourcesAreUp = true;
    }

    @Override
    public void onPause() {
        logD("onPause");
        releaseOpenGlResources();
    }

    private void releaseOpenGlResources(){
        if (shaderProgram != -1) {
            GLES31.glDeleteProgram(shaderProgram);
            shaderProgram = -1;
        }
        if (vertexArrayObjects != null) {
            GLES31.glDeleteVertexArrays(vertexArrayObjects.capacity(), vertexArrayObjects);
            vertexArrayObjects = null;
        }
        if (vertexBufferObjects != null){
            GLES31.glDeleteBuffers(vertexBufferObjects.capacity(), vertexBufferObjects);
            vertexBufferObjects = null;
        }
        resourcesAreUp = false;
    }

    Random myRandom = new Random();

    @Override
    public String getLogName() {
        return "PreviewImageProcessor";
    }
}
