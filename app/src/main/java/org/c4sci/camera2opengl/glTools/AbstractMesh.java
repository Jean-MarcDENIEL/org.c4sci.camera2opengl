package org.c4sci.camera2opengl.glTools;

import android.opengl.GLES31;

import org.c4sci.camera2opengl.RenderingRuntimeException;

import java.nio.Buffer;
import java.nio.IntBuffer;

/**
 * This class represents 6-face boxes.
 */
public abstract class AbstractMesh implements IGlMesh {

    float[] xyzwVertices = null;
    float[] rvbaColors = null;
    float[] xyzwNormals = null;
    short[] vertexIndices = null;
    int meshUsage = -1;

    int vertexArrayObject = -1;
    int lastAdaptedProgram = -1;


    /**
     * Creates a six face box whose vertices are given in parameters. The vertices are ordered as in nested Loops on X then Y then Z :
     * [0,0,0], [0,0,1], [0,1,0] ... [1,1,1]
     * @param xyzw_vertices Vertices sorted by nested loops on X then Y then Z. Cannot be null
     * @param rvba_colors RVBA colors in vertices order. May be null.
     * @param xyzw_normals Normals in vertices order. May be null.
     * @param mesh_usage E.g. {@link GLES31#GL_STATIC_DRAW} or the like as used by {@link GLES31#glBufferData(int, int, Buffer, int)}
     * @throws org.c4sci.camera2opengl.RenderingRuntimeException is xyzw_vertices is null
     */
    public AbstractMesh(float[] xyzw_vertices, float[] rvba_colors, float[] xyzw_normals, int mesh_usage){
        xyzwVertices = xyzw_vertices;
        rvbaColors = rvba_colors;
        xyzwNormals = xyzw_normals;
        meshUsage = mesh_usage;

        //TODO
        // Adat this code in derived classes
//        if (xyzw_vertices.length / 4 != 8){
//            throw new RenderingRuntimeException("Cannot create a box without 8 vertices: currently " + (xyzwVertices.length / 4));
//        }
    }

    public abstract short[] computeVertexIndices();

    /**
     * In case of derived classes override, this method must be called by {@link #setupOpenGlResources()}
     */
    @Override
    public void setupOpenGlResources() {
        vertexIndices = computeVertexIndices();
        vertexArrayObject = IGlMesh.setupBuffers(xyzwVertices, rvbaColors, xyzwNormals, vertexIndices);
        lastAdaptedProgram = -1;
    }

    /**
     * This method must be first called by derived classes {@link #draw(int)} method that must override
     * it to complete the drawing.
     * @param shader_program Shader program id.
     */
    @Override
    public void draw(int shader_program) {
        if (vertexIndices == null){
            throw new RenderingRuntimeException("OpenGL resource are not set up");
        }
        if (shader_program != lastAdaptedProgram) {
            IGlMesh.adaptBuffersToProgram(vertexArrayObject, shader_program);
            lastAdaptedProgram = shader_program;
        }
    }

    /**
     * This method must be called by derived classes method {@link #releaseOpenGlResources()}
     * in case they override it.
     */
    @Override
    public void releaseOpenGlResources() {
        if (vertexArrayObject != -1) {
            IGlMesh.releaseBuffers(vertexArrayObject);
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
