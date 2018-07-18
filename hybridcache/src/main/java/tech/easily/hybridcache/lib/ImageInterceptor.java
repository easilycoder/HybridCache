package tech.easily.hybridcache.lib;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.MimeTypeFilter;
import android.text.TextUtils;
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

    private static final String ACCEPT_HEADER = "Accept";

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
        // first ,we try to get file extension from the url
        String extension = getFileExtensionFromUrl(url);
        if (INTERCEPT_URL_EXTENSIONS.contains(extension)) {
            return true;
        }
        // then,we try to get the res type according to the request header
        if (headers != null && headers.keySet().contains(ACCEPT_HEADER)) {
            String acceptResType = headers.get(ACCEPT_HEADER);
            return acceptResType != null && acceptResType.startsWith("image");
        }
        return false;
    }

    private String getFileExtensionFromUrl(String url) {
        String extension = "";
        try {
            Uri uri = Uri.parse(url);
            String lastPath = uri.getLastPathSegment();
            if (!TextUtils.isEmpty(lastPath)) {
                int lastDotPosition = lastPath.lastIndexOf('.');
                if (lastDotPosition >= 0) {
                    extension = lastPath.substring(lastDotPosition + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extension;
    }


    @Override
    protected String getDefaultFileCacheDirName() {
        return "image";
    }
}
