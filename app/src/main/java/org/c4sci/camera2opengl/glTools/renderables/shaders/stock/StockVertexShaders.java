package org.c4sci.camera2opengl.glTools.renderables.shaders.stock;

import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderAttributes;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderCode;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderVariable;

import java.util.Arrays;

public class StockVertexShaders {

    public static final ShaderVariable VERTEX_INPUT;
    public static final ShaderVariable COLOR_INPUT;
    public static final ShaderVariable COLOR_UNIFORM;
    public static final ShaderVariable MVP_UNIFORM;

    public static final ShaderVariable VARYING_COLOR_OUTPUT;

    /**
     * This shader takes a {@link StockVertexShaders#COLOR_UNIFORM} as uniform and applies it to vertices as {@link StockVertexShaders#VARYING_COLOR_OUTPUT}.
     */
    public static final ShaderCode UNICOLOR_CODE;
    /**
     * This shader takes vertices color {@link StockVertexShaders#COLOR_INPUT} to interpolate as {@link StockVertexShaders#VARYING_COLOR_OUTPUT}.
     */
    public static final ShaderCode INTERPOLATED_COLOR_CODE;
    /**
     * This shader applies a uniform {@link StockVertexShaders#MVP_UNIFORM} to gl_Position
     */
    public static final ShaderCode MODEL_VIEW_PROJECTION_VERTEX_CODE;
    /**
     * This shader passes as they are in VBOs
     * <ul>
     *     <li>{@link ShaderAttributes#VERTEX} to gl_Position, and </li>
     *     <li> {@link ShaderAttributes#COLOR} to {@link StockVertexShaders#VARYING_COLOR_OUTPUT} </li>
     *     respectively.
     */
    public static final ShaderCode IDENTITY_VERTEX_CODE;

    static {
        VERTEX_INPUT = new ShaderVariable(ShaderAttributes.VERTEX.toString(),
                ShaderVariable.StorageQualifier.INPUT,
                ShaderVariable.VEC_4,
                ShaderAttributes.VERTEX.ordinal());

        COLOR_INPUT = new ShaderVariable(ShaderAttributes.COLOR.toString(),
                ShaderVariable.StorageQualifier.INPUT,
                ShaderVariable.VEC_4,
                ShaderAttributes.COLOR.ordinal());

        COLOR_UNIFORM = new ShaderVariable(ShaderAttributes.COLOR.toString(),
                ShaderVariable.StorageQualifier.UNIFORM,
                ShaderVariable.VEC_4,
                ShaderVariable.UNBOUND_VARIABLE);

        MVP_UNIFORM = new ShaderVariable(ShaderAttributes.MVP.toString(),
                ShaderVariable.StorageQualifier.UNIFORM,
                ShaderVariable.MAT_4,
                ShaderVariable.UNBOUND_VARIABLE);

        VARYING_COLOR_OUTPUT = new ShaderVariable("vVaryingColor",
                ShaderVariable.StorageQualifier.OUTPUT,
                ShaderVariable.VEC_4, ShaderVariable.UNBOUND_VARIABLE);


        UNICOLOR_CODE = new ShaderCode(
                Arrays.asList(new ShaderVariable[]{COLOR_UNIFORM, VARYING_COLOR_OUTPUT}),
                VARYING_COLOR_OUTPUT.getName() + " = " + COLOR_UNIFORM.getName() +";\n",
                null);

        INTERPOLATED_COLOR_CODE = new ShaderCode(
                Arrays.asList(new ShaderVariable[]{COLOR_INPUT, VARYING_COLOR_OUTPUT}),
                VARYING_COLOR_OUTPUT.getName() + " = " + COLOR_INPUT.getName() +";\n",
                null);

        IDENTITY_VERTEX_CODE = new ShaderCode(
                Arrays.asList(new ShaderVariable[]{VERTEX_INPUT}),
                "gl_Position = " + VERTEX_INPUT.getName() +";\n",
                null);

        MODEL_VIEW_PROJECTION_VERTEX_CODE = new ShaderCode(
                Arrays.asList(new ShaderVariable[]{VERTEX_INPUT, MVP_UNIFORM}),
                "gl_Position = " + MVP_UNIFORM.getName() + " * " + VERTEX_INPUT.getName() + ";\n",
                null);
    }
}
