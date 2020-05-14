package org.c4sci.camera2opengl.utilities;

import android.util.Size;

import org.c4sci.camera2opengl.RenderingRuntimeException;

/**
 * These classes are intended at choosing a definition among those proposed by a camera.
 */
public class CameraResolutionChooser {

    //TODO
    // take sensor tilting into account

    private static final float EPSILON = 1E-6f;

    private abstract static class SizeAppraisal {
        /**
         *
         * @param width_px Camera resolution width
         * @param height_px Camera resolution height
         * @param size_criterion_opt Optional width criterion
         * @return The best is closest to 0
         */
        abstract float appraise(float width_px, float height_px, float size_criterion_opt);
    }

    public enum ShapeCriterion {
        /**
         *  Will avoid or minimize cropping
         */
        SHAPE_UNCROPPED(new SizeAppraisal(){
            @Override
            float appraise(float width_px, float height_px, float shape_criterion_opt) {
                return 1.0f / (width_px * height_px);
            }
        })
        ,
        /**
         * Will maximize width / height ratio
         */
        SHAPE_WIDEST(new SizeAppraisal() {
            @Override
            float appraise(float width_px, float height_px, float shape_criterion_opt) {
                float width_on_height_ratio = width_px / height_px;
                return 1.0f / width_on_height_ratio;
            }
        })
        ,
        /**
         * Will minimize width / height ratio
         */
        SHAPE_NARROWEST(new SizeAppraisal() {
            @Override
            float appraise(float width_px, float height_px, float shape_criterion_opt) {
                float width_on_height_ratio = width_px / height_px;
                return width_on_height_ratio;
            }
        }),
        /**
         * Will prefer width / height ratio closest to 1
         */
        SHAPE_SQUAREST(new SizeAppraisal() {
            @Override
            float appraise(float width_px, float height_px, float shape_criterion_opt) {
                float width_on_height_ratio = width_px / height_px;
                return (float)Math.abs(1.0 - width_on_height_ratio);
            }
        }),
        /**
         * Will prefer the width / height ratio closest to a given W/H ratio given by optional
         * value in {@link #chooseOptimalCaptureDefinition(Size[], ShapeCriterion, float, ResolutionCriterion, float, boolean)}
         */
        SHAPE_WIDTH_ON_HEIGHT_CLOSEST(new SizeAppraisal() {
            @Override
            float appraise(float width_px, float height_px, float shape_criterion_opt) {
                float _ratio = width_px / height_px;
                return (float)Math.abs(_ratio - shape_criterion_opt);
            }
        })
        ;

        ShapeCriterion(SizeAppraisal resolution_appraise){
            shapeAppraisal = resolution_appraise;
        }

        private SizeAppraisal shapeAppraisal;
    };

    public enum ResolutionCriterion {
        RESOLUTION_HIGHEST(new SizeAppraisal() {
            @Override
            float appraise(float width_px, float height_px, float resolution_criterion_opt) {
                return 1.0f / (width_px* height_px);
            }
        }),
        RESOLUTION_LOWEST(new SizeAppraisal() {
            @Override
            float appraise(float width_px, float height_px, float size_criterion_opt) {
                return width_px * height_px;
            }
        }),
        /**
         * Will choose the resolution witch width is closest to optional parameter
         */
        WIDTH_CLOSEST(new SizeAppraisal() {
            @Override
            float appraise(float width_px, float height_px, float size_criterion_opt) {
                return (float)Math.abs(width_px - size_criterion_opt);
            }
        }),
        /**
         * Will choose the resolution witch height is closest to optional parameter
         */
        HEIGHT_CLOSEST(new SizeAppraisal() {
            @Override
            float appraise(float width_px, float height_px, float size_criterion_opt) {
                return (float)Math.abs(height_px - size_criterion_opt);
            }
        });
        ResolutionCriterion(SizeAppraisal resolution_appraisal){
            resolutionAppraisal = resolution_appraisal;
        }

