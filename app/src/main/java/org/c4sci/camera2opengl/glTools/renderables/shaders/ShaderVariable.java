package org.c4sci.camera2opengl.glTools.renderables.shaders;

public class ShaderVariable {

    public enum StorageQualifier{
        NONE(""),
        CONSTANT("const"),
        INPUT("in"),
        INPUT_CENTROID("in centroid"),
        OUTPUT("out"),
        OUTPUT_CENTROID("output centroid"),
        INPUT_OUTPUT("inout"),
        UNIFORM("uniform");

        private String storageQualifier;
        StorageQualifier(String storage_qualifier){
            storageQualifier = storage_qualifier;
        }
        @Override
        public String toString(){
            return storageQualifier;
        }
    }

    private String variableName;
    private StorageQualifier variableQualifier;
    private String variableType;
    private int variableBinding;

    /**
     *
     * @param variable_name The name of the variable. E.g vColor
     * @param variable_storage_qualifier Storage qualifier among {@link StorageQualifier} (remember only one {@link StorageQualifier#INPUT_OUTPUT} is allowed)
     * @param variable_type The type. E.g "float" or "vec4" ...
     * @param variable_binding Allows passing from one shader to another without re-binding buffers to the newly used shader. Use -1 for no binding, use
     */
    public ShaderVariable(String variable_name, StorageQualifier variable_storage_qualifier, String variable_type, int variable_binding) {
        this.variableName = variable_name;
        this.variableQualifier = variable_storage_qualifier;
        this.variableType = variable_type;
        this.variableBinding = variable_binding;
    }

    public String getVariableName() {
        return variableName;
    }

    public StorageQualifier getStorageQualifier() {
        return variableQualifier;
    }

    public String getVariableType() {
        return variableType;
    }
}
