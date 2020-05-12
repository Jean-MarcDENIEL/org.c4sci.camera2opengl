package org.c4sci.camera2opengl.ui;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import androidx.annotation.NonNull;

import org.c4sci.threads.IParametrizedRunnable;

public class PreviewSessionCallBack extends CameraCaptureSession.CaptureCallback {

    private static final int ON_CAPTURE_STARTED_ACTION = 0;
    private static final int ON_CAPTURE_COMPLETED_FOCUSED = 1;

    private final Runnable onCaptureStartedAction;
    private final Runnable onCaptureCompletedFocused;
    private final Runnable onCaptureCompletedFocusing;

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
