package org.c4sci.camera2opengl.glTools.renderables.shaders;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShaderCodeSnippet {
    private List<ShaderVariable> shaderVariables;
    private String codeBody;
    private List<String> codeFunctions;

    public static final String EOL = ";\n";

    /**
     * Declares a portion of shader code.
     * @param shader_variables The input and output variables. May be null.
     * @param code_body The code body that lies inside "void main(void) { .... }" exploiting the variables. May be null.
     * @param code_functions The functions used by the body code.
     */
    public ShaderCodeSnippet(List<ShaderVariable> shader_variables, String code_body, List<String> code_functions) {
        this.shaderVariables = shader_variables;
        this.codeBody = code_body;
        this.codeFunctions = code_functions;
    }

    public List<ShaderVariable> getShaderVariables() {
        return shaderVariables;
    }

    public String getCodeBody() {
        return codeBody;
    }

    public List<String> getCodeFunctions() {
        return codeFunctions;
    }
}
