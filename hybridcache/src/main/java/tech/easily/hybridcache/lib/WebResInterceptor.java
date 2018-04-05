package tech.easily.hybridcache.lib;

import android.webkit.WebResourceResponse;

import java.util.Map;

/**
 * using the interceptor to handle the {@link android.webkit.WebView}'s res-file loading process
 * when we call {@link android.webkit.WebView#loadUrl(String)} ,the WebView will try to load all the resource for rendering the html file,
 * just like:css files,js files,images and even the other html files.
 * <p>
 * As for this ,we can intercept the process to do something interesting ,just like caching the resource files
 * <p>
 * Created by lemon on 06/01/2018.
 */

interface WebResInterceptor {

    interface Chain {

        /**
         * the request url we intercept
         *
         * @return
         */
        String getRequestUrl();

        /**
         * @return the request headers
         */
        Map<String, String> getRequestHeaders();

        /**
         * add our intercept logic here
         *
         * @param url
         * @param headers
         * @return
         */
        WebResourceResponse proceed(String url, Map<String, String> headers);
    }

    /**
     * @param chain
     * @return
     */
    WebResourceResponse intercept(Chain chain);

}
