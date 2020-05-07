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
    private final Runnable onSkippedCaptureAction;

    private ProgrammablePoolThread threadPool;
    private ProgrammablePoolThread.TaskPublishingPolicy publishingPolicy;

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
            final Runnable on_capture_focusing_action,
            final Runnable on_skipped_capture_action,
            final ProgrammablePoolThread.TaskPublishingPolicy publishing_policy,
            final int thread_pool_size){
        onCaptureStartedAction = on_capture_started_action;
        onCaptureCompletedFocused = on_capture_focused_action;
        onCaptureCompletedFocusing = on_capture_focusing_action;
        onSkippedCaptureAction = on_skipped_capture_action;
        publishingPolicy = publishing_policy;

        threadPool = new ProgrammablePoolThread(thread_pool_size);
        threadPool.addProcessor(ON_CAPTURE_STARTED_ACTION, new OnCaptureStartedAction());
        threadPool.addProcessor(ON_CAPTURE_COMPLETED_FOCUSED, new OnCaptureCompletedFocusedAction());
        threadPool.start();

    }

    @Override
    public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
        super.onCaptureStarted(session, request, timestamp, frameNumber);
        if (threadPool != null) {
            threadPool.publishTaskToProcess(ON_CAPTURE_STARTED_ACTION, null, publishingPolicy);
        }
        else{
            throw new RuntimeException("Threadpool = null");
        }
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        Integer _focused = result.get(CaptureResult.CONTROL_AF_STATE);
        if (_focused != null) {
            if (_focused == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED) {
                if (!threadPool.publishTaskToProcess(ON_CAPTURE_COMPLETED_FOCUSED, null, publishingPolicy)) {
                    onSkippedCaptureAction.run();
                }
            }
            else{
                onCaptureCompletedFocusing.run();
            }
        }
    }
}
