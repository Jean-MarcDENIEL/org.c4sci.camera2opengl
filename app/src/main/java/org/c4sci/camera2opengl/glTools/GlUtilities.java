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
     * If the assertion condition is false,
     * <ol>
     *     <li>gets an investigation message from #error_investigator</li>
     *     <li>runs to_do_if_fail</li>
     *     <li>throws a {@link RenderingRuntimeException} showing the error message, the flags and the investigation result</li>
     * </ol>
     *
     * @param error_msg The message of the faulty operation
     * @param to_do_if_fail The action to do in case of failure (e.g. release resources). May be null.
     * @param failure_investigator The investigator giving complementary informations. Mey be null.
     * @throws RenderingRuntimeException is assert_ is false
     */
    public static void assertGles31Call(final boolean assert_, final String error_msg, final Runnable to_do_if_fail, final ErrorInvestigator failure_investigator){
        if (!assert_){
            StringBuilder _msg = new StringBuilder();
            _msg.append(error_msg);
            _msg.append("OpenGL Error flags:\n");
            appendOpenGlStateMachineErrors(_msg);
            if (failure_investigator != null) {
                _msg.append("\ndue to:\n" + failure_investigator.getExplanation());
            }
            if (to_do_if_fail != null){
                to_do_if_fail.run();
            }
            throw new RenderingRuntimeException(_msg.toString());
        }
    }

    /**
     * Runs {@link #assertGles31Call(boolean, String, Runnable, ErrorInvestigator)} without any investigator
     * @param assert_ Assertion condition
     * @param error_msg Error message
     * @param to_do_if_fail Action to do if assertion fails
     */
    public static void assertGles31Call(final boolean assert_, final String error_msg, final Runnable to_do_if_fail){
        assertGles31Call(assert_, error_msg, to_do_if_fail,  null);
    }

    /**
     * Runs {@link #assertGles31Call(boolean, String, Runnable, ErrorInvestigator)} without action nor investigator.
     * @param assert_ Assertion condition
     * @param error_msg Error message.
     * @throws
     */
    public static void assertGles31Call(final boolean assert_, final String error_msg){
        assertGles31Call(assert_, error_msg, null,  null);
    }

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
        StringBuilder _msg = new StringBuilder();
        if (appendOpenGlStateMachineErrors(_msg)) {
            if (error_investigator != null){
                _msg.append(" due to :\n" + error_investigator.getExplanation());
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

    private static boolean appendOpenGlStateMachineErrors(StringBuilder msg_){
        boolean _gl_error = false;
        int _error_code;
        while ((_error_code = GLES31.glGetError()) != GLES31.GL_NO_ERROR){
            _gl_error = true;
            msg_.append(GLES31_ERROR_CODES.get(_error_code)+"\n");
        }
        return _gl_error;
    }

}
