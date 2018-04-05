package tech.easily.hybridcache.lib.fresco;

import android.net.Uri;
import android.text.TextUtils;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.WriterCallbacks;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import tech.easily.hybridcache.lib.cache.CacheLoadFinishListener;
import tech.easily.hybridcache.lib.cache.CacheProvider;

/**
 * provide images cache with fresco
 * <p>
 * Created by lemon on 06/01/2018.
 */

public class FrescoImageProvider implements CacheProvider {

    private static class SingleTonHolder {
        private static FrescoImageProvider INSTANCE = new FrescoImageProvider();
    }

    public static FrescoImageProvider getInstance() {
        return SingleTonHolder.INSTANCE;
    }

    private FrescoCacheKeyProvider cachKeyProvider;

    private FrescoImageProvider() {
    }

    @Override
    public InputStream get(String key) {
        File file = getCachedImageOnDisk(key);
        if (file != null && file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void put(String key, File cacheFile, final CacheLoadFinishListener listener) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        boolean isAdded = false;
        if (cacheFile != null && cacheFile.exists()) {
            isAdded = addFileToLocalCache(key, cacheFile);
        }
        if (isAdded) {
            listener.onFinished();
            return;
        }
        prefetchToDiskCache(key, new ImageRequestListener() {

            @Override
            public void onStart(ImageRequest request) {
            }

            @Override
            public void onSuccess(ImageRequest request) {
                if (listener != null) {
                    listener.onFinished();
                }
            }

            @Override
            public void onFailed(ImageRequest request, Throwable throwable) {
                if (listener != null) {
                    listener.onFinished();
                }
            }

            @Override
            public void onCancel(String requestId) {
                if (listener != null) {
                    listener.onFinished();
                }
            }
        });
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    private boolean addFileToLocalCache(String requestUrl, File file) {
        CacheKey remoteUrlCacheKey = getCacheKey(requestUrl);
        InputStream localFileInputStream = null;
        try {
            localFileInputStream = new FileInputStream(file);
            BinaryResource resource = Fresco.getImagePipelineFactory().getMainFileCache().insert(remoteUrlCacheKey, WriterCallbacks.from(localFileInputStream));
            return resource != null && resource.size() > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (localFileInputStream != null) {
                    localFileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getCachedImageOnDisk(String url) {
        CacheKey key = getCacheKey(url);
        if (key == null) {
            return null;
        }

        File localFile = null;
        if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(key)) {
            BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(key);
            if (resource != null) {
                localFile = ((FileBinaryResource) resource).getFile();
            }
        } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(key)) {
            BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(key);
            if (resource != null) {
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }
        return localFile;
    }

    private DataSource<Void> prefetchToDiskCache(String url, ImageRequestListener listener) {
        if (!TextUtils.isEmpty(url)) {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url));
            if (listener != null) {
                builder.setRequestListener(new ImageRequestWrapper(listener));
            }
            return imagePipeline.prefetchToDiskCache(builder.build(), null);
        }
        if (listener != null) {
            listener.onFailed(null, new Exception("url is empty"));
        }

        return null;
    }

    private CacheKey getCacheKey(String url) {
        if (cachKeyProvider == null) {
            cachKeyProvider = new FrescoCacheKeyProvider() {
                @Override
                public CacheKey getCacheKey(String url) {
                    if (TextUtils.isEmpty(url)) {
                        return null;
                    }
                    return DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(Uri.parse(url)), null);
                }
            };
        }
        return cachKeyProvider.getCacheKey(url);
    }


    public void setCachKeyProvider(FrescoCacheKeyProvider cachKeyProvider) {
        this.cachKeyProvider = cachKeyProvider;
    }
}
