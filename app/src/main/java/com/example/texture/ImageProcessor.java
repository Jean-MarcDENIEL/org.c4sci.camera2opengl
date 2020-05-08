package com.example.texture;

/**
 * All open gl code should be in the {@link #()} method
 */
public interface ImageProcessor{

    public abstract int leastMajorOpenGlVersion();
    public abstract int leastMinorOpenGlVersion();
    public abstract void processImage(ImageProcessorBundle processor_bundle);
}
