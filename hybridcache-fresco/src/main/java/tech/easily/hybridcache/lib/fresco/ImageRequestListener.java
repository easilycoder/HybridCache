package tech.easily.hybridcache.lib.fresco;

import com.facebook.imagepipeline.request.ImageRequest;

/**
 * Created by lemon on 06/01/2018.
 */

interface ImageRequestListener {

    void onStart(ImageRequest request);

    void onSuccess(ImageRequest request);

    void onFailed(ImageRequest request, Throwable throwable);

    void onCancel(String requestId);
}
