package com.example.learningcamera2texture;

import android.util.Log;

public interface ILogable {

    public static boolean   _DEBUG_ALLOWED = true;

    String getLogName();

    static void logD(ILogable source_, String msg_){
        //System.out.println(source_.getLogName() + " : " + msg_);
        Log.d(source_.getLogName() , msg_);
    }

    default void logD(Throwable e_){
        StringBuilder _msg = new StringBuilder();
        while (e_ != null){
            _msg.append(e_.getMessage() + "\n");
        }
        logD(_msg.toString());
    }

    default void logD(String msg_){
        logD(this, msg_);
    }

    void logE(String err_msg);

    void logE(Throwable e_);

}
