package tech.easily.hybridcache.lib;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import tech.easily.hybridcache.lib.cache.SimpleCacheProvider;
import tech.easily.hybridcache.lib.cache.CacheLoadFinishListener;
import tech.easily.hybridcache.lib.cache.CacheProvider;
import tech.easily.hybridcache.lib.utils.FileUtils;
import tech.easily.hybridcache.lib.utils.MD5;

import static tech.easily.hybridcache.lib.HttpConnectionDownloader.DEFAULT_CONNECT_TIMEOUT;
import static tech.easily.hybridcache.lib.HttpConnectionDownloader.DEFAULT_READ_TIMEOUT;

/**
 * share the common logic with other {@link WebResInterceptor},just like download the real resource ,manage cache
 * <p>
 * Created by lemon on 06/01/2018.
 */
public abstract class BaseInterceptor implements WebResInterceptor {

    public interface OnErrorListener {
        /**
         * meet an error when downloading the web resource
         *
         * @param requestUrl  the url of the web res
         * @param errorCode   error code
         * @param responseMsg error message
         */
        void onError(String requestUrl, int errorCode, String responseMsg);
    }

    private static final String HYBRID_CACHE_ROOT_DIR = "hybrid_cache/";
    private static final String WEB_CACHE_TEMP_DIR = HYBRID_CACHE_ROOT_DIR + "temp";
    private static final String RES_TYPE_HTML = "text/html";
    private static final String CHARSET_ENCODE = "UTF-8";
    private static final long DEFAULT_CACHE_DISK_SIZE = 10 * 1024 * 1024;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CacheProvider cacheProvider;
    private String tempDir;
    private OnErrorListener onErrorListener;

    public BaseInterceptor(@NonNull Context context) {
        this(context, null);
    }

    public BaseInterceptor(@NonNull Context context, CacheProvider cacheProvider) {
        this.tempDir = FileUtils.getLibraryDiskDir(context, WEB_CACHE_TEMP_DIR);
        this.cacheProvider = cacheProvider;
        if (cacheProvider == null) {
            initDefaultDiskCacheProvider(context, HYBRID_CACHE_ROOT_DIR + getDefaultFileCacheDirName(), getDefaultCacheDiskSize());
        }
    }

    protected String getDefaultFileCacheDirName() {
        return "";
    }

    protected long getDefaultCacheDiskSize() {
        return DEFAULT_CACHE_DISK_SIZE;
    }

    protected int getResReadTimeout() {
        return DEFAULT_READ_TIMEOUT;
    }

    protected int getResConnectTimeout() {
        return DEFAULT_CONNECT_TIMEOUT;
    }

    protected abstract boolean isIntercept(String url, Map<String, String> headers);

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    @Override
    public WebResourceResponse intercept(Chain chain) {
        final String url = chain.getRequestUrl();
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        InputStream inputStream = null;
        // if the current interceptor doesn't intercept the current request,pass to the next interceptor
        if (!isIntercept(url, chain.getRequestHeaders())) {
            return chain.proceed(url, chain.getRequestHeaders());
        }
        if (cacheProvider != null && !cacheProvider.isClosed()) {
            inputStream = cacheProvider.get(url);
        }
        // if cache existed,we return the cache directly
        // in this case,the other interceptors had no opportunity to run
        if (inputStream != null) {
            return new WebResourceResponse(mimeType, "UTF-8", inputStream);
        }
        // init the downloader to download the res
        HttpConnectionDownloader downloader = HttpConnectionDownloader.getInstance();
        downloader.setConnectTimeout(getResConnectTimeout());
        downloader.setReadTimeout(getResReadTimeout());
        HttpConnectionDownloader.DownloadResult result = downloader.downloadRes(chain.getRequestUrl(), chain.getRequestHeaders());
        // it seems that something wrong happened as the response code>=400
        if (result != null && result.responseCode >= 400 && onErrorListener != null) {
            dispatchError(url, result.responseCode, result.responseMsg);
        }
        WebResourceResponse response = buildWebResponse(result);
        // use a Proxy mode,wrap the source stream with a FileWriter,so that we can store the resource in the cache as long as the stream is read
        if (response != null && response.getData() != null) {
            String tempPath = String.format("%s%s%s", tempDir, File.separator, MD5.md5(url));
            TmpFileWriteListener listener = new TmpFileWriteListener(cacheProvider, url);
            InputStream wrapperInputStream = new WebResInputStreamWrapper(response.getData(), new TempFileWriter(tempPath, listener));
            response.setData(wrapperInputStream);
        }
        return response;

    }

    private void initDefaultDiskCacheProvider(Context context, String name, long size) {
        String path = FileUtils.getLibraryDiskDir(context, name);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File cacheDir = new File(path);
        //noinspection ResultOfMethodCallIgnored
        cacheDir.mkdirs();
        try {
            cacheProvider = SimpleCacheProvider.open(context, cacheDir, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WebResourceResponse buildWebResponse(HttpConnectionDownloader.DownloadResult result) {
        if (result == null) {
            return null;
        }
        /*
          the return string of {@link tech.easily.hybridcache.lib.HttpConnectionDownloader.DownloadResult#contentType} can be "ext/html;charset=utf-8"
          in this case directly using this return value can not be recognized as a webpage by{@link WebView}
         */
        if (result.contentType != null && result.contentType.toLowerCase().contains("html")) {
            result.contentType = RES_TYPE_HTML;
            result.contentEncoding = CHARSET_ENCODE;
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? new WebResourceResponse(result.contentType, result.contentEncoding, result.responseCode, result.responseMsg, result.responseHeaders, result.inputStream) : new WebResourceResponse(result.contentType, "UTF-8", result.inputStream);
    }

    private void dispatchError(final String url, final int code, final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onErrorListener.onError(url, code, message);
            }
        });
    }

    private class TmpFileWriteListener implements TempFileWriter.WriterCallback {
        private CacheProvider mDiskCache;
        private String remoteURL;

        TmpFileWriteListener(CacheProvider diskCache, String netUrl) {
            this.mDiskCache = diskCache;
            this.remoteURL = netUrl;
        }

        @Override
        public void onSuccess(final String filePath) {
            try {
                final File localCache = new File(filePath);
                mDiskCache.put(remoteURL, localCache, new CacheLoadFinishListener() {
                    @Override
                    public void onFinished() {
                        //noinspection ResultOfMethodCallIgnored
                        localCache.delete();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailed(String filePath) {
        }
    }
}
