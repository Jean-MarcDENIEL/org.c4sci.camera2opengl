package org.c4sci.camera2opengl.glTools;

import android.opengl.GLES31;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is intended at furnishing basic shaders in order to make rendering easier
 */
public class ShaderUtility {

    public static final String IDENTITY_SHADER_VERTEX_CODE =
            "//Identity shader : simply apply coordinates and color\n" +
            "#version 310 // GLSL 3.1 = OpenGL ES 3.1\n" +
            "in vec4 vVertex;\n" +
            "in vec4 vColor;\n" +
            "out vec4 vVaryingColor; // Color value passed to fragment shader\n" +
            "void main(void)\n" +
            "   {\n" +
            "   vVaryingColor = vColor;\n" +
            "   gl_Position = vVertex;\n" +
            "   }";

    public static final String IDENTITY_SHADER_FRAGMENT_CODE =
            "// Identity shader : simply pass the color to rasterize\n" +
                    "#version 310 //GLSL 3.10 = OpenGL ES 3.1\n" +
                    "out vec4 vFragColor; // Color to rasterize\n" +
                    "in vec4 vVaryingColor; // incoming color from vertex stage\n" +
                    "void main(void)\n" +
                    "   {\n" +
                    "   vFragColor = vVaryingColor;\n" +
                    "   }";


    /**
     * Indices of attributes used by stock shaders (use Ordinal()). You can mix stock shaders
     * with your own as long as you respect this convention.
     */
    public enum ShaderAttributeIndices {
        ATTRIBUTE_VERTEX,
        ATTRIBUTE_COLOR,
        ATTRIBUTE_NORMAL,
        ATTRIBUTE_TEXTURE0,
        ATTRIBUTE_TEXTURE1,
        ATTRIBUTE_TEXTURE2,
        ATTRIBUTE_TEXTURE3,
        ATTRIBUTE_LAST
    };

    public static final List<Pair<Integer, String>> IDENTITY_SHADER_ATTRIBUTES = new ArrayList<>();
    static{
        IDENTITY_SHADER_ATTRIBUTES.add(new Pair<>(ShaderAttributeIndices.ATTRIBUTE_VERTEX.ordinal(), "vVertex"));
        IDENTITY_SHADER_ATTRIBUTES.add(new Pair<>(ShaderAttributeIndices.ATTRIBUTE_COLOR.ordinal(), "vColor"));
        IDENTITY_SHADER_ATTRIBUTES.add(new Pair<>(ShaderAttributeIndices.ATTRIBUTE_COLOR.ordinal(), "vVaryingColor"));
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
        int _fragment_shader = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
        GlUtilities.ensureGles31Call("glCreateShader(GLES31.GL_FRAGMENT_SHADER)");

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
        GlUtilities.ensureGles31Call("glCompileShader(_vertex_shader)",
                _shaders_deleter,
                () -> { return GLES31.glGetShaderInfoLog(_vertex_shader); });
        GLES31.glCompileShader(_fragment_shader);
        GlUtilities.ensureGles31Call("glCompileShader(_fragment_shader)",
                _shaders_deleter,
                () -> { return GLES31.glGetShaderInfoLog(_vertex_shader); });

        // Create the final program and attach the shaders
        int _program = GLES31.glCreateProgram();
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
            GlUtilities.ensureGles31Call("glCreateProgram()", _total_deleter);
        }

        // Attemps to link program to device
        GLES31.glLinkProgram(_program);
        GlUtilities.ensureGles31Call("glLinkProgram(_program)", _total_deleter,
                () ->{ return GLES31.glGetProgramInfoLog(_program);});

        // Delete shaders as they are not useful anymore
        _shaders_deleter.run();

        return _program;

    }
}
