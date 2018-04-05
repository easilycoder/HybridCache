package tech.easily.hybridcache

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_webview.*
import tech.easily.hybridcache.lib.ImageInterceptor
import tech.easily.hybridcache.lib.fresco.FrescoImageProvider


/**
 * Created by lemon on 2018/4/4.
 */
class FrescoCacheActivity : WebActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addInterceptor(ImageInterceptor(this,FrescoImageProvider.getInstance()))
        webView.loadUrl(loadPage)
    }
}