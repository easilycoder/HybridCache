package tech.easily.hybridcache.lib;

import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by lemon on 06/01/2018.
 */

final class TempFileWriter {

    interface WriterCallback {
        void onSuccess(String filePath);

        void onFailed(String filePath);
    }

    private WriterCallback callback;

    private OutputStream outputStream;

    private String outputFilePath;

    private boolean isFinished;

    TempFileWriter(String outputFilePath, WriterCallback writerCallback) {
        this.outputFilePath = outputFilePath;
        this.callback = writerCallback;
        initOutputStream(outputFilePath);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initOutputStream(String outFilePath) {
        try {
            if (outputStream == null && !TextUtils.isEmpty(outFilePath)) {
                File file = new File(outFilePath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                outputStream = new FileOutputStream(outFilePath);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void write(byte[] buffer, int byteOffset, int realCount) {
        if (outputStream != null) {
            try {
                if (realCount > 0) {
                    outputStream.write(buffer, byteOffset, realCount);
                } else {
                    isFinished = true;
                }
            } catch (IOException e) {
                safeClose(outputStream);
                outputStream = null;
                if (callback != null) {
                    callback.onFailed(outputFilePath);
                }
            }
        }
    }

    void close() {
        if (outputStream == null) {
            return;
        }
        if (callback != null) {
            if (isFinished) {
                callback.onSuccess(outputFilePath);
            } else {
                callback.onFailed(outputFilePath);
            }
        }
        safeClose(outputStream);
    }

    private void safeClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
