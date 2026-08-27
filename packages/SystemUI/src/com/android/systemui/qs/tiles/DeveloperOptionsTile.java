/*
 * Copyright (C) 2025 The LineageOS Project
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

package com.android.systemui.qs.tiles;

import static com.android.internal.logging.MetricsLogger.VIEW_UNKNOWN;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.service.quicksettings.Tile;
import android.widget.Switch;

import androidx.annotation.Nullable;

import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.systemui.animation.Expandable;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QsEventLogger;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.shared.QSSettingsPackageRepository;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.res.R;
import com.android.systemui.util.settings.GlobalSettings;
import com.android.systemui.util.settings.SettingObserver;

import javax.inject.Inject;

/** Quick settings tile: toggle Developer options */
public class DeveloperOptionsTile extends QSTileImpl<BooleanState> {

    public static final String TILE_SPEC = "dev_options";

    @Nullable
    private Icon mIcon = null;

    private final SettingObserver mSetting;
    private final QSSettingsPackageRepository mQSSettingsPackageRepository;
    private final UserManager mUserManager;

    @Inject
    public DeveloperOptionsTile(
            QSHost host,
            QsEventLogger uiEventLogger,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger,
            GlobalSettings globalSettings,
            QSSettingsPackageRepository qsSettingsPackageRepository
    ) {
        super(host, uiEventLogger, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger);
        mQSSettingsPackageRepository = qsSettingsPackageRepository;
        mUserManager = mContext.getSystemService(UserManager.class);
        mSetting = new SettingObserver(globalSettings, mHandler,
                Global.DEVELOPMENT_SETTINGS_ENABLED,
                Build.TYPE.equals("eng") ? 1 : 0) {
            @Override
            protected void handleValueChanged(int value, boolean observedChange) {
                handleRefreshState(value);
            }
        };
    }

    @Override
    public boolean isAvailable() {
        return mUserManager != null && mUserManager.isUserAdmin(mHost.getUserId());
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mSetting.setListening(false);
    }

    @Override
    public void handleSetListening(boolean listening) {
        super.handleSetListening(listening);
        mSetting.setListening(listening);
    }

    @Override
    protected void handleClick(@Nullable Expandable expandable) {
        if (!isAvailable()) {
            return;
        }
        if (mState.value) {
            // Match the Settings dashboard switch: hide Developer options and reset ADB / flags.
            final Intent intent = new Intent(
                    DevelopmentSettingsEnabler.ACTION_DISABLE_DEVELOPMENT_SETTINGS);
            intent.setComponent(new ComponentName(
                    mQSSettingsPackageRepository.getSettingsPackageName(),
                    DevelopmentSettingsEnabler.DISABLE_DEVELOPMENT_SETTINGS_RECEIVER));
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mContext.sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
        } else {
            // Same as tapping the build number seven times: enable the menu, leave flags as-is.
            DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(mContext, true);
        }
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                .setPackage(mQSSettingsPackageRepository.getSettingsPackageName());
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_developer_options_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        checkIfRestrictionEnforcedByAdminOnly(state, UserManager.DISALLOW_DEBUGGING_FEATURES);

        final int value = arg instanceof Integer ? (Integer) arg : mSetting.getValue();
        final boolean enabled = value != 0;
        state.value = enabled;
        state.label = mContext.getString(R.string.quick_settings_developer_options_label);
        if (mIcon == null) {
            mIcon = maybeLoadResourceIcon(R.drawable.ic_qs_developer_options);
        }
        state.icon = mIcon;
        state.expandedAccessibilityClassName = Switch.class.getName();
        if (!isAvailable()) {
            state.state = Tile.STATE_UNAVAILABLE;
            state.contentDescription = state.label;
            return;
        }
        if (enabled) {
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_developer_options_on);
            state.state = Tile.STATE_ACTIVE;
        } else {
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_developer_options_off);
            state.state = Tile.STATE_INACTIVE;
        }
    }

    @Override
    public int getMetricsCategory() {
        return VIEW_UNKNOWN;
    }
}
