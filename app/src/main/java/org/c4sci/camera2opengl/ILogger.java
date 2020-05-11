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

    default boolean canLogD(){
        return _DEBUG_ALLOWED;
    }
    default boolean canLogE(){
        return _ERROR_ALLOWED;
    }

    default void logD(Throwable e_){
        if (canLogD()) {
            StringBuilder _msg = new StringBuilder();
            while (e_ != null) {
                _msg.append(e_.getMessage() + "\n");
                e_ = e_.getCause();
            }
            logD(_msg.toString());
        }
    }

    default void logD(String msg_){
        if (canLogD()) {
            logD(this, msg_);
        }
    }

    default void logE(String err_msg) {
        if (_ERROR_ALLOWED) {
            Log.e(getLogName(), err_msg);
        }
    };

    default void logE(Throwable e_) {
        if (canLogE()) {
            Log.e(getLogName(), e_.getMessage(), e_);
        }
    };

}
