package org.c4sci.camera2opengl.glTools.renderables.meshes;

import android.opengl.GLES31;

import org.c4sci.camera2opengl.RenderingRuntimeException;

public class AxeAlignedBoxMesh extends AbstractMesh {

    private final static int BOX_VERTEX_COUNT = 8;
    private final static int FLOAT_PER_VERTEX = 4;

    private boolean isFlat;

    public AxeAlignedBoxMesh(float box_width, float box_height, float box_depth, float[] box_lower_point, float[] rgba_face_color, boolean is_flat, int mesh_usage){
        super(computeVertices(box_width, box_height, box_depth), computeColors(rgba_face_color), computeNormals(is_flat), mesh_usage);
        isFlat = is_flat;
    }
    
    @Override
    public short[] computeVertexIndices() {
        /*
        Vertex indices are :

           Y ^  3_____7
             | /     /|
            2 /____6/ |5
             |  1  | /
             |_____|/_____ > X
             0     4

         */
        short[] _res = new short[BOX_VERTEX_COUNT * 2];
        // First : strip around vertical faces
        _res[0] = 0;
        _res[1] = 4;
        _res[2] = 1;
        _res[3] = 5;
        _res[4] = 3;
        _res[5] = 7;
        _res[6] = 6;
        _res[7] = 2;
        return _res;
    }

    @Override
    protected void drawMesh(int shader_program, MeshStyle mesh_style) {
        GLES31.glBindVertexArray(super.vertexArrayObject);
        int _gl_mode;
        switch(mesh_style){
            case LINES:
                _gl_mode = GLES31.GL_LINE_STRIP;
                break;
            case FILLED:
                _gl_mode = GLES31.GL_TRIANGLE_STRIP;
                break;
            case POINTS:
                _gl_mode = GLES31.GL_POINTS;
                break;
            default:
                throw new RenderingRuntimeException("Unmanaged mesh style: " + mesh_style);
        }
        GLES31.glDrawArrays(_gl_mode, 0, BOX_VERTEX_COUNT);

    }
    /*
     * The vertices are ordered as following nested loops on X, then Y then Z.
     */

    private static float[] computeVertices(float box_width, float box_height, float box_depth){
        return forEach((vertices_, offset_, x_, y_, z_) -> {
            vertices_[offset_] = box_width * x_;
            vertices_[offset_+1] = box_height * y_;
            vertices_[offset_+2] = box_depth * z_;
            vertices_[offset_+3] = 1;
        });
    }

    private static float[] computeColors(float[] box_color){
        return forEach((vertices_, offset_, x_, y_, z_) -> {
            vertices_[offset_] =    box_color[0];
            vertices_[offset_+1] =  box_color[1];
            vertices_[offset_+2] =  box_color[2];
            vertices_[offset_+3] =  box_color[3];
        });
    }

    private static float[] computeNormals(boolean is_flat){
        float _norm = (float)Math.sqrt(3.0*0.5*0.5);
        return forEach((vertices_, offset_, x_, y_, z_) -> {
            vertices_[offset_] =    ((float)x_ - 0.5f)/_norm;
            vertices_[offset_+1] =  ((float)y_ - 0.5f)/_norm;
            vertices_[offset_+2] =  ((float)z_ - 0.5f)/_norm;
            vertices_[offset_+3] =  1;
        });
    }

    private interface VertexProcessor{
        void processVertex(float[] vertices_, int offset_, int x, int y, int z);
    }
    private static float[] forEach(VertexProcessor v_proc){
        float[] _res = new float[BOX_VERTEX_COUNT * FLOAT_PER_VERTEX];
        int _i = 0;
        for (int _x=0; _x<=1; _x++){
            for (int _y=0; _y<= 1; _y++){
                for (int _z=0; _z<=1; _z++){
                    v_proc.processVertex(_res, _i, _x, _y, _z);
                    _i += 4;
                }
            }
        }
        return _res;
    }
}
