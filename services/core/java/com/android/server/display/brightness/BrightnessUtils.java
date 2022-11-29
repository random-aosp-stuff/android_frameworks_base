/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.display.brightness;

import android.os.PowerManager;

import com.android.server.display.DisplayBrightnessState;

/**
 * A helper class for eualuating brightness utilities
 */
public final class BrightnessUtils {
    /**
     * Checks whether the brightness is within the valid brightness range, not including off.
     */
    public static boolean isValidBrightnessValue(float brightness) {
        return !Float.isNaN(brightness) && brightness >= PowerManager.BRIGHTNESS_MIN
                && brightness <= PowerManager.BRIGHTNESS_MAX;
    }

    /**
     * A utility to construct the DisplayBrightnessState
     */
    public static DisplayBrightnessState constructDisplayBrightnessState(
            int brightnessChangeReason, float brightness, float sdrBrightness) {
        BrightnessReason brightnessReason = new BrightnessReason();
        brightnessReason.setReason(brightnessChangeReason);
        return new DisplayBrightnessState.Builder()
                .setBrightness(brightness)
                .setSdrBrightness(sdrBrightness)
                .setBrightnessReason(brightnessReason)
                .build();
    }
}
