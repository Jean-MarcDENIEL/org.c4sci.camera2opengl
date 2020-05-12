package org.c4sci.camera2opengl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.opengl.EGL14;

public class RenderingRuntimeException extends RuntimeException {

    static private final Map<Integer, String> elg14ErrorToStringMap = new ConcurrentHashMap<>();
    static {
        int[] _egl_errors = new int[]{
                EGL14.EGL_SUCCESS,
                EGL14.EGL_NOT_INITIALIZED,
                EGL14.EGL_BAD_ACCESS,
                EGL14.EGL_BAD_ALLOC,
                EGL14.EGL_BAD_ATTRIBUTE,
                EGL14.EGL_BAD_CONFIG,
                EGL14.EGL_BAD_CONTEXT,
                EGL14.EGL_BAD_CURRENT_SURFACE,
                EGL14.EGL_BAD_DISPLAY,
                EGL14.EGL_BAD_MATCH,
                EGL14.EGL_BAD_NATIVE_PIXMAP,
                EGL14.EGL_BAD_NATIVE_WINDOW,
                EGL14.EGL_BAD_PARAMETER,
                EGL14.EGL_BAD_SURFACE,
                EGL14.EGL_CONTEXT_LOST
        };
        String[] _egl_msgs = new String[]{
                "EGL14.EGL_SUCCESS",
                "EGL14.EGL_NOT_INITIALIZED",
                "EGL14.EGL_BAD_ACCESS",
                "EGL14.EGL_BAD_ALLOC",
                "EGL14.EGL_BAD_ATTRIBUTE",
                "EGL14.EGL_BAD_CONFIG",
                "EGL14.EGL_BAD_CONTEXT",
                "EGL14.EGL_BAD_CURRENT_SURFACE",
                "EGL14.EGL_BAD_DISPLAY",
                "EGL14.EGL_BAD_MATCH",
                "EGL14.EGL_BAD_NATIVE_PIXMAP",
                "EGL14.EGL_BAD_NATIVE_WINDOW",
                "EGL14.EGL_BAD_PARAMETER",
                "EGL14.EGL_BAD_SURFACE",
                "EGL14.EGL_CONTEXT_LOST"
        };
        for (int _i=0; _i<_egl_errors.length; _i++){
            elg14ErrorToStringMap.put(_egl_errors[_i], _egl_msgs[_i]);
        }
    }

    public static String convertThreadTrace(Thread thread_){
        StringBuilder _trace = new StringBuilder();
        for (StackTraceElement _elt : thread_.getStackTrace()){
            _trace.append("    " + _elt.getMethodName() + " @ " + _elt.getLineNumber()+"\n");
        }
        return _trace.toString();
    }

    public static String translateEgl14Error(final int egl_error){
        String _res = elg14ErrorToStringMap.get(egl_error);
        if (_res == null){
            _res = "Unknown error code = " + egl_error;
        }
        return _res;
    }

    public RenderingRuntimeException(String message_) {
        super(message_);
    }

    public RenderingRuntimeException(String message_, Throwable cause) {
        super(message_, cause);
    }
}
