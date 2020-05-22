package org.c4sci.camera2opengl.glTools.renderables.shaders.stock;

import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderCode;
import org.c4sci.camera2opengl.glTools.renderables.shaders.ShaderVariable;

import java.util.Arrays;

public class StockFragmentShaders {

    public static final ShaderVariable INPUT_VARYING_COLOR;
    public static final ShaderVariable OUTPUT_FRAGMENT_COLOR;

    public static final ShaderCode IDENTITY_FRAGMENT_CODE;

    static{
        INPUT_VARYING_COLOR = new ShaderVariable(
                StockVertexShaders.VARYING_COLOR_OUTPUT.getVariableName(),
                ShaderVariable.StorageQualifier.INPUT,
                StockVertexShaders.VARYING_COLOR_OUTPUT.getVariableType(),
                ShaderVariable.UNBOUND_VARIABLE);

        OUTPUT_FRAGMENT_COLOR = new ShaderVariable(
                "VFragColor",
                ShaderVariable.StorageQualifier.OUTPUT,
                ShaderVariable.VEC_4,
                ShaderVariable.UNBOUND_VARIABLE);

        IDENTITY_FRAGMENT_CODE = new ShaderCode(
                Arrays.asList(new ShaderVariable[]{INPUT_VARYING_COLOR, OUTPUT_FRAGMENT_COLOR}),
                OUTPUT_FRAGMENT_COLOR.getVariableName() + " = " + INPUT_VARYING_COLOR.getVariableName()+";\n",
                null);

    }
}
