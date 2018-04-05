package tech.easily.hybridcache.lib;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * wrap the resource stream ,so we can cached it when it is read by the system
 * <p>
 * Created by lemon on 06/01/2018.
 */

final class WebResInputStreamWrapper extends InputStream {

    private InputStream srcInputStream;
    private TempFileWriter tempFileWriter;


    WebResInputStreamWrapper(InputStream srcInputStream, TempFileWriter tempFileWriter) {
        this.srcInputStream = srcInputStream;
        this.tempFileWriter = tempFileWriter;
    }

    @Override
    public int available() throws IOException {
        return srcInputStream.available();
    }

    @Override
    public void close() throws IOException {
        super.close();
        tempFileWriter.close();
        srcInputStream.close();
    }

    @Override
    public void mark(int readLimit) {
        super.mark(readLimit);
        srcInputStream.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        return srcInputStream.markSupported();
    }

    @Override
    public int read(@NonNull byte[] buffer) throws IOException {
        int count = srcInputStream.read(buffer);
        tempFileWriter.write(buffer, 0, count);
        return count;
    }

    @Override
    public int read(@NonNull byte[] buffer, int byteOffset, int byteCount) throws IOException {
        int count = srcInputStream.read(buffer, byteOffset, byteCount);
        tempFileWriter.write(buffer, byteOffset, count);
        return count;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        srcInputStream.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        srcInputStream.skip(byteCount);
        return super.skip(byteCount);
    }

    @Override
    public int read() throws IOException {
        return srcInputStream.read();
    }
}
