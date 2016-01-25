/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts.common.preference;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.android.contacts.common.R;
import com.android.contacts.common.activity.LicenseActivity;
import com.android.contacts.common.compat.MetadataSyncEnabledCompat;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.model.account.GoogleAccountType;

import java.util.List;

/**
 * This fragment shows the preferences for the first header.
 */
public class DisplayOptionsPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_display_options);

        removeUnsupportedPreferences();

        // Set build version of Contacts App.
        final PackageManager manager = getActivity().getPackageManager();
        try {
            final PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            final Preference versionPreference = findPreference(
                    getString(R.string.pref_build_version_key));
            versionPreference.setSummary(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            // Nothing
        }

        final Preference licensePreference = findPreference(
                getString(R.string.pref_open_source_licenses_key));
        licensePreference.setIntent(new Intent(getActivity(), LicenseActivity.class));
    }

    private void removeUnsupportedPreferences() {
        // Disable sort order for CJK locales where it is not supported
        final Resources resources = getResources();
        if (!resources.getBoolean(R.bool.config_sort_order_user_changeable)) {
            getPreferenceScreen().removePreference(findPreference("sortOrder"));
        }

        // Disable display order for CJK locales as well
        if (!resources.getBoolean(R.bool.config_display_order_user_changeable)) {
            getPreferenceScreen().removePreference(findPreference("displayOrder"));
        }

        // Remove the "Default account" setting if there aren't any writable accounts
        final AccountTypeManager accountTypeManager = AccountTypeManager.getInstance(getContext());
        final List<AccountWithDataSet> accounts = accountTypeManager.getAccounts(
                /* contactWritableOnly */ true);
        if (accounts.isEmpty()) {
            getPreferenceScreen().removePreference(findPreference("accounts"));
        }

        // Show Contact metadata sync option when 1) metadata sync is enabled
        // and 2) there is at least one focus google account
        boolean hasFocusGoogleAccount = false;
        for (AccountWithDataSet account : accounts) {
            if (GoogleAccountType.ACCOUNT_TYPE.equals(account.type) && account.dataSet == null) {
                hasFocusGoogleAccount = true;
                break;
            }
        }
        if (!hasFocusGoogleAccount
                || !MetadataSyncEnabledCompat.isMetadataSyncEnabled(getContext())) {
            getPreferenceScreen().removePreference(findPreference("contactMetadata"));
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}

