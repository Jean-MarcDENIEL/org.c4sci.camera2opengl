package org.c4sci.camera2opengl.glTools.renderables.meshes;

/**
 * This interface is responsible for associating a data to a mesh vertex
 * in function of its x y z inner coordinates.
 */
public interface VertexProcessor{
    /**
     * Fills the data buffer with
     * @param vertices_ X Y Z W vector per vertex
     * @param offset_ The index to fill the buffer in
     * @param x x in [0,1] depending on the meaning given by the method user
     * @param y y in [0,1] depending on the meaning given by the method user
     * @param z z in [0,1] depending on the meaning given by the method user
     */
    void processVertex(float[] vertices_, int offset_, int x, int y, int z);
}
