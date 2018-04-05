package tech.easily.hybridcache.lib;

import android.webkit.WebResourceResponse;

import java.util.List;
import java.util.Map;

/**
 * a interceptor chain ,just like the okhttp we use
 * <p>
 * Created by lemon on 06/01/2018.
 */

final class DefaultInterceptorChain implements WebResInterceptor.Chain {

    private List<BaseInterceptor> interceptors;
    private String requestUrl;
    private Map<String, String> requestHeaders;
    private int index;

    public DefaultInterceptorChain(List<BaseInterceptor> interceptors, String requestUrl, Map<String, String> requestHeaders, int index) {
        this.interceptors = interceptors;
        this.requestUrl = requestUrl;
        this.requestHeaders = requestHeaders;
        this.index = index;
    }

    @Override
    public String getRequestUrl() {
        return requestUrl;
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public WebResourceResponse proceed(String url, Map<String, String> headers) {
        if (index >= interceptors.size()) {
            return null;
        }
        // Call the next interceptor in the chain.
        DefaultInterceptorChain next = new DefaultInterceptorChain(interceptors, requestUrl, requestHeaders, index + 1);
        BaseInterceptor interceptor = interceptors.get(index);
        return interceptor.intercept(next);
    }
}
