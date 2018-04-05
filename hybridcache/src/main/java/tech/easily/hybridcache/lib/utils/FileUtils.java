package tech.easily.hybridcache.lib.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by lemon on 06/01/2018.
 */

public class FileUtils {

    public static String getLibraryDiskDir(Context context, String dirName) {
        String tempPath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            File cacheFile = context.getExternalCacheDir();
            if (cacheFile != null) {
                tempPath = cacheFile.getPath();
            }
        }
        if (tempPath == null) {
            File cacheFile = context.getCacheDir();
            if (cacheFile != null) {
                tempPath = cacheFile.getPath();
            }
        }
        return tempPath != null ? String.format("%s%s%s", tempPath, File.separator, dirName) : null;
    }

}
