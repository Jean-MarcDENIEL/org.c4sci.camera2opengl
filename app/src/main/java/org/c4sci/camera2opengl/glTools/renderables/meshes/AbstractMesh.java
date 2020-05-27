package org.c4sci.camera2opengl.glTools.renderables.meshes;

import android.opengl.GLES31;

import org.c4sci.camera2opengl.RenderingRuntimeException;
import org.c4sci.camera2opengl.glTools.GlUtilities;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderAttributes;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderUtility;
import org.c4sci.camera2opengl.glTools.renderables.IRenderable;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents meshes that are able to draw themselves. <br>
 * This class is based on OpenGL ES 3.1.
 */
public abstract class AbstractMesh implements IRenderable {

    float[] xyzwVertices = null;
    short[] vertexIndices = null;
    List<DataToVbo> dataPerVertex;
    int meshUsage = -1;

    protected int vertexArrayObject = -1;
    int lastAdaptedProgram = -1;

    //TODO
    // Use a list of DataToVBO to make Abstract Meshes more generic
    // Idem for derived classes
    /**
     * Creates a generic mesh. Data can be associated to each vertex. E.g.:<br>
     * float[] _color_data = new float[VERTEX_COUNT * DATA_PER_COLOR]
     * ...
     * fill _color_data for each vertex
     * ...
     * DataToVob _color_vbo = new DataToVbo(_color_data, ShaderAttributes.COLOR.toString(), meshUsage, DATA_PER_COLOR);
     *
     * @param xyzw_vertices Vertices sorted by nested loops on X then Y then Z. Cannot be null
     * @param per_vertex_data List of all data to assiciate to each vertex. It may be null.
     * @param mesh_usage E.g. {@link GLES31#GL_STATIC_DRAW} or the like as used by {@link GLES31#glBufferData(int, int, Buffer, int)}
     * @throws org.c4sci.camera2opengl.RenderingRuntimeException is xyzw_vertices is null
     */
    public AbstractMesh(float[] xyzw_vertices, List<DataToVbo> per_vertex_data, int mesh_usage){
        xyzwVertices = xyzw_vertices;
        meshUsage = mesh_usage;
        dataPerVertex = per_vertex_data;
    }

    /**
     * Each derived class must indicate here how are ordered the vertices when the mesh is drawn.
     * @return The list of vertex indices as used by the {@link #drawMesh(int, MeshStyle)} method.
     */
    public abstract short[] computeVertexIndices();

    /**
     *This method must be called by {@link #setupOpenGlResources()} of derived classes.
     */
    @Override
    public void setupOpenGlResources() {
        vertexIndices = computeVertexIndices();
        List<DataToVbo> _buffers = new ArrayList<>();
        _buffers.add(new DataToVbo(xyzwVertices, ShaderAttributes.VERTEX.toString(), meshUsage, DATA_PER_VERTEX));
        _buffers.addAll(dataPerVertex);

        vertexArrayObject = IRenderable.setupBuffers(_buffers, vertexIndices);
        lastAdaptedProgram = -1;
    }

    public final void draw(int shader_program, MeshStyle mesh_style) {
        if (vertexIndices == null){
            throw new RenderingRuntimeException("OpenGL resource are not set up");
        }
        if (shader_program != lastAdaptedProgram) {
            IRenderable.adaptBuffersToProgram(vertexArrayObject, shader_program);
            lastAdaptedProgram = shader_program;
        }
        // Tells openGL we are working with object 0
        GLES31.glBindVertexArray(vertexArrayObject);
        GlUtilities.ensureGles31Call("glBindVertexArray(vertexArrayObject)", ()->releaseOpenGlResources());

        drawMesh(shader_program, mesh_style);
    }

    /**
     * This method is to be called in the OpenGL thread, and in a well formed OpenGL context.
     * It contains the special code used by the derived class to draw itself.
     * @param shader_program
     * @param mesh_style
     */
    protected abstract void drawMesh(int shader_program, MeshStyle mesh_style);

    /**
     * This method must be called by derived classes method {@link #releaseOpenGlResources()}.
     */
    @Override
    public void releaseOpenGlResources() {
        if (vertexArrayObject != -1) {
            IRenderable.releaseBuffers(vertexArrayObject);
            vertexArrayObject = -1;
        }
        lastAdaptedProgram = -1;
    }

    @Override
    public int majorOpenGlVersion() {
        return 3;
    }

    @Override
    public int minorOpenGlVersion() {
        return 1;
    }
}
