package org.c4sci.camera2opengl.preview;

import android.app.Activity;
import android.view.SurfaceView;
import android.view.TextureView;

import org.c4sci.camera2opengl.utilities.CameraResolutionChooser;

/**
 * Root class to link a camera preview to treatments :
 * <ol>
 *     <li>The camera preview is a stream to a {@link TextureView}, that can be used during OpenGL rendering.</li>
 *     <li>This rendering is bound to output {@link SurfaceView}s</li>
 *     <li>all OpenGL actions are ensured to be made in the same dedicated thread that possesses the {@link android.opengl.EGLContext}s.</li>
 *     <li>At each focus phase, an action is performed in a dedicated thread. If the action cannot
 *     be performed (e.g. there is not time), a dedicted action is performed.</li>
 * </ol>
 */
public abstract class CameraPreviewToProcessor {

    private Runnable actionOnFocusStarted;
    private Runnable actionOnFocused;
    private Runnable actionOnFocusing;
    private Runnable actionOnProcessingSkipped;

    private TextureView             inputTexturePreview;
    private SurfaceView[]           outputSurfaceViews;
    private Activity                rootActivity;
    private PreviewImageProcessor   previewImageProcessor;

    private CameraResolutionChooser.ShapeCriterion      textureShapeCriterion;
    private CameraResolutionChooser.ResolutionCriterion textureResolutionCriterion;

    private CameraResolutionChooser.OptionalComputer    textureShapeOptional;
    private CameraResolutionChooser.OptionalComputer    textureResolutionOptional;

    /**
     *
     * @param output_surface_views The views that can be modified using OpenGL in the {@link PreviewImageProcessor}
     * @param start_focusing_ui Action to perform when focusing get started
     * @param focused_ui Action to perform when focused is acquired
     * @param focusing_ui Action to perform when focus is in progress
     * @param skipped_ui Action to perform in case there is no time to process the last focused image with the {@link PreviewImageProcessor}
     * @param root_activity The activity the surfaces belong to.
     * @param image_processor The processing of the preview images
     * @param shape_criterion The criterion to choose the camera definition when the TextureView is resized
     */
    public CameraPreviewToProcessor(
            final SurfaceView[] output_surface_views,
            final Runnable start_focusing_ui,
            final Runnable focused_ui,
            final Runnable focusing_ui,
            final Runnable skipped_ui,
            Activity root_activity,
            PreviewImageProcessor image_processor,
            CameraResolutionChooser.ShapeCriterion shape_criterion,
            CameraResolutionChooser.OptionalComputer shape_optional_computer,
            CameraResolutionChooser.ResolutionCriterion resolution_criterion,
            CameraResolutionChooser.OptionalComputer resolution_optional_computer){

        outputSurfaceViews = output_surface_views;
        rootActivity =      root_activity;
        previewImageProcessor = image_processor;

        actionOnFocusStarted = start_focusing_ui;
        actionOnFocused =         focused_ui;
        actionOnFocusing =        focusing_ui;
        actionOnProcessingSkipped =         skipped_ui;

        textureShapeCriterion =         shape_criterion;
        textureShapeOptional =          shape_optional_computer;
        textureResolutionCriterion =    resolution_criterion;
        textureResolutionOptional =     resolution_optional_computer;
    }

    /**
     * This method is to be called in the @afterViews method (see org.androidannotations annotations) of UI class.
     * @param texture_view  The {@link TextureView} that will receive the preview, and can be used as an OpenGL texture
     * @see <a href="https://developer.android.com/reference/android/view/TextureView">TextureView</a></a>
     */
    public abstract void afterViews(TextureView texture_view);

    /**
     * This method must be called by onResume() of the underlying UI Activity or Fragment.
     * @see <a href="https://developer.android.com/reference/android/app/Activity">Licecycles of Activities and Fragments</a>
     */
    public abstract void onResume();

    /**
     * This method must be called by onPause() of the underlying UI Activity or Fragment.
     */
    public abstract void onPause();

    public Runnable getActionOnFocusStarted() {
        return actionOnFocusStarted;
    }

    public Runnable getActionOnFocused() {
        return actionOnFocused;
    }

    public Runnable getActionOnFocusing() {
        return actionOnFocusing;
    }

    public Runnable getActionOnProcessingSkipped() {
        return actionOnProcessingSkipped;
    }

    public SurfaceView[] getOutputSurfaceViews() {
        return outputSurfaceViews;
    }

    public Activity getRootActivity() {
        return rootActivity;
    }

    public PreviewImageProcessor getPreviewImageProcessor() {
        return previewImageProcessor;
    }

    public TextureView getInputTexturePreview() {
        return inputTexturePreview;
    }

    /**
     * Updates the {@link TextureView} that can be used as an entry texture containing the camera preview
     * @param inputTexturePreview
     */
    public void setInputTexturePreview(TextureView inputTexturePreview) { this.inputTexturePreview = inputTexturePreview; }

    public CameraResolutionChooser.ShapeCriterion getTextureShapeCriterion() { return textureShapeCriterion; }

    public CameraResolutionChooser.ResolutionCriterion getTextureResolutionCriterion() { return textureResolutionCriterion; }

    public CameraResolutionChooser.OptionalComputer getTextureShapeOptional() { return textureShapeOptional; }

    public CameraResolutionChooser.OptionalComputer getTextureResolutionOptional() { return textureResolutionOptional; }

}
