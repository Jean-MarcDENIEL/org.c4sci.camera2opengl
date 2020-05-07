package com.example.learningcamera2texture.ui;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import androidx.annotation.NonNull;

import org.c4sci.threads.IParametrizedRunnable;
import org.c4sci.threads.ProgrammableThreadingException;
import org.c4sci.threads.ProgrammablePoolThread;

public class PreviewSessionCallBack extends CameraCaptureSession.CaptureCallback {

    private static final int ON_CAPTURE_STARTED_ACTION = 0;
    private static final int ON_CAPTURE_COMPLETED_FOCUSED = 1;

    private final Runnable onCaptureStartedAction;
    private final Runnable onCaptureCompletedFocused;
    private final Runnable onCaptureCompletedFocusing;

    private class OnCaptureStartedAction implements IParametrizedRunnable{

        @Override
        public Object getParametersBundle() {
            return null;
        }
        @Override
        public IParametrizedRunnable newInstance() {
            return new OnCaptureStartedAction();
        }

        @Override
        public boolean needsParametersBundle() {
            return false;
        }

        @Override
        public Class parametersBundleClass() {
            return null;
        }

        @Override
        public void run() {
            onCaptureStartedAction.run();
        }
    }

    private class OnCaptureCompletedFocusedAction implements IParametrizedRunnable{

        @Override
        public Object getParametersBundle() {
            return null;
        }

        @Override
        public IParametrizedRunnable newInstance() {
            return new OnCaptureCompletedFocusedAction();
        }

        @Override
        public boolean needsParametersBundle() {
            return false;
        }

        @Override
        public Class parametersBundleClass() {
            return null;
        }

        @Override
        public void run() {
            onCaptureCompletedFocused.run();
        }
    }

    public PreviewSessionCallBack(
            final Runnable on_capture_started_action,
            final Runnable on_capture_focused_action,
            final Runnable on_capture_focusing_action){
        onCaptureStartedAction = on_capture_started_action;
        onCaptureCompletedFocused = on_capture_focused_action;
        onCaptureCompletedFocusing = on_capture_focusing_action;
    }

    @Override
    public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
        super.onCaptureStarted(session, request, timestamp, frameNumber);
        onCaptureStartedAction.run();
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        Integer _focused = result.get(CaptureResult.CONTROL_AF_STATE);
        if (_focused == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED) {
            onCaptureCompletedFocused.run();
        }
        else{
            onCaptureCompletedFocusing.run();
        }
    }
}
