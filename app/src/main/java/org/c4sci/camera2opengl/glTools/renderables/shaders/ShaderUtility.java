package org.c4sci.camera2opengl.glTools.renderables.shaders;

import android.opengl.GLES31;
import android.util.Pair;

import org.c4sci.camera2opengl.RenderingRuntimeException;
import org.c4sci.camera2opengl.glTools.GlUtilities;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is intended at furnishing basic shaders in order to make rendering easier
 */
public class ShaderUtility {

//    public static final String COLOR_SHADER_VERTEX_CODE =
//            "#version 310 es\n" +
//                    "//Identity shader : simply apply coordinates and color\n" +
//                    "in vec4 vVertex;\n" +
//                    "uniform vec4 vColor;\n" +
//                    "out vec4 vVaryingColor; // Color value passed to fragment shader\n" +
//                    "void main(void)\n" +
//                    "   {\n" +
//                    "   vVaryingColor = vColor;\n" +
//                    "   gl_Position = vVertex;\n" +
//                    "   }";
//
//    public static final String IDENTITY_SHADER_VERTEX_CODE =
//            "#version 310 es\n" +
//            "//Identity shader : simply apply coordinates and color\n" +
//           "in vec4 vVertex;\n" +
//            "in vec4 vColor;\n" +
//            "out vec4 vVaryingColor; // Color value passed to fragment shader\n" +
//            "void main(void)\n" +
//            "   {\n" +
//            "   vVaryingColor = vColor;\n" +
//            "   gl_Position = vVertex;\n" +
//            "   }";
//
//    public static final String IDENTITY_SHADER_FRAGMENT_CODE =
//            "#version 310 es\n" +
//            "// Identity shader : simply pass the color to rasterize\n" +
//            "precision lowp float;\n"+
//            "out vec4 vFragColor; // Color to rasterize\n" +
//            "in vec4 vVaryingColor; // incoming color from vertex stage\n" +
//            "void main(void)\n" +
//            "   {\n" +
//            "       vFragColor = vVaryingColor;\n" +
//            "   }";


//
//
//    public static final List<Pair<Integer, String>> IDENTITY_SHADER_ATTRIBUTES = new ArrayList<>();
//    static{
//        for (ShaderAttributes _attrib : ShaderAttributes.values()){
//            IDENTITY_SHADER_ATTRIBUTES.add(new Pair<>(_attrib.ordinal(), _attrib.toString()));
//        }
//    }

    /**
     * Creates and binds a program with the shaders code passed as parameters, or raise un
     * unchecked {@link org.c4sci.camera2opengl.RenderingRuntimeException}.
     * @param vertex_code The well formed vertex shader
     * @param fragment_code The code of the fragment shader
     * @return The program handler is everything went right.
     */
    public static int makeProgramFromShaders(final AssembledShader vertex_code, final AssembledShader fragment_code){
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

        // Verifies that every fragment shader input corresponds to a vertex shader output of the same name and the same type.
        for (ShaderVariable _frag_var : fragment_code.getAllVariables()){
            if (_frag_var.getStorageQualifier().isAnInput()) {
                boolean _found = false;
                for (ShaderVariable _vertex_var : vertex_code.getAllVariables()) {
                    if (_frag_var.getName().contentEquals(_vertex_var.getName())) {
                        if (!_vertex_var.getStorageQualifier().isAnOutput()){
                            throw new RenderingRuntimeException("Cannot link vertex to fragment shader: " +
                                    _vertex_var.getName() + " is not an output from Vertex shader");
                        }
                        else{
                            if (_vertex_var.getBinding() != _frag_var.getBinding()){
                                throw new RenderingRuntimeException("Cannot link vertex to fragment shader: " + _vertex_var.getName() +
                                        " has different bindings: " + _vertex_var.getBinding() + " but " + _frag_var.getBinding());
                            }
                            if (!_vertex_var.getType().contentEquals(_frag_var.getType())){
                                throw new RenderingRuntimeException("Cannot link vertex to fragment shader: " + _vertex_var.getName() +
                                        " has different types: " + _vertex_var.getType() +" but " + _frag_var.getType());
                            }
                            _found = true;
                        }
                    }
                }
                if (!_found){
                    throw new RenderingRuntimeException("Cannot link vertex to fragment shader: " + _frag_var.getName()+
                            " has no corresponding vertex shader output");
                }
            }
        }

        // Loads the source codes
        GLES31.glShaderSource(_vertex_shader, vertex_code.getCompleteCode());
        GlUtilities.ensureGles31Call("glShaderSource(_vertex_shader, vertex_code)", ()-> GLES31.glDeleteShader(_vertex_shader));
        GLES31.glShaderSource(_fragment_shader, fragment_code.getCompleteCode());
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
                () -> { return GLES31.glGetShaderInfoLog(_vertex_shader) +
                        "\n VERTEX SHADER CODE : \n" + vertex_code.getCompleteCode(); });

        GLES31.glCompileShader(_fragment_shader);
        GLES31.glGetShaderiv(_fragment_shader, GLES31.GL_COMPILE_STATUS, _shader_compile_state);
        GlUtilities.assertGles31Call(
                _shader_compile_state.get(0) == GLES31.GL_TRUE,
                "glCompileShader(_fragment_shader)",
                _shaders_deleter,
                () -> { return GLES31.glGetShaderInfoLog(_fragment_shader)+
                        "\n FRAGMENT SHADER CODE : \n" + fragment_code.getCompleteCode(); });

        // Create the final program and attach the shaders
        int _program = GLES31.glCreateProgram();
        GlUtilities.assertGles31Call(_program !=0, "Can't create program");
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
        for (ShaderVariable _vertex_var : vertex_code.getAllVariables()){
            if (_vertex_var.getStorageQualifier().isAnInput() && (_vertex_var.getBinding() != -1)){
                GLES31.glBindAttribLocation(_program, _vertex_var.getBinding(), _vertex_var.getName());
                GlUtilities.ensureGles31Call("glBindAttribLocation() vertex shader with " + _vertex_var.getBinding() + " " + _vertex_var.getName(), _total_deleter);
            }
        }

        for (ShaderVariable _fragment_var : fragment_code.getAllVariables()){
            if (_fragment_var.getStorageQualifier().isAnInput() && (_fragment_var.getBinding() != -1)){
                GLES31.glBindAttribLocation(_program, _fragment_var.getBinding(), _fragment_var.getName());
                GlUtilities.ensureGles31Call("glBindAttribLocation() fragment shader with " + _fragment_var.getBinding() + " " + _fragment_var.getName(), _total_deleter);
            }
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
