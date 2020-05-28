package org.c4sci.camera2opengl.glTools.renderables.shaders.stock;

import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderAttributes;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderCodeSnippet;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderVariable;

import java.util.Arrays;

public class StockFragmentShaderSnippets {

    /* ********************************* VARIABLES / ATTRIBUTES ********************************* */
    public static final ShaderVariable INPUT_VARYING_COLOR;
    public static final ShaderVariable OUTPUT_FRAGMENT_COLOR;
    public static final ShaderVariable INPUT_VARYING_EYE_VERTEX;
    public static final ShaderVariable INPUT_VARYING_TEX_COORD;
    public static final ShaderVariable UNIFORM_TEXTURE0_ID;
    public static final ShaderVariable UNIFORM_PREVIEW_TEXTURE0_ID;

    /**
     * something like "uniform vec4 v4Ambient" :
     * <ol>
     *     <li> x = Scale factor  [0,1]</li>
     *     <li> y = "Near" dist to the eye [0 INF]</li>
     *     <li> z = Min ambient factor [0,1]</li>
     *     <li> w = 1/dist attenuation power [0, INF] </li>
     * </ol>
     */
    public static final ShaderVariable LIGHT_AMBIENT_UNIFORM;
    /**
     * something like "uniform mat4 m4Directional"
     * <ol>
     *     <li>m4Directional[0]: .x=R[0,1] .y=G[0,1] .z=B[0,1] .w=Attentuation power [0, INF] of distance to the eye</li>
     *     <li>m4Directional[1].xyz = lighting direction</li>
     * </ol>
     */
    public static final ShaderVariable LIGH_DIRECTIONAL_UNIFORM;

    /* ************************************* CODE SNIPPETS ************************************** */

    /**
     * This basic shader interpolates {@link #OUTPUT_FRAGMENT_COLOR} between vertices {@link #INPUT_VARYING_COLOR}
     */
    public static final ShaderCodeSnippet IDENTITY_FRAGMENT_CODE;

    /**
     * This codes multiplies {@link #OUTPUT_FRAGMENT_COLOR} by an ambient that decreases with distance to the eye.<br>
     * It is parametrized by {@link #LIGHT_AMBIENT_UNIFORM}.
     */
    public static final ShaderCodeSnippet AMBIENT_LIGHT_MUL_CODE_ADDON;

    /**
     * This code multiplies {@link #OUTPUT_FRAGMENT_COLOR}.rgb by {@link #INPUT_VARYING_TEX_COORD}
     */
    public static final ShaderCodeSnippet PREVIEW_TEXTURE_RGB_SET_ADDON;

    static{

        /* ************* VARIABLES ********************* */
        INPUT_VARYING_COLOR = new ShaderVariable(
                StockVertexShaderSnippets.VARYING_COLOR_OUTPUT.getName(),
                ShaderVariable.StorageQualifier.INPUT,
                StockVertexShaderSnippets.VARYING_COLOR_OUTPUT.getType(),
                ShaderVariable.UNBOUND_VARIABLE);

        OUTPUT_FRAGMENT_COLOR = new ShaderVariable(
                "v4FragColor",
                ShaderVariable.StorageQualifier.OUTPUT,
                ShaderVariable.VEC_4,
                ShaderVariable.UNBOUND_VARIABLE);

        INPUT_VARYING_EYE_VERTEX = new ShaderVariable(
                StockVertexShaderSnippets.VARYING_EYE_VERTEX_OUTPUT.getName(),
                ShaderVariable.StorageQualifier.INPUT,
                StockVertexShaderSnippets.VARYING_EYE_VERTEX_OUTPUT.getType(),
                ShaderVariable.UNBOUND_VARIABLE
        );


        LIGHT_AMBIENT_UNIFORM = new ShaderVariable(ShaderAttributes.AMBIENT.toString(),
                ShaderVariable.StorageQualifier.UNIFORM,
                ShaderVariable.VEC_4, ShaderVariable.UNBOUND_VARIABLE);

        LIGH_DIRECTIONAL_UNIFORM = new ShaderVariable(ShaderAttributes.DIRECTIONAL.toString(),
                ShaderVariable.StorageQualifier.UNIFORM,
                ShaderVariable.MAT_4, ShaderVariable.UNBOUND_VARIABLE);

        INPUT_VARYING_TEX_COORD = new ShaderVariable(
                StockVertexShaderSnippets.VARYING_TEXCOORD_OUTPUT.getName(),
                ShaderVariable.StorageQualifier.INPUT,
                StockVertexShaderSnippets.VARYING_TEXCOORD_OUTPUT.getType(),
                ShaderVariable.UNBOUND_VARIABLE);

        UNIFORM_TEXTURE0_ID = new ShaderVariable(
                ShaderAttributes.TEXTURE0.toString(),
                ShaderVariable.StorageQualifier.UNIFORM,
                ShaderVariable.SAMPLER_2D,
                ShaderVariable.UNBOUND_VARIABLE);

        UNIFORM_PREVIEW_TEXTURE0_ID = new ShaderVariable(
                ShaderAttributes.TEXTURE0.toString(),
                ShaderVariable.StorageQualifier.UNIFORM,
                ShaderVariable.SAMPLER_EXT_OES,
                ShaderVariable.UNBOUND_VARIABLE);

        /* *********************************** CODE SNIPPETS ************************************ */

        IDENTITY_FRAGMENT_CODE = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{INPUT_VARYING_COLOR, OUTPUT_FRAGMENT_COLOR}),
                OUTPUT_FRAGMENT_COLOR + " = " + INPUT_VARYING_COLOR+";\n");

        AMBIENT_LIGHT_MUL_CODE_ADDON = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{LIGHT_AMBIENT_UNIFORM, OUTPUT_FRAGMENT_COLOR, INPUT_VARYING_EYE_VERTEX}),
                "float _eye_dist = max(1.0, length("+ INPUT_VARYING_EYE_VERTEX + ") - " + LIGHT_AMBIENT_UNIFORM+ ".y);\n"+
                        "float _att = clamp(pow(1.0/_eye_dist," + LIGHT_AMBIENT_UNIFORM + ".w), "  + LIGHT_AMBIENT_UNIFORM+ ".z, 1.0);\n" +
                        OUTPUT_FRAGMENT_COLOR + ".rgb *=" + LIGHT_AMBIENT_UNIFORM + ".x * _att;\n");

        PREVIEW_TEXTURE_RGB_SET_ADDON = new ShaderCodeSnippet(
                Arrays.asList(new ShaderVariable[]{INPUT_VARYING_TEX_COORD, OUTPUT_FRAGMENT_COLOR, UNIFORM_PREVIEW_TEXTURE0_ID}),
                Arrays.asList("#extension GL_OES_EGL_image_external : require"),
                OUTPUT_FRAGMENT_COLOR +" = texture("+UNIFORM_TEXTURE0_ID+"," + INPUT_VARYING_TEX_COORD + ".st);\n",
                null);
    }
}
