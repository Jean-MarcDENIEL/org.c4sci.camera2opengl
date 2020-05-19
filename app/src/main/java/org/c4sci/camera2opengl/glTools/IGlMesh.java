package org.c4sci.camera2opengl.glTools;

import android.opengl.GLES31;

import org.c4sci.camera2opengl.RenderingRuntimeException;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This interface defines objects that are able to draw themselves by calling OopenGL draw... methods.
 * These objects can be made of sub objects.
 * These objects use OpenGL 3.1 calls.
 */
public interface IGlMesh {

    public enum MeshStyle {
        FILLED,
        LINES,
        POINTS
    }

    /**
     * Setup Vertex Object Array (VOA) and Vertex Buffers Objects (VBO).
     * All VBO must follow the convention for {@link ShaderUtility.ShaderAttributes}
     */
    void setupOpenGlResources();
    void draw(int shader_program);
    void releaseOpenGlResources();
    int majorOpenGlVersion();
    int minorOpenGlVersion();

    static final int BYTES_PER_FLOAT = 4;
    static final int DATA_PER_VERTEX = 4;
    static final int DATA_PER_COLOR = 4;
    static final int DATA_PER_NORMAL = 4;

    static public int setupBuffers(float[] xyzw_vertices, float[] rvba_colors, float[] xyzw_normals, short[] v_indices){
        if (xyzw_vertices == null){
            throw new RenderingRuntimeException("Cannot create a mesh without vertices");
        }
        if ((rvba_colors != null)&&(rvba_colors.length != xyzw_vertices.length)){
            throw new RenderingRuntimeException("Colors bad number: " + rvba_colors.length + "instead of " + xyzw_vertices.length + " floats");
        }
        if ((xyzw_normals != null) && (xyzw_normals.length != xyzw_vertices.length)){
            throw new RenderingRuntimeException("Normals bad number:" + xyzw_normals.length + "instead of " + xyzw_vertices.length + "floats");
        }
        if (v_indices == null){
            throw new RenderingRuntimeException("Cannot create a mesh withtout vertex indices");
        }

        // Creates the VAO associated with the object
        // to manage Vertex Buffer Objects (VBO) dedicated to an object to draw.
        IntBuffer _vao = IntBuffer.allocate(1);
        GLES31.glGenVertexArrays(_vao.capacity(), _vao);
        GlUtilities.ensureGles31Call("glGenVertexArrays(1, _vao)");

        // Bind the VAO so it will store the succeeding VBO calls
        GLES31.glBindVertexArray(_vao.get(0));
        GlUtilities.ensureGles31Call("glBindVertexArray(vertexArrayObject.get(0))", ()-> releaseBuffers(_vao.get(0)));

        // Creates the Vertex Buffer Objects : vertex coordinates, vertex colors, vertex normals
        // and binds them according to ShaderUtility.ShaderAttributes
        // 0 : vertex coordinates
        // 1 : vertex colors
        // 2 : vertex normals
        // 3 : mesh indices

        IntBuffer _vbos = IntBuffer.allocate(4);
        GLES31.glGenBuffers(_vbos.capacity(), _vbos);

        // Buffer 0 = Vertices
        setupBuffer(_vao.get(0), _vbos.get(0), FloatBuffer.wrap(xyzw_vertices), GLES31.GL_FLOAT,
                BYTES_PER_FLOAT, DATA_PER_VERTEX, ShaderUtility.ShaderAttributes.VERTEX.variableName(), GLES31.GL_STATIC_DRAW);

        if (rvba_colors != null) {
            // Buffer 1 =  colors
            setupBuffer(_vao.get(0), _vbos.get(1), FloatBuffer.wrap(rvba_colors), GLES31.GL_FLOAT,
                    BYTES_PER_FLOAT, DATA_PER_COLOR, ShaderUtility.ShaderAttributes.COLOR.variableName(), GLES31.GL_STATIC_DRAW);
        }
        else{
            GLES31.glDeleteBuffers(1, IntBuffer.wrap(new int[]{_vbos.get(1)}));
        }

        if (xyzw_normals != null) {
            // Buffer 2 = normals
            setupBuffer(_vao.get(0), _vbos.get(2), FloatBuffer.wrap(xyzw_normals), GLES31.GL_FLOAT,
                    BYTES_PER_FLOAT, DATA_PER_NORMAL, ShaderUtility.ShaderAttributes.NORMAL.variableName(), GLES31.GL_STATIC_DRAW);
        }
        else{
            GLES31.glDeleteBuffers(1, IntBuffer.wrap(new int[]{_vbos.get(1)}));
        }

        // Binds buffer 3 to vertex indices, fills it
        setupIndexBuffer(_vao.get(0), _vbos.get(3),  ShortBuffer.wrap(v_indices), GLES31.GL_STATIC_DRAW);

        return _vao.get(0);
    }

