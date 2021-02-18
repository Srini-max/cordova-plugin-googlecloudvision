package com.cordova.darkmode;

import org.apache.cordova.*;
import org.json.JSONArray;

import android.util.Log;
import android.content.res.Configuration;
import android.provider.Settings;

public class OSTheme extends CordovaPlugin {
    String TAG = "DarkModePlugin";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        if (action.equals("darkmode")) {
            try {
                int a = cordova.getActivity().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

                switch (a) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        Log.d(TAG, "Dark Mode: off");
                        callbackContext.success("false");
                        break;
                        // Night mode is not active, we're in day time
                    case Configuration.UI_MODE_NIGHT_YES:
                        Log.d(TAG, "Dark Mode: on");
                        callbackContext.success("true");
                        break;
                        // Night mode is active, we're at night!
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        // We don't know what mode we're in, assume notnight
                        Log.d(TAG, "Dark Mode: undefined");
                        callbackContext.success("Not able to detect");
                        break;
                }
            } catch (Settings.SettingNotFoundException e) {
                Log.d(TAG, "Error finding setting night mode: " + e.getMessage());
                Log.d(TAG, "Checking dark/night mode enabled status");
                callbackContext.error("Error finding setting night mode: " + e.getMessage());
            }
            return true;

        } else {
            Log.d(TAG, "Wrong Function... nothing to detect");
            return false;
        }
    }
}
