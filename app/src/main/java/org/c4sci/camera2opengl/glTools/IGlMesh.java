package org.c4sci.camera2opengl.glTools;

import android.opengl.GLES31;

import org.c4sci.camera2opengl.RenderingRuntimeException;

import java.nio.Buffer;
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
     * Setup Vertex Object Array (VOA) and Vertex Buffers Objects (VBO) as well as VBO for indices.
     * All VBO must follow the convention for {@link ShaderUtility.ShaderAttributes}
     */
    void setupOpenGlResources();

    /**
     * Draws the object in the style. Its VBOs attributes locations are adapted to the shader program's.
     * @param shader_program The shader_program to render with.
     * @param mesh_style The kind of rendering (filled, lines ...)
     * Note that when drawing, derived class are free to do what they want with their proper shaders.
     */
    void draw(int shader_program, MeshStyle mesh_style);
    void releaseOpenGlResources();

    /**
     * Indicates the major version of OpenGL calls used inside the class methods.
     * @return e.g. 3 for OpenGL 3+
     */
    int majorOpenGlVersion();

    /**
     * Indicates the minor version of OpenGL calls used inside the class methods.
     * @return e.g. 1 for OpenGL 3.1
     */
    int minorOpenGlVersion();

    static final int BYTES_PER_FLOAT = 4;
    static final int DATA_PER_VERTEX = 4;
    static final int DATA_PER_COLOR = 4;
    static final int DATA_PER_NORMAL = 4;

    public class DataToVbo{
        float[] rawData;
        String  attributeName;
        int     bufferUsage;
        int     dataCountPerVertex;

        /**
         * Data to be put ion a Vertex Buffer Object (VBO)
         * @param raw_data Raw data.
         * @param attribute_name The attribute name following the convention on {@link ShaderUtility.ShaderAttributes}
         * @param buffer_usage Indicates how data will be used. Same args as {@link GLES31#glBufferData(int, int, Buffer, int)}: {@link GLES31#GL_STATIC_DRAW} ....
         * @param data_count_per_vertex The number of floats per vertex. Usually 4 for vectors (x y z w) or colors (r g b a).
         */
        public DataToVbo(float[] raw_data, String attribute_name, int buffer_usage, int data_count_per_vertex) {
            this.rawData = raw_data;
            this.attributeName = attribute_name;
            this.bufferUsage = buffer_usage;
            this.dataCountPerVertex = data_count_per_vertex;
        }
    }


    static public int setupBuffers(List<DataToVbo> buffers_data,   short[] v_indices){
        // First, ensure that at least one buffer contains vertices coordinates
        int _vertices_index = -1;
        for (int _i=0; _i< buffers_data.size() && _vertices_index == -1; _i++){
            if (buffers_data.get(_i).attributeName.contentEquals(ShaderUtility.ShaderAttributes.VERTEX.attributeName())){
               _vertices_index = _i;
            }
        }
        if (_vertices_index < 0){
            throw new RenderingRuntimeException("Cannot create a mesh without vertices");
        }

        // Second ensure that all those buffers offer one data vector per vertex
        int _vertex_count = buffers_data.get(_vertices_index).rawData.length / buffers_data.get(_vertices_index).dataCountPerVertex;
        for (DataToVbo _data : buffers_data){
            int _data_count = _data.rawData.length / _data.dataCountPerVertex;
            if (_data_count != _vertex_count){
                throw new RenderingRuntimeException("VBO " + _data.attributeName + ": bad data count =" + _data_count + " expected " + _vertex_count);
            }
        }

        if (v_indices == null){
            throw new RenderingRuntimeException("Cannot create a mesh withtout vertex indices");
        }

        // Creates the VAO associated with the object
        // to manage Vertex Buffer Objects (VBO) dedicated to an object to draw.
        IntBuffer _vao = IntBuffer.allocate(1);
        GLES31.glGenVertexArrays(_vao.capacity(), _vao);
        GlUtilities.ensureGles31Call("glGenVertexArrays(1, _vao)");

        // Bind the VAO so it will store the following VBO calls
        GLES31.glBindVertexArray(_vao.get(0));
        GlUtilities.ensureGles31Call("glBindVertexArray(vertexArrayObject.get(0))", ()-> releaseBuffers(_vao.get(0)));

        // Creates the Vertex Buffer Objects : vertex coordinates, vertex colors, vertex normals ....
        // and binds them according to their ShaderUtility.ShaderAttributes and usage
        IntBuffer _vbos = IntBuffer.allocate(buffers_data.size()+1);
        GLES31.glGenBuffers(_vbos.capacity(), _vbos);

        for (int _i=0; _i < buffers_data.size(); _i++){
            int _vbo = _vbos.get(_i);
            DataToVbo _data = buffers_data.get(_i);
            setupBuffer(_vao.get(0), _vbo, FloatBuffer.wrap(_data.rawData), GLES31.GL_FLOAT,
                    BYTES_PER_FLOAT, _data.dataCountPerVertex, _data.attributeName, _data.bufferUsage);
        }

        // Binds last buffer to vertex indices, fills it
        setupIndexBuffer(_vao.get(0), _vbos.get(buffers_data.size()),  ShortBuffer.wrap(v_indices), GLES31.GL_STATIC_DRAW);

        return _vao.get(0);
    }

    static public void releaseBuffers(int vertex_array_object){

        for (Integer _vbo : vaoToVbos.get(vertex_array_object)){
            vboToDataType.remove(_vbo);
            vboToDataCountPerVertex.remove(_vbo);
            vboToAttributeName.remove(_vbo);
            GLES31.glDeleteBuffers(1, IntBuffer.wrap(new int[]{_vbo}));
        }

        Integer _vbo_index = vaoToVboIndex.get(vertex_array_object);
        if (_vbo_index != null){
            vaoToVboIndex.remove(_vbo_index);
            GLES31.glDeleteBuffers(1, IntBuffer.wrap(new int[]{_vbo_index}));
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

        System.out.println("IGL Mesh Setup buffer :" + vbo_id + "= " + attribute_name);
    }

    static public void setupIndexBuffer(int vao_id, int vbo_id, Buffer vbo_data, int buffer_usage){
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, vbo_id);
        GlUtilities.ensureGles31Call("glBindBuffer(GL_ELEMENT_ARRAY_BUFFER)", ()->releaseBuffers(vao_id));

        GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER, vbo_data.capacity() * 2, vbo_data, buffer_usage);
        GlUtilities.ensureGles31Call("glBufferData(GL_ELEMENT_ARRAY_BUFFER)", ()->releaseBuffers(vao_id));

        //vboToAttributeName.put(vbo_id, attribute_name);

        addVboIndexToVao(vao_id, vbo_id);
    }

    static void addVboIndexToVao(int vao_id, int vbo_id){
        vaoToVboIndex.put(vao_id, vbo_id);
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

        if (_attrib_loc >= 0) {
            // Binds the VBO so we can work on it. Otherwise we would modify something else in the OpenGL state machine.
            GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo_id);

            // Here the attribute is used by the shader program
            GLES31.glEnableVertexAttribArray(_attrib_loc);
            GlUtilities.ensureGles31Call("glEnableVertexAttribArray", () -> releaseBuffers(vao_id));

            // Tells OpenGL how to use this coordinates buffer
            GLES31.glVertexAttribPointer(
                    _attrib_loc,                            // attribute index in the program
                    vboToDataCountPerVertex.get(vbo_id),    //eg. 4 for x y z w per vertex
                    vboToDataType.get(vbo_id),              // coordinates are floats
                    false,                        // fixed point float are not normalized
                    0,                                // 0 bytes between 2 vertex data : data are tightly packed
                    0                                 // 0 offset
            );
            GlUtilities.ensureGles31Call("glVertexAttribPointer(...)", () -> releaseBuffers(vao_id));
        }
    }

    static Map<Integer, Integer>  vboToDataCountPerVertex = new ConcurrentHashMap<>();
    static Map<Integer, String> vboToAttributeName = new ConcurrentHashMap<>();
    static Map<Integer, Integer> vboToDataType = new ConcurrentHashMap<>();
    static Map<Integer, List<Integer>> vaoToVbos = new ConcurrentHashMap<>();
    static Map<Integer, Integer> vaoToVboIndex = new ConcurrentHashMap<>();
}
