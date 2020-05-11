package org.c4sci.camera2opengl;
import android.util.Log;


/**
 * This class is used to permit logging and tracing of processes.
 */

public interface ILogger {

    boolean   _DEBUG_ALLOWED = true;
    boolean   _ERROR_ALLOWED = true;

    String getLogName();

    static void logD(ILogger source_, String msg_){
        if (_DEBUG_ALLOWED) {
            Log.d(source_.getLogName(), msg_);
        }
    }

    default void logD(Throwable e_){
        StringBuilder _msg = new StringBuilder();
        while (e_ != null){
            _msg.append(e_.getMessage() + "\n");
            e_ = e_.getCause();
        }
        logD(_msg.toString());
    }

    default void logD(String msg_){
        logD(this, msg_);
    }

    default void logE(String err_msg) {
        if (_ERROR_ALLOWED) {
            Log.e(getLogName(), err_msg);
        }
    };

    default void logE(Throwable e_) {
        if (_ERROR_ALLOWED) {
            Log.e(getLogName(), e_.getMessage(), e_);
        }
    };

}
