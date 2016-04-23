package org.geekcircle.ffmpeggo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by leo on 16-4-22.
 *
 * Ref. http://stackoverflow.com/questions/21996070/android-ffmpeg-best-approach
 *
 */
public class Installer {
    private static final String TAG = "INSTALLER";
    private static final int IO_BUFFER_SIZE = 4096;

    public static void installBinaryFromRaw(Context context, int resId, File file) {
        final InputStream rawStream = context.getResources().openRawResource(resId);
        if (rawStream == null) {
            throw new RuntimeException("Failed to open resouce stream");
        }

        final OutputStream binStream = getFileOutputStream(file);

            pipeStreams(rawStream, binStream);
            try {
                rawStream.close();
                binStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close streams!", e);
                throw new RuntimeException("Failed to close stream: " + e.getMessage());
            }

            doChmod(file, 777);
    }

    public static OutputStream getFileOutputStream(File file) {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found attempting to stream file.", e);
            throw new RuntimeException("Failed to open output file " + file.toString()
                + ": " + e.getMessage());
        }
    }

    public static void pipeStreams(InputStream is, OutputStream os) {
        byte[] buffer = new byte[IO_BUFFER_SIZE];
        int count;
        try {
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error writing stream.", e);
            throw new RuntimeException("Error pipe stream: " + e.getMessage());
        }
    }

    public static void doChmod(File file, int chmodValue) {
        final StringBuilder sb = new StringBuilder();
        sb.append("chmod");
        sb.append(' ');
        sb.append(chmodValue);
        sb.append(' ');
        sb.append(file.getAbsolutePath());

        try {
            Runtime.getRuntime().exec(sb.toString());
        } catch (IOException e) {
            Log.e(TAG, "Error performing chmod", e);
            throw new RuntimeException("Error changing file mode: " + e.getMessage());
        }
    }
}
