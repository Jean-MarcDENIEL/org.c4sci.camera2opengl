package org.c4sci.camera2opengl.glTools;

import android.opengl.GLES31;
import android.util.Pair;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is intended at furnishing basic shaders in order to make rendering easier
 */
public class ShaderUtility {

    public static final String IDENTITY_SHADER_VERTEX_CODE =
            "#version 310 es\n" +
            "//Identity shader : simply apply coordinates and color\n" +
           "in vec4 vVertex;\n" +
            "in vec4 vColor;\n" +
            "out vec4 vVaryingColor; // Color value passed to fragment shader\n" +
            "void main(void)\n" +
            "   {\n" +
            "   vVaryingColor = vColor;\n" +
            "   gl_Position = vVertex;\n" +
            "   }";

    public static final String IDENTITY_SHADER_FRAGMENT_CODE =
            "#version 310 es\n" +
            "// Identity shader : simply pass the color to rasterize\n" +
            "precision lowp float;\n"+
            "out vec4 vFragColor; // Color to rasterize\n" +
            "in vec4 vVaryingColor; // incoming color from vertex stage\n" +
            "void main(void)\n" +
            "   {\n" +
            "       vFragColor = vVaryingColor;\n" +
            "   }";


    /**
     * Variables used by stock shaders: their attribute index is Ordinal(). You can safely mix stock shaders
     * with your own as long as you respect this convention. For example your owns attributes indices should start
     * at this enum values().length and should be different to these stock attribute names.
     */
    public enum ShaderAttributes {
        VERTEX("vVertex"),
        COLOR("vColor"),
        NORMAL("vNormal"),
        TEXTURE0("vTextureO"),
        TEXTURE1("vTexture1"),
        TEXTURE2("vTexture2"),
        TEXTURE3("vTexture3");

        private String attributeVariable;

        public String variableName(){
            return attributeVariable;
        }

        ShaderAttributes(String attribute_variable){
            attributeVariable = attribute_variable;
        }
    };

    public static final List<Pair<Integer, String>> IDENTITY_SHADER_ATTRIBUTES = new ArrayList<>();
    static{
        for (ShaderAttributes _attrib : ShaderAttributes.values()){
            IDENTITY_SHADER_ATTRIBUTES.add(new Pair<>(_attrib.ordinal(), _attrib.variableName()));
        }
    }

    /**
     * Creates and binds a program with the shaders code passed as parameters, or raise un
     * unchecked {@link org.c4sci.camera2opengl.RenderingRuntimeException}.
     * @param vertex_code The code of the vertex shader
     * @param fragment_code The code of the fragment shader
     * @param attributes_indices_names The attributes bindings (indices and names)
     * @return The program handler is everything went right.
     */
    public static int loadVertexAndFragmentShaders(final String vertex_code, final String fragment_code,
                                                   List<Pair<Integer, String>> attributes_indices_names){
        // Create shader objects
        int _vertex_shader = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
        GlUtilities.ensureGles31Call("glCreateShader(GLES31.GL_VERTEX_SHADER)");
        GlUtilities.assertGles31Call(_vertex_shader != 0, "glCreateShader(GLES31.GL_VERTEX_SHADER)");
        int _fragment_shader = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
        GlUtilities.ensureGles31Call("glCreateShader(GLES31.GL_FRAGMENT_SHADER)");
        GlUtilities.assertGles31Call(_fragment_shader != 0, "glCreateShader(GLES31.GL_FRAGMENT_SHADER)");

        Runnable _shaders_deleter = () -> {
            GLES31.glDeleteShader(_vertex_shader);
            GLES31.glDeleteShader(_fragment_shader);
        };

        // Loads the source codes
        GLES31.glShaderSource(_vertex_shader, vertex_code);
        GlUtilities.ensureGles31Call("glShaderSource(_vertex_shader, vertex_code)", ()-> GLES31.glDeleteShader(_vertex_shader));
        GLES31.glShaderSource(_fragment_shader, fragment_code);
        GlUtilities.ensureGles31Call("glShaderSource(_fragment_shader, fragment_code)",
                _shaders_deleter);

        // Compile the source codes
        GLES31.glCompileShader(_vertex_shader);
        IntBuffer _shader_compile_state = IntBuffer.allocate(1);
        GLES31.glGetShaderiv(_vertex_shader, GLES31.GL_COMPILE_STATUS, _shader_compile_state);
        GlUtilities.assertGles31Call(
                _shader_compile_state.get(0) == GLES31.GL_TRUE,
                "glCompileShader(_vertex_shader)",
                _shaders_deleter,
                () -> { return GLES31.glGetShaderInfoLog(_vertex_shader); });

        GLES31.glCompileShader(_fragment_shader);
        GLES31.glGetShaderiv(_fragment_shader, GLES31.GL_COMPILE_STATUS, _shader_compile_state);
        GlUtilities.assertGles31Call(
                _shader_compile_state.get(0) == GLES31.GL_TRUE,
                "glCompileShader(_fragment_shader)",
                _shaders_deleter,
                () -> { return GLES31.glGetShaderInfoLog(_fragment_shader); });

        // Create the final program and attach the shaders
        int _program = GLES31.glCreateProgram();
        GlUtilities.assertGles31Call(_program !=0,
                "Can't create program");
        GlUtilities.ensureGles31Call("glCreateProgram()", _shaders_deleter);

        Runnable _total_deleter = ()-> {
            _shaders_deleter.run();
            GLES31.glDeleteProgram(_program);
        };

        GLES31.glAttachShader(_program, _vertex_shader);
        GlUtilities.ensureGles31Call("glAttachShader(_program, _vertex_shader)", _total_deleter);
        GLES31.glAttachShader(_program, _fragment_shader);
        GlUtilities.ensureGles31Call("glAttachShader(_program, _fragment_shader)", _total_deleter);

        // Bind attributes names to their location
        for (Pair<Integer, String> _attribute : attributes_indices_names){
            GLES31.glBindAttribLocation(_program, _attribute.first, _attribute.second);
            GlUtilities.ensureGles31Call("glBindAttribLocation() with " + _attribute.first + " " + _attribute.second, _total_deleter);
        }

        // Attemps to link program to device
        GLES31.glLinkProgram(_program);
        IntBuffer _program_state = IntBuffer.allocate(1);
        GLES31.glGetProgramiv(_program, GLES31.GL_LINK_STATUS, _program_state);
        GlUtilities.assertGles31Call(_program_state.get(0) == GLES31.GL_TRUE, "glLinkProgram(_program)",
                _total_deleter, () ->{return GLES31.glGetProgramInfoLog(_program);});
        GlUtilities.ensureGles31Call("glLinkProgram(_program)", _total_deleter,
                () ->{ return GLES31.glGetProgramInfoLog(_program);});

        // Delete shaders as they are not useful anymore
        _shaders_deleter.run();

        return _program;

    }
}
