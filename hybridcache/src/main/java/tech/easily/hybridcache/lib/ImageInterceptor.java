package tech.easily.hybridcache.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tech.easily.hybridcache.lib.cache.CacheProvider;

/**
 * intercept image web res request
 * <p>
 * Created by lemon on 06/01/2018.
 */
public class ImageInterceptor extends BaseInterceptor {

    private static final List<String> INTERCEPT_URL_EXTENSIONS = new ArrayList<String>() {
        {
            add("png");
            add("jpg");
            add("jpeg");
            add("webp");
            add("bmp");
            add("gif");
        }
    };

    public ImageInterceptor(@NonNull Context context) {
        super(context);
    }

    public ImageInterceptor(@NonNull Context context, CacheProvider cacheProvider) {
        super(context, cacheProvider);
    }

    @Override
    protected long getDefaultCacheDiskSize() {
        return 50 * 1024 * 1024L;
    }

    @Override
    protected boolean isIntercept(String url, Map<String, String> headers) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return INTERCEPT_URL_EXTENSIONS.contains(extension);
    }

    @Override
    protected String getDefaultFileCacheDirName() {
        return "image";
    }
}
