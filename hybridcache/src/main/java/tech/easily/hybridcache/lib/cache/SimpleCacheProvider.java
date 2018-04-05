package tech.easily.hybridcache.lib.cache;

import android.content.Context;
import android.text.TextUtils;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tech.easily.hybridcache.lib.utils.PackageInfoHelper;

/**
 * manage cache in local disk storage
 * <p>
 * Created by lemon on 06/01/2018.
 */

public class SimpleCacheProvider implements CacheProvider {

    private DiskLruCache diskLruCache;
    private CacheKeyProvider cacheKeyProvider = new MD5KeyProvider();

    private SimpleCacheProvider(Context context, File dir, long maxSize) throws IOException {
        diskLruCache = DiskLruCache.open(dir, PackageInfoHelper.getAppVersionCode(context), 1, maxSize);
    }

    public static synchronized SimpleCacheProvider open(Context context, File dir, long maxSize) throws IOException {
        return new SimpleCacheProvider(context, dir, maxSize);
    }

    @Override
    public InputStream get(String key) {
        if (diskLruCache == null || TextUtils.isEmpty(key)) {
            return null;
        }
        try {
            String cacheKey = cacheKeyProvider.buildCacheKey(key);
            DiskLruCache.Snapshot snapshot = diskLruCache.get(cacheKey);
            if (snapshot != null) {
                return snapshot.getInputStream(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void put(String key, File value, CacheLoadFinishListener listener) {
        if (diskLruCache == null || value == null || TextUtils.isEmpty(key)) {
            return;
        }
        DiskLruCache.Editor editor = null;
        try {
            String cacheKey = cacheKeyProvider.buildCacheKey(key);
            editor = diskLruCache.edit(cacheKey);
            if (editor != null) {
                OutputStream os = editor.newOutputStream(0);
                if (writeCache(value, os) && listener != null) {
                    listener.onFinished();
                }
                editor.commit();
            }
            diskLruCache.flush();
        } catch (IOException e) {
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    public boolean clear() {
        try {
            if (diskLruCache != null) {
                diskLruCache.delete();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean isCacheExists(String key) {
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = diskLruCache.get(cacheKeyProvider.buildCacheKey(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (snapshot == null) {
            return false;
        }
        snapshot.close();
        return true;
    }

    private boolean writeCache(File value, OutputStream outputStream) throws IOException {
        if (value == null || !value.exists() || outputStream == null) {
            return false;
        }
        byte[] data = new byte[4 * 1024];
        int length;
        InputStream inputStream = new FileInputStream(value);
        while ((length = inputStream.read(data)) != -1) {
            outputStream.write(data, 0, length);
        }
        inputStream.close();
        outputStream.close();
        return true;
    }

    @Override
    public boolean isClosed() {
        try {
            if (diskLruCache != null) {
                return diskLruCache.isClosed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void setCacheKeyProvider(CacheKeyProvider cacheKeyProvider) {
        this.cacheKeyProvider = cacheKeyProvider;
    }
}
