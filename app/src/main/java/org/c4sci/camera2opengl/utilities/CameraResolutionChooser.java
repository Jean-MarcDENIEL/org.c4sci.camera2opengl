package org.c4sci.camera2opengl.utilities;

import android.util.Size;

import org.c4sci.camera2opengl.RenderingRuntimeException;

/**
 * These classes are intended at choosing a definition ammong those proposed by a camera.
 */
public class CameraResolutionChooser {

    private abstract static class ResolutionAppraise{
        /**
         *
         * @param width_px Camera resolution width
         * @param height_px Camera resolution height
         * @param width_criterion_opt Optional width criterion
         * @param height_criterion_opt Optional height criterion
         * @return The best is closest to 0
         */
        abstract double appraise(float width_px, float height_px, float width_criterion_opt, float height_criterion_opt);
    }

    public enum ShapeCriterion {
        /**
         *  Will avoid or minimize cropping
         */
        SHAPE_UNCROPPED(new ResolutionAppraise(){
            @Override
            double appraise(float width_px, float height_px, float width_criterion_opt, float height_criterion_opt) {
                return 1.0 / (width_px * height_px);
            }
        })
        ,
        /**
         * Will maximize width / height ratio
         */
        SHAPE_WIDEST(new ResolutionAppraise() {
            @Override
            double appraise(float width_px, float height_px, float width_criterion_opt, float height_criterion_opt) {
                float width_on_height_ratio = width_px / height_px;
                return 1.0 / width_on_height_ratio;
            }
        })
        ,
        /**
         * Will minimize width / height ratio
         */
        SHAPE_NARROWEST(new ResolutionAppraise() {
            @Override
            double appraise(float width_px, float height_px, float width_criterion_opt, float height_criterion_opt) {
                float width_on_height_ratio = width_px / height_px;
                return width_on_height_ratio;
            }
        }),
        /**
         * Will prefer width / height ratio closest to 1
         */
        SHAPE_SQUAREST(new ResolutionAppraise() {
            @Override
            double appraise(float width_px, float height_px, float width_criterion_opt, float height_criterion_opt) {
                float width_on_height_ratio = width_px / height_px;
                return Math.abs(1.0 - width_on_height_ratio);
            }
        }),
//        /**
//         * Will prefer the width / height ratio closest to a given value
//         */
//        SHAPE_WITH_ON_HEIGHT_CLOSEST
        ;

        ShapeCriterion(ResolutionAppraise resolution_appraise){
            resolutionAppraise = resolution_appraise;
        }

        private ResolutionAppraise  resolutionAppraise;
    };

    public enum ResolutionCriterion {
        RESOLUTION_HIGHEST,
        RESOLUTION_LOWEST,
        /**
         * Will choose the resolution witch width is closest to optional parameter
         */
        WIDTH_CLOSEST,
        /**
         * Will choose the resolution witch height is closest to optional parameter
         */
        HEIGHT_CLOSEST
    }

    /**
     * Will choose the closest resolution to parameters.
     * @param possible_capture_definitions Possible camera resolutions.
     * @param surface_width Surface width in pixels
     * @param surface_height Surface height in pixels
     * @param sensor_aligned_with_view Indicates whether the sensor is tilted from the surface view (usually true = landscape mode)
     * @return The closest camera resolution among those passed as parameter.
     */
    public Size chooseClosestCaptureDefinition(
            Size[] possible_capture_definitions, int surface_width, int surface_height,
            boolean sensor_aligned_with_view){
        ensureDefinitionsAvailable(possible_capture_definitions);
        if ((surface_width <= 0)|| (surface_height <= 0)){
            throw new RenderingRuntimeException("Cannot work on null or negative surface dimensions: " + surface_width " * " + surface_height);
        }
        float width_on_height_ratio = (float)surface_width / (float)surface_height;
        return chooseOptimalCaptureDefinition(
                possible_capture_definitions,
                ShapeCriterion.SHAPE_WITH_ON_HEIGHT_CLOSEST, width_on_height_ratio,
                ResolutionCriterion.WIDTH_CLOSEST, surface_width,
                sensor_aligned_with_view);
    }

    /**
     * Will choose the optimal resolution, to best conform to the criteria given in parameters, in two filtering passes.
     * @param possible_capture_resolutions Possible camera resolutions
     * @param shape_criterion The criterion on the shape of the image, as a primary filter among possible resolutions
     * @param shape_criterion_value An optional shape criterion value
     * @param resolution_definition The secondary filter among possible resolutions
     * @param resolution_definition_value An optional resolution value
     * @param sensor_aligned_with_view Indicates whether the sensor is tilted from the surface view (usually true = landscape mode)
     * @return The optimal resolution among those passed as parameter.
     */
    public Size chooseOptimalCaptureDefinition(
            Size[] possible_capture_resolutions, ShapeCriterion shape_criterion, float shape_criterion_value,
            ResolutionCriterion resolution_definition, float resolution_definition_value,
            boolean sensor_aligned_with_view){
        //TODO
        // TO be completed

        // First pass : select shapes
        //
        boolean[] filter_passed = new boolean[possible_capture_resolutions.length];

        return null;
    }


    private static void ensureDefinitionsAvailable(final Size[] possible_resolutions){
        if ((possible_resolutions == null) ||(possible_resolutions.length == 0)){
            throw new RenderingRuntimeException("No capture definition available");
        }
    }

}
