package tech.easily.hybridcache.lib;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * download the real resource with {@link HttpURLConnection}
 * <p>
 * Created by lemon on 06/01/2018.
 */

final class HttpConnectionDownloader {

    public static class DownloadResult {
        public int responseCode;
        public String responseMsg;
        public InputStream inputStream;
        public Map<String, String> responseHeaders ;
        public String contentEncoding;
        public String contentType;

        DownloadResult(int responseCode, String responseMsg, InputStream inputStream, Map<String, String> responseHeaders, String contentEncoding, String contentType) {
            this.responseCode = responseCode;
            this.responseMsg = responseMsg;
            this.inputStream = inputStream;
            this.responseHeaders = responseHeaders;
            this.contentEncoding = contentEncoding;
            this.contentType = contentType;
        }
    }

    private static class SingleTonHolder {
        private static HttpConnectionDownloader INSTANCE = new HttpConnectionDownloader();
    }

    static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000;
    static final int DEFAULT_READ_TIMEOUT = 60 * 1000;
    private static final String REQUEST_METHOD = "GET";
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    public static HttpConnectionDownloader getInstance() {
        return SingleTonHolder.INSTANCE;
    }

    private HttpConnectionDownloader() {

    }

    public DownloadResult downloadRes(String requestUrl, Map<String, String> headers) {
        if (TextUtils.isEmpty(requestUrl) || !Patterns.WEB_URL.matcher(requestUrl).matches()) {
            return null;
        }
        String currentUrl = requestUrl;
        String currentScheme = Uri.parse(requestUrl).getScheme();
        InputStream inputStream = null;
        String responseMsg = null;
        int responseCode = HttpURLConnection.HTTP_OK;
        HttpURLConnection connection = null;
        while (true) {
            String nextURL;
            String nextScheme;
            try {
                URL httpUrl = new URL(currentUrl);
                connection = (HttpURLConnection) httpUrl.openConnection();
                connection.setRequestMethod(REQUEST_METHOD);
                if (headers != null) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setConnectTimeout(connectTimeout);
                connection.setReadTimeout(readTimeout);
                nextURL = connection.getHeaderField("Location");
                nextScheme = (nextURL == null) ? null : Uri.parse(nextURL).getScheme();
                // handle the webpage redirect situation
                if (nextURL == null || (nextScheme != null && nextScheme.equals(currentScheme))) {
                    responseCode = connection.getResponseCode();
                    responseMsg = connection.getResponseMessage();
                    inputStream = connection.getInputStream();
                    break;
                }
                currentUrl = nextURL;
                currentScheme = nextScheme;
            } catch (Exception e) {
                e.printStackTrace();
                inputStream = null;
                break;
            }
        }
        Map<String, String> responseHeaders = new HashMap<>();
        String encoding = "UTF-8";
        String contentType = null;
        if (connection != null) {
            if (connection.getContentEncoding() != null) {
                encoding = connection.getContentEncoding();
            }
            contentType = connection.getContentType();
            Map<String, List<String>> map = connection.getHeaderFields();
            if (map != null) {
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    List<String> value = entry.getValue();
                    if (!value.isEmpty()) {
                        responseHeaders.put(entry.getKey(), value.get(0));
                    }
                }
            }
        }
        return new DownloadResult(responseCode, responseMsg, inputStream, responseHeaders, encoding, contentType);
    }

    void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
