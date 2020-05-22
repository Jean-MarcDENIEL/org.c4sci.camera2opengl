package org.c4sci.camera2opengl.glTools.renderables.meshes;

import android.opengl.GLES31;

import org.c4sci.camera2opengl.RenderingRuntimeException;
import org.c4sci.camera2opengl.glTools.GlUtilities;
import org.c4sci.camera2opengl.glTools.renderables.meshes.AbstractMesh;

public class TriangleMesh extends AbstractMesh {
    private static final int VERTEX_PER_TRIANGLE = 3;

    public TriangleMesh(float[] xyzw_vertices, float[] rvba_colors, float[] xyzw_normals, int mesh_usage) {
        super(xyzw_vertices, rvba_colors, xyzw_normals, mesh_usage);
    }

    @Override
    public short[] computeVertexIndices() {
        return new short[]{0, 1, 2};
    }

    @Override
    protected void drawMesh(int shader_program, MeshStyle mesh_style) {
        // Tells openGL we are working with object 0
        GLES31.glBindVertexArray(vertexArrayObject);
        GlUtilities.ensureGles31Call("glBindVertexArray(vertexArrayObject)", ()->releaseOpenGlResources());

        int _gl_mode;
        switch(mesh_style){
            case LINES:
                _gl_mode = GLES31.GL_LINE_LOOP;
                break;
            case FILLED:
                _gl_mode = GLES31.GL_TRIANGLES;
                break;
            case POINTS:
                _gl_mode = GLES31.GL_POINTS;
                break;
            default:
                throw new RenderingRuntimeException("Unmanaged mesh style: " + mesh_style);
        }
        GLES31.glDrawArrays(_gl_mode, 0, VERTEX_PER_TRIANGLE);
    }
}
