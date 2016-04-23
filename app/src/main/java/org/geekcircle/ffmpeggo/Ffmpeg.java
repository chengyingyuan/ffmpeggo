package org.geekcircle.ffmpeggo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by leo on 16-4-22.
 */
public class Ffmpeg {
    private static final String TAG = "FFMEPG";
    private static final String FFMPEG = "ffmpegarmv7a";
    private static final int FFMPEG_RESID = R.raw.ffmpegarmv7a;

    private Context context;
    private String path;
    private int resId;
    private String mediaPath;
    private String targetPath;

    public Ffmpeg(Context context) {
        this.context = context;
        this.path = new File(context.getCacheDir(),FFMPEG).toString();
        this.resId = FFMPEG_RESID;
        this.mediaPath = null;
    }

    public void install() {
        File ffmpegFile = new File(this.path);
        if (!ffmpegFile.exists()) {
            try {
                ffmpegFile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "Failed to create new file!", e);
                throw new RuntimeException("Failed to create ffmpeg file: " + e.getMessage());
            }
            Installer.installBinaryFromRaw(context, R.raw.ffmpegarmv7a, ffmpegFile);

        } else {
            Log.d(TAG, "ffmpeg was installed");
        }

        ffmpegFile.setExecutable(true);
        // Seems OK
        Log.d(TAG, "ffmpeg install path is " + this.path);
    }

    public String about() {
        StringBuilder sb = new StringBuilder(this.path);
        sb.append(" --help");
        return sb.toString();
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }
    public String getMediaPath() { return this.mediaPath; }
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
    public String getTargetPath() { return this.targetPath; }

    public String clip(int startsec, int durationsec) {
        StringBuilder sb = new StringBuilder(this.path);
        sb.append(" -i " + mediaPath);
        sb.append(" -ss " + startsec);
        sb.append(" -t " + durationsec);
        sb.append(" -strict -2 " + targetPath);
        return sb.toString();
    }
}
