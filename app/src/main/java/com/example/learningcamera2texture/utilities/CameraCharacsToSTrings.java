package com.example.learningcamera2texture.utilities;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.util.Range;
import android.util.SizeF;

public class CameraCharacsToSTrings {

    private static final String[] AF_MODES = {"OFF", "AUTO", "MACRO", "CONTINUOUS PICTURE", "CONTINOUS VIDEO", "EDOF"};
    private static final String[] HARDWARE_LEVEL = {"LIMITED", "FULL", "LEGACY", "LEVEL_3", "EXTERNAL"};
    private static final String[] LENS_CALIBRATION = {"UNCALIBRATED", "APPROXIMATE", "CALIBRATED",};
    private static final String[] TONEMAP_MODES = {"CONTRAST_CURVE", "FAST", "HIGH_QUALITY", "GAMMA_VALUE", "PRESET_CURVE"};


    private static String stringValueAmong(int value_, String[] possible_values) {
        if (value_ >= possible_values.length) {
            return "Unkown";
        } else {
            return possible_values[value_];
        }
    }

    private String showCameraCharacteristics(CameraCharacteristics _characs){

        StringBuilder _res = new StringBuilder();

        Range<Integer>[] _ranges =  _characs.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        for (Range<Integer> _framerate : _ranges){
            _res.append("   AE Framerate = " + _framerate.getLower() + " - " + _framerate.getUpper() + " FPS\n");
        }
        for (int _af_mode : _characs.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)){
            _res.append("    AF mode : " + stringValueAmong(_af_mode, AF_MODES)+"\n");
        }
        _res.append("    Max AF regions = " + _characs.get(
                CameraCharacteristics.CONTROL_MAX_REGIONS_AF)+"\n");
        _res.append("   Flash available ? " +(
                _characs.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ? "yes" : "no")+"\n");
        int _level = _characs.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        _res.append("   HARDWARE level = " + stringValueAmong(_level, HARDWARE_LEVEL)+"\n");

        int _facing = _characs.get(CameraCharacteristics.LENS_FACING);
        String _facing_msg;
        switch(_facing){
            case CameraMetadata.LENS_FACING_BACK:
                _facing_msg = "Backward";
                break;
            case CameraMetadata.LENS_FACING_FRONT:
                _facing_msg = "Frontward (selfie)";
                break;
            case CameraMetadata.LENS_FACING_EXTERNAL:
                _facing_msg = "External";
                break;
            default:
                _facing_msg = "Unkown lens facing direction";
        }
        _res.append("   LENS facing " + _facing_msg+"\n");
        _res.append("   Available f-numbers : \n");
        for (float _f_stop : _characs.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)){
            _res.append("       f-number: " + _f_stop+"\n");
        }

        _res.append("   Available density filters :\n");
        for(float _density : _characs.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES)){
            _res.append("       " + _density + " EV\n");
        }

        _res.append("   Available focal lengths :\n");
        for (float _foc_length : _characs.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)){
            _res.append("      " + _foc_length + " mm\n");
        }

        int _lens_calib = _characs.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
        _res.append("   Lens Calibration : " + stringValueAmong(_lens_calib, LENS_CALIBRATION)+"\n");
        _res.append("   Hyperfocal dist = " +
                _characs.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE) + " DIOPTR (1/D)\n");
        _res.append("   Min focus dist = " +
                _characs.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) + " DIOPTR (1/D)\n");

        Rect _rect = _characs.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        _res.append("   Array size : " +_rect.width() + " x " + _rect.height()+"\n");

        Range<Long> _exposure_range = _characs.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        if (_exposure_range != null) {
            _res.append("   Exposure from " + _exposure_range.getLower() + " to " + _exposure_range.getUpper() + " nanoseconds\n");
        }
        else{
            _res.append("   No exposure range available\n");
        }

        SizeF _size = _characs.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        _res.append("   Sensor size = " + _size.getWidth() + " x " + _size.getHeight() +" mm\n");

        Range<Integer> _iso_range = _characs.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        if (_iso_range != null){
            _res.append("   ISO from " + _iso_range.getLower() + " to  " + _iso_range.getUpper() + " ISO\n");
        }
        _res.append("   Sensor orientation : " + _characs.get(CameraCharacteristics.SENSOR_ORIENTATION) + " degree clockwise"+"\n");
        _res.append("   ToneMap Modes available are: \n");
        for (int _tone : _characs.get(CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES)){
            _res.append("      " + stringValueAmong(_tone, TONEMAP_MODES)+"\n");
            if (_tone == CameraCharacteristics.TONEMAP_MODE_CONTRAST_CURVE){
                _res.append("       -> Max tone curve points = " + _characs.get(CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS)+"\n");
            }
        }
        return _res.toString();
    }
}
