package org.c4sci.camera2opengl.glTools.renderables.shaders;

/**
 * Variables used by stock shaders: their attribute index is Ordinal(). You can safely mix stock shaders
 * with your own as long as you respect this convention. For example your owns attributes indices should start
 * at this enum values().length and should be different to these stock attribute names.<br>
 * This class allows passing from one shader to another without rebinding VBOs, as long as the shaders respect this
 * convention and use the same bindings for other variables.
 */
public enum ShaderAttributes {
    VERTEX("v4Vertex"),
    COLOR("v4Color"),
    NORMAL("v4Normal"),
    TEXTURE0("vTextureO"),
    TEXTURE1("vTexture1"),
    TEXTURE2("vTexture2"),
    TEXTURE3("vTexture3"),
    MVP("m4Mvp"),
    AMBIENT("v4Ambient"),
    DIRECTIONAL("m4Directional"),
    EYEVERTEX("v4EyeVertex");

    private String attributeVariable;

    @Override
    public String toString(){
        return attributeVariable;
    }

    ShaderAttributes(String attribute_variable){
        attributeVariable = attribute_variable;
    }
};
