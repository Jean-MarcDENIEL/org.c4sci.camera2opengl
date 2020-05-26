package org.c4sci.camera2opengl.glTools.renderables.shaders;

/**
 * These variables can be used as is in code snippets due to {@link #toString()} override.
 */
public class ShaderVariable {

    public static final int UNBOUND_VARIABLE = -1;
    public static final String VEC_4 = "vec4";
    public static final String MAT_4 = "mat4";

    public enum StorageQualifier{
        NONE("", false, false),
        CONSTANT("const", false, false),
        INPUT("in", true, false),
        INPUT_CENTROID("in centroid", true, false),
        OUTPUT("out",false,true),
        OUTPUT_CENTROID("output centroid", false, true),
        INPUT_OUTPUT("inout", true, true),
        UNIFORM("uniform", false, false),
        INPUT_FLAT("flat in", true, false),
        OUTPUT_FLAT("flat out", false, true),
        INPUT_OUTPUT_FLAT("flat inout", true, true);

        private String storageQualifier;
        private boolean isInput;
        private boolean isOutput;
        StorageQualifier(String storage_qualifier,
                         boolean is_input, boolean is_output){
            storageQualifier = storage_qualifier;
            isInput = is_input;
            isOutput = is_output;
        }
        @Override
        public String toString(){
            return storageQualifier;
        }
        public boolean isAnOutput(){ return isOutput; }
        public boolean isAnInput(){ return isInput; }
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

    public String getName() {
        return variableName;
    }

    public StorageQualifier getStorageQualifier() {
        return variableQualifier;
    }

    public String getType() {
        return variableType;
    }

    public int getBinding(){ return variableBinding;}

    @Override
    public String toString(){
        return getName();
    }

}