        private SizeAppraisal   resolutionAppraisal;
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
            throw new RenderingRuntimeException("Cannot work on null or negative surface dimensions: " + surface_width + " * " + surface_height);
        }
        float width_on_height_ratio = (float)surface_width / (float)surface_height;
        return chooseOptimalCaptureDefinition(
                possible_capture_definitions,
                ShapeCriterion.SHAPE_WIDTH_ON_HEIGHT_CLOSEST, width_on_height_ratio,
                ResolutionCriterion.WIDTH_CLOSEST, surface_width,
                sensor_aligned_with_view);
    }

    /**
     * Will choose the optimal resolution, to best conform to the criteria given in parameters, in two filtering passes.
     * @param possible_capture_resolutions Possible camera resolutions
     * @param shape_criterion The criterion on the shape of the image, as a primary filter among possible resolutions
     * @param shape_criterion_value An optional shape criterion value depending on the shape criterion
     * @param resolution_criterion The secondary filter among possible resolutions
     * @param resolution_criterion_value An optional resolution value depending on the resolution_criterion
     * @param sensor_aligned_with_view Indicates whether the sensor is tilted from the surface view (usually true = landscape mode)
     * @return The optimal resolution among those passed as parameter.
     * @throws RenderingRuntimeException in case of error
     */
    public Size chooseOptimalCaptureDefinition(
            Size[] possible_capture_resolutions, ShapeCriterion shape_criterion, float shape_criterion_value,
            ResolutionCriterion resolution_criterion, float resolution_criterion_value,
            boolean sensor_aligned_with_view){
        ensureDefinitionsAvailable(possible_capture_resolutions);
        //TODO
        // TO be completed

        // First pass : select shapes
        //
        float[] _appraisal = new float[possible_capture_resolutions.length];
        for (int _i=0; _i<possible_capture_resolutions.length; _i++){
            _appraisal[_i] = shape_criterion.shapeAppraisal.appraise(possible_capture_resolutions[_i].getWidth(), possible_capture_resolutions[_i].getHeight(),
                   shape_criterion_value);
        }
        float _lowest_appraisal = _appraisal[0]; // lower is better
        for (int _i=1; _i<_appraisal.length; _i++){
            if (_lowest_appraisal > _appraisal[_i]){
                _lowest_appraisal = _appraisal[_i];
            }
        }
        boolean[] _shape_filter_passed = new boolean[possible_capture_resolutions.length];
        for (int _i=0; _i< possible_capture_resolutions.length; _i++){
            _shape_filter_passed[_i] = _appraisal[_i] - _lowest_appraisal < EPSILON;
        }

        // Second pass : select resolutions among selected shapes
        boolean _first_resol_appraisal = true;
        int _best_resolution_index = 0;
        for (int _i=0; _i<possible_capture_resolutions.length; _i++){
            if (_shape_filter_passed[_i]) {
                float _resol_appraise =
                        resolution_criterion.resolutionAppraisal.appraise(
                                possible_capture_resolutions[_i].getWidth(),
                                possible_capture_resolutions[_i].getHeight(),
                                resolution_criterion_value);
                if (_first_resol_appraisal) {
                    _first_resol_appraisal = false;
                    _lowest_appraisal = _resol_appraise;
                    _best_resolution_index = _i;
                } else {
                    if (_appraisal[_i] < _lowest_appraisal) {
                        _lowest_appraisal = _resol_appraise;
                        _best_resolution_index = _i;
                    }
                }
            }
        }

        if (_first_resol_appraisal){
            // Error : no resolution passed the first filter !
            throw new RenderingRuntimeException("No resolution passed the shape filter: " + shape_criterion + " with: " + shape_criterion_value);
        }

        return possible_capture_resolutions[_best_resolution_index];
    }


    private static void ensureDefinitionsAvailable(final Size[] possible_resolutions){
        if ((possible_resolutions == null) ||(possible_resolutions.length == 0)){
            throw new RenderingRuntimeException("No capture definition available");
        }
    }

}
