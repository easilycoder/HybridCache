package tech.easily.hybridcache.lib.fresco;

import com.facebook.cache.common.CacheKey;

/**
 * using to define custom CacheKey strategy
 * <p>
 * Created by lemon on 2018/4/4.
 */
public interface FrescoCacheKeyProvider {

    CacheKey getCacheKey(String url);
}
