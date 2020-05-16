package org.c4sci.camera2opengl.glTools;


import android.opengl.GLES31;
import android.util.SparseArray;

import org.c4sci.camera2opengl.RenderingRuntimeException;

public final class GlUtilities {

    static final SparseArray<String> GLES31_ERROR_CODES = new SparseArray<>();
    static{
        GLES31_ERROR_CODES.put(GLES31.GL_INVALID_ENUM, "Invalid enum");
        GLES31_ERROR_CODES.put(GLES31.GL_INVALID_VALUE, "Invalid value");
        GLES31_ERROR_CODES.put(GLES31.GL_INVALID_OPERATION, "Invalid operation");
        GLES31_ERROR_CODES.put(GLES31.GL_OUT_OF_MEMORY, "Out of memory");
    }

    public interface ErrorInvestigator {
        String getExplanation();
    };

    /**
     * If the OpenGL state machine is in error,
     * <ol>
     *     <li>gets an investigation message from #error_investigator</li>
     *     <li>runs to_do_if_fail</li>
     *     <li>throws a {@link RenderingRuntimeException} showing the error message, the flags and the investigation result</li>
     * </ol>
     *
     * @param gl_operation The name of the faulty operation
     * @param to_do_if_fail The action to do in case of failure (e.g. release resources)
     */
    public static final void ensureGles31Call(String gl_operation, Runnable to_do_if_fail, ErrorInvestigator error_investigator){
        boolean _gl_error = false;
        int _error_code;
        StringBuilder _msg = new StringBuilder();
        while ((_error_code = GLES31.glGetError()) != GLES31.GL_NO_ERROR){
            _gl_error = true;
            _msg.append(GLES31_ERROR_CODES.get(_error_code)+"\n");
        }
        if (_gl_error) {
            if (error_investigator != null){
                _msg.append(error_investigator.getExplanation());
            }
            if (to_do_if_fail != null) {
                to_do_if_fail.run();
            }
            throw new RenderingRuntimeException(gl_operation + "failed :\n" + _msg.toString());
        }
    }

    /**
     * If the OpenGL state machine is in error,
     * <ol>
     *     <li>runs to_do_if_fail</li>
     *     <li>throws a {@link RenderingRuntimeException} showing the error message and flags</li>
     * </ol>
     *
     * @param gl_operation The name of the faulty operation
     * @param to_do_if_fail The action to do in case of failure (e.g. release resources)
     */
    public static final void ensureGles31Call(String gl_operation, Runnable to_do_if_fail){
        ensureGles31Call(gl_operation, to_do_if_fail, null);
    }

    /**
     * If the OpenGL state machine is in error, throws a {@link RenderingRuntimeException} showing the error message and flags.
     * @param gl_operation The name of the faulty operation
     */
    public static final void ensureGles31Call(String gl_operation){
        ensureGles31Call(gl_operation, null);
    }

}
