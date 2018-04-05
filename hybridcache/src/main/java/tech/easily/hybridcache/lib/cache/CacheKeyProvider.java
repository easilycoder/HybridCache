package tech.easily.hybridcache.lib.cache;

/**
 * interface to generate the cache key,
 * implements it ,if flexible cache key strategy is needed.Simply using the default implementation {@link MD5KeyProvider}
 * <p>
 * Created by lemon on 06/01/2018.
 */

public interface CacheKeyProvider {

    /**
     * create a cache key
     *
     * @param baseKey usually it is the res's url
     * @return the real cache key String
     */
    String buildCacheKey(String baseKey);


}
