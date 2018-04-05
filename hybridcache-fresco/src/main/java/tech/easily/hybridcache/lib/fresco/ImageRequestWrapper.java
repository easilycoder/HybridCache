package tech.easily.hybridcache.lib.fresco;

import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.request.ImageRequest;

import java.util.Map;

/**
 * Created by lemon on 06/01/2018.
 */

final class ImageRequestWrapper implements RequestListener {

    private ImageRequestListener srcListener;

    public ImageRequestWrapper(ImageRequestListener listener) {
        this.srcListener = listener;
    }

    @Override
    public void onRequestStart(ImageRequest request, Object callerContext, String requestId, boolean isPrefetch) {
        if (srcListener != null) {
            srcListener.onStart(request);
        }
    }

    @Override
    public void onRequestSuccess(ImageRequest request, String requestId, boolean isPrefetch) {
        if (srcListener != null) {
            srcListener.onSuccess(request);
        }
    }

    @Override
    public void onRequestFailure(ImageRequest request, String requestId, Throwable throwable, boolean isPrefetch) {
        if (srcListener != null) {
            srcListener.onFailed(request, throwable);
        }

    }

    @Override
    public void onRequestCancellation(String requestId) {
        if (srcListener != null) {
            srcListener.onCancel(requestId);
        }
    }

    @Override
    public void onProducerStart(String requestId, String producerName) {

    }

    @Override
    public void onProducerEvent(String requestId, String producerName, String eventName) {

    }

    @Override
    public void onProducerFinishWithSuccess(String requestId, String producerName, Map<String, String> extraMap) {

    }

    @Override
    public void onProducerFinishWithFailure(String requestId, String producerName, Throwable t, Map<String, String> extraMap) {

    }

    @Override
    public void onProducerFinishWithCancellation(String requestId, String producerName, Map<String, String> extraMap) {

    }

    @Override
    public void onUltimateProducerReached(String requestId, String producerName, boolean successful) {

    }

    @Override
    public boolean requiresExtraMap(String requestId) {
        return false;
    }
}
