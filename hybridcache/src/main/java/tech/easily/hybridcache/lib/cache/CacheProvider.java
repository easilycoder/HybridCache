package tech.easily.hybridcache.lib.cache;

import java.io.File;
import java.io.InputStream;

/**
 * implements the interface to manage the cache
 * <p>
 * Created by lemon on 06/01/2018.
 */

public interface CacheProvider {
    /**
     * get the cache stream
     *
     * @param key the cache key
     * @return the cache stream
     */
    InputStream get(String key);

    /**
     * write cache with the base key and cache
     *
     * @param key      the cache key
     * @param value    cache file
     * @param listener when store cache finish,it will be invoked
     */
    void put(String key, File value, CacheLoadFinishListener listener);


    boolean isClosed();
}