    static public void releaseBuffers(int vertex_array_object){

        for (Integer _vbo : vaoToVbos.get(vertex_array_object)){
            vboToDataType.remove(_vbo);
            vboToDataCountPerVertex.remove(_vbo);
            vboToAttributeName.remove(_vbo);
            GLES31.glDeleteBuffers(1, IntBuffer.wrap(new int[]{_vbo}));
        }

        GLES31.glDeleteVertexArrays(1, IntBuffer.wrap(new int[]{vertex_array_object}));
    }

    /**
     * @param vao_id
     * @param vbo_id
     * @param vbo_data All these data will be stored in the buffer, to its max capacity()
     * @param data_type e.g. {@link GLES31#GL_FLOAT}
     * @param attribute_name The corresponding attribute following convention on {@link ShaderUtility.ShaderAttributes}
     * @param buffer_usage Specifies the expected usage pattern of the data store. The symbolic
     *                     constant must be GL_STREAM_DRAW, GL_STREAM_READ, GL_STREAM_COPY,
     *                     GL_STATIC_DRAW, GL_STATIC_READ, GL_STATIC_COPY, GL_DYNAMIC_DRAW,
     *                     GL_DYNAMIC_READ, or GL_DYNAMIC_COPY.
     * @return
     */
    static public void setupBuffer(int vao_id, int vbo_id, Buffer vbo_data, int data_type, int bytes_per_data, int data_count_per_vertex, String attribute_name, int buffer_usage){
        // Binds buffer to attribute, fills it and tells OpenGL what this buffer is
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo_id);
        GlUtilities.ensureGles31Call("glBindBuffer", ()->releaseBuffers(vao_id));

        // Fills the buffer with data
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, vbo_data.capacity() * bytes_per_data, vbo_data, buffer_usage);
        GlUtilities.ensureGles31Call("glBufferData", ()->releaseBuffers(vao_id));

        vboToAttributeName.put(vbo_id, attribute_name);
        vboToDataCountPerVertex.put(vbo_id, data_count_per_vertex);
        vboToDataType.put(vbo_id, data_type);

        addVboToVao(vao_id, vbo_id);
    }

    static public void setupIndexBuffer(int vao_id, int vbo_id, Buffer vbo_data, int buffer_usage){
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, vbo_id);
        GlUtilities.ensureGles31Call("glBindBuffer(GL_ELEMENT_ARRAY_BUFFER)", ()->releaseBuffers(vao_id));

        GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER, vbo_data.capacity() * 2, vbo_data, buffer_usage);
        GlUtilities.ensureGles31Call("glBufferData(GL_ELEMENT_ARRAY_BUFFER)", ()->releaseBuffers(vao_id));

        addVboToVao(vao_id, vbo_id);
    }

    static void addVboToVao(int vao_id, int vbo_id){
        if (!vaoToVbos.containsKey(vao_id)){
            vaoToVbos.put(vao_id, new ArrayList<>());
        }
        vaoToVbos.get(vao_id).add(vbo_id);
    }

    static public void adaptBuffersToProgram(int vao_id, int shader_program){
        for (Integer _vbo: vaoToVbos.get(vao_id)){
            adaptBufferToProgram(vao_id, shader_program, _vbo);
        }
    }

    static public void adaptBufferToProgram(int vao_id, int shader_program, int vbo_id){

        // Indicates the attribute bound with the buffer
        int _attrib_loc = GLES31.glGetAttribLocation(shader_program, vboToAttributeName.get(vbo_id));
        GlUtilities.ensureGles31Call("glGetAttribLocation", ()->releaseBuffers(vao_id));
        GLES31.glEnableVertexAttribArray(_attrib_loc);
        GlUtilities.ensureGles31Call("glEnableVertexAttribArray", ()->releaseBuffers(vao_id));

        // Tells OpenGL how to use this coordinates buffer
        GLES31.glVertexAttribPointer(
                _attrib_loc,    // attribute index in the program
                vboToDataCountPerVertex.get(vbo_id), //eg. 4 for x y z w per vertex
                vboToDataType.get(vbo_id),    // coordinates are floats
                false,    // fixed point float are not normalized
                0,            // 0 bytes between 2 vertex data : data are tightly packed
                0               // 0 offset
        );
        GlUtilities.ensureGles31Call("glVertexAttribPointer(...)", ()->releaseBuffers(vao_id));
    }

    static Map<Integer, Integer>  vboToDataCountPerVertex = new ConcurrentHashMap<>();
    static Map<Integer, String> vboToAttributeName = new ConcurrentHashMap<>();
    static Map<Integer, Integer> vboToDataType = new ConcurrentHashMap<>();
    static Map<Integer, List<Integer>> vaoToVbos = new ConcurrentHashMap<>();
}
