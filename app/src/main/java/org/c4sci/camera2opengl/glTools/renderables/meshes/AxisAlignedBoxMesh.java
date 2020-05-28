package org.c4sci.camera2opengl.glTools.renderables.meshes;

import android.opengl.GLES31;

import org.c4sci.camera2opengl.RenderingRuntimeException;
import org.c4sci.camera2opengl.glTools.GlUtilities;

import java.util.List;

public class AxisAlignedBoxMesh extends AbstractMesh {

    private final static int BOX_VERTEX_COUNT = 8;
    private final static int FLOAT_PER_VERTEX = 4;
    private static final int STRIP_LENGTH = 10;

    private static final int POINTS_COUNT = 8;
    private static final int FAN_OFFSET = POINTS_COUNT * 2; // 2 = size of shorts
    private static final int FAN_COUNT = 8;
    private static final int FAN_2_OFFSET = FAN_OFFSET + FAN_COUNT * 2;
    private static final int LINES_OFFSET = FAN_2_OFFSET + FAN_COUNT * 2;
    private static final int LINES_LENGTH = 9;

    private boolean isFlat;

    public AxisAlignedBoxMesh(float box_width, float box_height, float box_depth, float[] box_lower_point, int mesh_usage,

                              List<DataToVbo> per_vertex_data){
        super(computeVertices(box_width, box_height, box_depth, box_lower_point), per_vertex_data, mesh_usage);
    }
    
    @Override
    public short[] computeVertexIndices() {
        /*
        Vertex indices are :

             Y  3_____7
             | /     /|
            2 /____6/ |
             |  1  | /5
             |_____|/_____X
             0     4
            /
           Z
         */

        short[] _res =new short[]{
                0, 1, 2, 3, 4, 5, 6, 7,  // POINTS
                0, 1, 5, 4, 6, 2, 3, 1, //  FILLED :    First fan
                7, 5, 1, 3, 2, 6, 4, 5,  //             Second fan
                0, 4, 6, 7, 3, 1, 0 , 2, 3 // LINES 1

        };
        return _res;
    }

    @Override
    protected void drawMesh(int shader_program, MeshStyle mesh_style) {
        GLES31.glBindVertexArray(super.vertexArrayObject);
        int _gl_mode;
        switch(mesh_style){
            case LINES:
                _gl_mode = GLES31.GL_LINE_LOOP;
                GLES31.glDrawElements(_gl_mode, LINES_LENGTH, GLES31.GL_UNSIGNED_SHORT, LINES_OFFSET);
                GlUtilities.ensureGles31Call("glDrawElements( lines)");
                break;
            case FILLED:
                _gl_mode = GLES31.GL_TRIANGLE_FAN;
                GLES31.glDrawElements(_gl_mode, FAN_COUNT, GLES31.GL_UNSIGNED_SHORT, FAN_OFFSET);
                GlUtilities.ensureGles31Call("glDrawElements( first FAN)");
                GLES31.glDrawElements(_gl_mode, FAN_COUNT, GLES31.GL_UNSIGNED_SHORT, FAN_2_OFFSET);
                GlUtilities.ensureGles31Call("glDrawElements( second FAN)");
                break;
            case POINTS:
                _gl_mode = GLES31.GL_POINTS;
                GLES31.glDrawElements(_gl_mode, POINTS_COUNT, GLES31.GL_UNSIGNED_SHORT, 0);
                GlUtilities.ensureGles31Call("glDrawElements( points )");
                break;
            default:
                throw new RenderingRuntimeException("Unmanaged mesh style: " + mesh_style);
        }
   }
    /*
     * The vertices are ordered as following nested loops on X, then Y then Z.
     */

    private static float[] computeVertices(float box_width, float box_height, float box_depth, float[] box_low_point){
        return forEach((vertices_, offset_, x_, y_, z_) -> {
            vertices_[offset_] = box_width * (float)x_ + box_low_point[0];
            vertices_[offset_+1] = box_height * (float)y_ + box_low_point[1];
            vertices_[offset_+2] = - box_depth * (float)z_ + box_low_point[2];
            vertices_[offset_+3] = 1;
        });
    }

    /**
     * Creates data per vertex.<br>
     * Calls a vertex processor for each box vertex with coordinates in [0,1]^3
     * @param v_proc the vertex processor that fills 4 floats for each vertex
     * @return The vertex data
     */
    public static float[] forEach(VertexIntProcessor v_proc){
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
