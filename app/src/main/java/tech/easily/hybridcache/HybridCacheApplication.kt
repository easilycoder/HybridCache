package tech.easily.hybridcache

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

/**
 * Created by hzyangjiehao on 2018/4/4.
 */
class HybridCacheApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }
}