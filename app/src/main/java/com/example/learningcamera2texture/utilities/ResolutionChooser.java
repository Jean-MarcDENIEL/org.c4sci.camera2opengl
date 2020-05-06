package com.example.learningcamera2texture.utilities;

import android.util.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResolutionChooser {

    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs_, Size rhs_) {
            return Long.signum( (long)(lhs_.getWidth() * lhs_.getHeight()) -
                    (long)(rhs_.getWidth() * rhs_.getHeight()));
        }
    }

    public static Size chooseOptimalCaptureDefinition(
            Size[] possible_capture_definitions, int surface_width, int surface_height,
            final boolean sensor_aligned_with_view) {
        List<Size> _big_enough = new ArrayList<Size>();

        double _widest_ratio = 0.0;
        for (Size _option : possible_capture_definitions){
            _widest_ratio = Math.max(_widest_ratio, (double)_option.getWidth()/_option.getHeight());
        }

        for(Size _option : possible_capture_definitions) {
            boolean _should_add =
                    ((double)_option.getWidth()/_option.getHeight() / _widest_ratio > 0.9)&&
                            (   (sensor_aligned_with_view && _option.getWidth() >= surface_width)||
                                    ((!sensor_aligned_with_view) && _option.getHeight() >= surface_width));

            if (_should_add){
                _big_enough.add(_option);
            }
        }
        Size _res;
        if(_big_enough.size() > 0) {
            _res = Collections.min(_big_enough, new CompareSizeByArea());
        } else {
            _res = possible_capture_definitions[0];
        }
        return _res;
    }
}
