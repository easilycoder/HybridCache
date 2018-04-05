package tech.easily.hybridcache

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.*
import android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
import kotlinx.android.synthetic.main.activity_webview.*
import tech.easily.hybridcache.lib.BaseInterceptor
import tech.easily.hybridcache.lib.HybridCacheManager

/**
 * Created by lemon on 2018/4/4.
 */
abstract class WebActivity : AppCompatActivity() {

    protected val loadPage = "https://m.mei.163.com"
    private val tag = "HybridCache"
    private val hybridCacheManager: HybridCacheManager = HybridCacheManager.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initView()
    }

    private fun initView() {
        val cachePath=externalCacheDir.absolutePath+"app_cache/"
        webView.settings.setAppCachePath(cachePath)
        webView.settings.setAppCacheEnabled(true)
        webView.settings.cacheMode=WebSettings.LOAD_DEFAULT

        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = MIXED_CONTENT_COMPATIBILITY_MODE
        }
        webView.webViewClient = object : WebViewClient() {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val response = hybridCacheManager.interceptWebResRequest(request)
                if (response != null) {
                    Log.d(tag, "request with url:${request?.url.toString()} hit cache")
                }
                return response
            }

            override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
                val response = hybridCacheManager.interceptWebResRequest(url)
                if (response != null) {
                    Log.d(tag, "request with url:$url hit cache")
                }
                return response
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
        }
    }

    protected fun addInterceptor(interceptor: BaseInterceptor) {
        hybridCacheManager.addCacheInterceptor(interceptor)
    }

}