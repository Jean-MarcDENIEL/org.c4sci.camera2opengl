package org.c4sci.camera2opengl.preview;

/**
 * This interface represents the processing of a preview image in a OpenGL context. That is why :
 * <ol>
 *     <li>Image treatment and OpenGL operations will take place in a single thread, which the {@link PreviewImageProcessor} doesn't have to care for.</li>
 *     <li>OpenGL data necessary to operate EGL and GL methods are given by an {@link PreviewImageBundle}</li>
 *     <li>The {@link PreviewImageProcessor}</li> does not have to deal with iamge drawing (i.e swapping buffers)
 * </ol>
 *
 * All open gl code should be in the {@link #processPreviewImage(PreviewImageBundle)} method
 */
public interface PreviewImageProcessor {

    /**
     * The major OpenGL version number that is necessary in {@link #processPreviewImage(PreviewImageBundle)}. E.g 3 for OpenGL 3
     * @return
     */
    public abstract int leastMajorOpenGlVersion();

    /**
     * The minor OpenGL version number, to complete {@link #leastMajorOpenGlVersion()}. E.g 1 for Open GL 3.1
     * @return
     */
    public abstract int leastMinorOpenGlVersion();

    /**
     * Process the preview image accessible through {@link PreviewImageBundle#getInputSurfaceTexture()}
     * @param processor_bundle gives all the necessary data to
     */
    public abstract void processPreviewImage(PreviewImageBundle processor_bundle);

    /**
     * This method should be called in parent onResume() or before the first call to {@link #processPreviewImage(PreviewImageBundle)}
     */
    public void onResume();

    /**
     * This must be called in parent onPause() or as soon as the {@link PreviewImageProcessor} won't be used anymore
     */
    public void onPause();
}
