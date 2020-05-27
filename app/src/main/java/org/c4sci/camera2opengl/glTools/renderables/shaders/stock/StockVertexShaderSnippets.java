package org.c4sci.camera2opengl.glTools.renderables.shaders.stock;

import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderAttributes;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderCodeSnippet;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderVariable;

import java.util.Arrays;

import static org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderAttributes.TEXCOORD;

public class StockVertexShaderSnippets {

    /* ******************************* VARIABLES / ATTRIBUTES *********************************** */

    /**
     * something like "in vec4 v4Vertex"
     */
    public static final ShaderVariable VERTEX_INPUT;
    /*
     * something like "in vec4 v4Color"
     */
    public static final ShaderVariable COLOR_INPUT;
    /**
     * something like "uniform vec4 v4Color"
     */
    public static final ShaderVariable COLOR_UNIFORM;
    /**
     * something like "uniform mat4 m4Mvp"
     */
    public static final ShaderVariable MVP_UNIFORM;


    public static final ShaderVariable VARYING_COLOR_OUTPUT;

    public static final ShaderVariable VARYING_EYE_VERTEX_OUTPUT;

    public static final ShaderVariable TEXCOORD_INPUT;
    public static final ShaderVariable VARYING_TEXCOORD_OUTPUT;

    /* ************************************ CODE SNIPPETS *************************************** */

    /**
     * This shader takes a {@link StockVertexShaderSnippets#COLOR_UNIFORM} as uniform and applies it to vertices as {@link StockVertexShaderSnippets#VARYING_COLOR_OUTPUT}.
     */
    public static final ShaderCodeSnippet UNICOLOR_CODE;
    /**
     * This shader takes vertices color {@link StockVertexShaderSnippets#COLOR_INPUT} to interpolate as {@link StockVertexShaderSnippets#VARYING_COLOR_OUTPUT}.
     */
    public static final ShaderCodeSnippet INTERPOLATED_COLOR_CODE;
    /**
     * This shader applies a uniform {@link StockVertexShaderSnippets#MVP_UNIFORM} to gl_Position
     */
    public static final ShaderCodeSnippet MODEL_VIEW_PROJECTION_VERTEX_CODE;
    /**
     * This shader passes as they are in VBOs
     * <ul>
     *     <li>{@link ShaderAttributes#VERTEX} to gl_Position, and </li>
     *     <li> {@link ShaderAttributes#COLOR} to {@link StockVertexShaderSnippets#VARYING_COLOR_OUTPUT} </li>
     *     respectively.
     */
    public static final ShaderCodeSnippet IDENTITY_VERTEX_CODE;

    /**
     * This shader passes the interpolated fragment position to {@link StockVertexShaderSnippets#VARYING_EYE_VERTEX_OUTPUT}
     */
    public static final ShaderCodeSnippet EYE_VERTEX_CODE;

    /**
     * This shader interpolates {@link #TEXCOORD_INPUT} to {@link #VARYING_TEXCOORD_OUTPUT}.
     */
    public static final ShaderCodeSnippet TEXTURE_COORD_CODE_ADDON;



    static {

        /* *********** VARIABLES *************************** */


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

        VARYING_COLOR_OUTPUT = new ShaderVariable("v4VaryingColor",
                ShaderVariable.StorageQualifier.OUTPUT,
                ShaderVariable.VEC_4, ShaderVariable.UNBOUND_VARIABLE);

        VARYING_EYE_VERTEX_OUTPUT = new ShaderVariable(ShaderAttributes.EYEVERTEX.toString(),
                ShaderVariable.StorageQualifier.OUTPUT,
                ShaderVariable.VEC_4, ShaderVariable.UNBOUND_VARIABLE);

        TEXCOORD_INPUT = new ShaderVariable(ShaderAttributes.TEXCOORD.toString(),
                ShaderVariable.StorageQualifier.INPUT,
                ShaderVariable.VEC_4,
                TEXCOORD.ordinal());

        VARYING_TEXCOORD_OUTPUT = new ShaderVariable("v4VaryingTexCoord",
                ShaderVariable.StorageQualifier.OUTPUT,
                TEXCOORD_INPUT.getType(),
                ShaderVariable.UNBOUND_VARIABLE);


        /* **************** CODE SNIPPETS ********************** */

        UNICOLOR_CODE = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{COLOR_UNIFORM, VARYING_COLOR_OUTPUT}),
                VARYING_COLOR_OUTPUT + " = " + COLOR_UNIFORM +";\n",
                null);

        INTERPOLATED_COLOR_CODE = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{COLOR_INPUT, VARYING_COLOR_OUTPUT}),
                VARYING_COLOR_OUTPUT + " = " + COLOR_INPUT +";\n",
                null);



        IDENTITY_VERTEX_CODE = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{VERTEX_INPUT}),
                "gl_Position = " + VERTEX_INPUT +";\n",
                null);

        MODEL_VIEW_PROJECTION_VERTEX_CODE = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{VERTEX_INPUT, MVP_UNIFORM}),
                "gl_Position = " + MVP_UNIFORM + " * " + VERTEX_INPUT + ";\n",
                null);

        EYE_VERTEX_CODE = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{VERTEX_INPUT, VARYING_EYE_VERTEX_OUTPUT, MVP_UNIFORM}),
                VARYING_EYE_VERTEX_OUTPUT + " = " + MVP_UNIFORM + " * " + VERTEX_INPUT + ";\n",
                null
        );

        TEXTURE_COORD_CODE_ADDON = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{TEXCOORD_INPUT, VARYING_TEXCOORD_OUTPUT}),
                VARYING_TEXCOORD_OUTPUT + " = " + TEXCOORD_INPUT +";\n",
                null
        );
    }
}
