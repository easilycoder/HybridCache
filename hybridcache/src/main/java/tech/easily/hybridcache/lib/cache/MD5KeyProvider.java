package tech.easily.hybridcache.lib.cache;

import tech.easily.hybridcache.lib.utils.MD5;

/**
 * use the MD5 algorithm to create the cache key
 * Created by lemon on 06/01/2018.
 */

public class MD5KeyProvider implements CacheKeyProvider {
    @Override
    public String buildCacheKey(String baseKey) {
        return MD5.md5(baseKey);
    }
}
