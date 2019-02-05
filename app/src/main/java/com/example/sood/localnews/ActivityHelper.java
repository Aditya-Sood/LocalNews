package com.example.sood.localnews;

import android.app.Activity;
import android.content.pm.ActivityInfo;

/**
 * Created by sood on 1/31/19.
 *
 * Helper class to fix screen orientation on creation
 */

public class ActivityHelper {

    public static void initialize(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
