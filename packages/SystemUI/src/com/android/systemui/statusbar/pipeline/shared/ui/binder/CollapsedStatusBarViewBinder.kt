/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.systemui.statusbar.pipeline.shared.ui.binder

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.android.systemui.lifecycle.repeatWhenAttached
import com.android.systemui.statusbar.pipeline.shared.ui.viewmodel.CollapsedStatusBarViewModel

object CollapsedStatusBarViewBinder {
    /**
     * Binds the view to the view-model. [listener] will be notified whenever an event that may
     * change the status bar visibility occurs.
     */
    @JvmStatic
    fun bind(
        view: View,
        viewModel: CollapsedStatusBarViewModel,
        listener: StatusBarVisibilityChangeListener,
    ) {
        view.repeatWhenAttached {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.isTransitioningFromLockscreenToOccluded.collect {
                    listener.onStatusBarVisibilityMaybeChanged()
                }
            }
        }
    }
}

/**
 * Listener to be notified when the status bar visibility might have changed due to the device
 * moving to a different state.
 */
fun interface StatusBarVisibilityChangeListener {
    fun onStatusBarVisibilityMaybeChanged()
}
