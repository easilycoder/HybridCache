package tech.easily.hybridcache.lib;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * the core object for using the hybridcache.
 * if you want to share the web res cache between native and webview,you should follow like this:
 * <p>
 * 1. Build your instance of {@link BaseInterceptor} to intercept the res you care.
 * you should define your {@link tech.easily.hybridcache.lib.cache.CacheProvider} if you want a different cache strategy from {@link tech.easily.hybridcache.lib.cache.SimpleCacheProvider}
 * 2. add the interceptor using {@link #addCacheInterceptor(BaseInterceptor)}
 * 3. intercept the request using {@link #interceptWebResRequest(String)} or {@link #interceptWebResRequest(WebResourceRequest)}
 * <p>
 * Created by lemon on 2018/4/4.
 */
public class HybridCacheManager {

    private List<BaseInterceptor> cacheInterceptors = new ArrayList<>();

    public static HybridCacheManager newInstance() {
        return new HybridCacheManager();
    }


    /**
     * call this method in {@link android.webkit.WebViewClient#shouldInterceptRequest(WebView, WebResourceRequest)}
     *
     * @param request the web res request
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse interceptWebResRequest(WebResourceRequest request) {
        String url = request.getUrl().toString();
        Map<String, String> headers = request.getRequestHeaders();
        return interceptWebResRequest(url, headers);
    }

    /**
     * call this method in {@link android.webkit.WebViewClient#shouldInterceptRequest(WebView, String)}
     *
     * @param url the web res url
     * @return
     */
    public WebResourceResponse interceptWebResRequest(String url) {
        return interceptWebResRequest(url, null);
    }

    private WebResourceResponse interceptWebResRequest(String requestUrl, Map<String, String> requestHeaders) {
        if (cacheInterceptors.isEmpty()) {
            return null;
        }
        BaseInterceptor.Chain chain = new DefaultInterceptorChain(cacheInterceptors, requestUrl, requestHeaders, 0);
        return chain.proceed(requestUrl, requestHeaders);
    }

    /**
     * notice that:if someone interceptor at the front intercept the request first,
     * the others interceptor may had no opportunity to take charge of the web res request.
     * So it's your response to make sure that never add two interceptors which will intercept the same type res request,
     * if not,something strange may happen
     *
     * @param interceptor the web res interceptor
     */
    public void addCacheInterceptor(BaseInterceptor interceptor) {
        if (interceptor != null) {
            cacheInterceptors.add(interceptor);
        }
    }
}
