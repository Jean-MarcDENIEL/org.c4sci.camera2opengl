package org.c4sci.camera2opengl.glTools.renderables.shaders;

import org.c4sci.camera2opengl.RenderingRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a well formed shader that is obtained by concatenating shader codes.
 */
public final class AssembledShader {
    private String completeCode;
    private List<ShaderVariable> allVariables;

    private static final String SHADER_HEADER;
    private static final String SHADER_PRECISION;
    private static final String MAIN_BEGINNING;
    private static final String MAIN_END;

    static{
        SHADER_HEADER =    "#version 310 es\n";
        SHADER_PRECISION = "precision lowp float" + ShaderCodeSnippet.EOL;
        MAIN_BEGINNING =
                "void main(void)\n"+
                        "   {\n";
        MAIN_END =
                "}\n";
    }

    public AssembledShader(String complete_code, List<ShaderVariable> all_variables) {
        this.completeCode = complete_code;
        this.allVariables = all_variables;
    }

    public String getCompleteCode() {
        return completeCode;
    }

    public List<ShaderVariable> getAllVariables() {
        return allVariables;
    }

    /**
     * Assemble several shader codes in one well formed. Shaders are applied in the list order.<br>
     * <b>Does not verify "binding aliasing".</b>
     *
     * @param shader_snippets The shader codes to assemble.
     * @return A well formed and ready to compile and link shader.
     * @throw RenderingRuntimeException if bindings in code are incohrent.
     */
    public static AssembledShader assembleShaders(List<ShaderCodeSnippet> shader_snippets) {
        //
        // First ensure that variables and uniforms are coherent between code snippets.
        Map<String, ShaderVariable> _name_to_variable = new ConcurrentHashMap<>();
        for (ShaderCodeSnippet _snippet : shader_snippets) {
            for (ShaderVariable _var : _snippet.getShaderVariables()) {
                if (_name_to_variable.containsKey(_var.getName())) {
                    ShaderVariable _already = _name_to_variable.get(_var.getName());
                    if ((_var.getStorageQualifier() != _already.getStorageQualifier()) ||
                            (!_var.getType().contentEquals(_already.getType())) ||
                            (_var.getBinding() != _already.getBinding())) {
                        throw new RenderingRuntimeException("Variable " + _var.getName() + " is incoherent among shader codes to assemble");
                    }
                } else {
                    _name_to_variable.put(_var.getName(), _var);
                }
            }
        }

        StringBuilder _res = new StringBuilder();
        _res.append(SHADER_HEADER);

        // Add all the header lines
        for (ShaderCodeSnippet _snippet: shader_snippets){
            List<String> _header_lines = _snippet.getHeaderLines();
            if (_header_lines != null){
                for (String _line : _header_lines){
                    _res.append(_line +"\n");
                }
            }
        }


        _res.append(SHADER_PRECISION);

        for (ShaderVariable _var : _name_to_variable.values()) {
            _res.append(
                    _var.getStorageQualifier() + "  " +
                            _var.getType() + " " +
                            _var.getName() + ShaderCodeSnippet.EOL);
        }

        _res.append(MAIN_BEGINNING);
        for (ShaderCodeSnippet _code : shader_snippets) {
            _res.append(_code.getCodeBody());
        }
        _res.append(MAIN_END);


        return new AssembledShader(_res.toString(), new ArrayList<>(_name_to_variable.values()));
    }
}
