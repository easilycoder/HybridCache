package tech.easily.hybridcache.lib.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

/**
 * Created by lemon on 06/01/2018.
 */

public class PackageInfoHelper {

    public static int getAppVersionCode(@NonNull Context context) {
        String packageName = context.getPackageName();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            return 0;
        }
    }
}
