package org.geekcircle.ffmpeggo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG="FFMPEGGO";
    public static final String PREF_MEDIAPATH = "mediaPath";
    public static final int CODE_CHANGEMEDIA = 1;
    public static final int CODE_CLIPMEDIA = 2;
    private static final Command[] COMMANDS = new Command[]{Command.CHANGE_MEDIA,
            Command.PLAY,Command.CLIP,Command.ABOUT};
    private ListView listView;
    private Ffmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        listView = (ListView)findViewById(R.id.listView);
        ArrayList<Map<String,String>> data = new ArrayList<>();
        for (Command cmd:COMMANDS) {
            HashMap<String,String> entry = new HashMap<>();
            entry.put("cmd", cmd.desc);
            data.add(entry);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,data,android.R.layout.simple_list_item_1,
                new String[]{"cmd"}, new int[]{android.R.id.text1});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Command cmd = COMMANDS[pos];
                switch (cmd) {
                    case ABOUT: executeAbout();break;
                    case CHANGE_MEDIA: executeChangeMedia();break;
                    case PLAY: executePlay();break;
                    case CLIP: executeClip();break;
                }
            }
        });

        ffmpeg = new Ffmpeg(this);
        try {
            ffmpeg.install();
        } catch(Exception e) {
            showAlertMessage("ffmpeg installer error", e.getMessage());
            finish();
        }
        ffmpeg.setMediaPath(prefs.getString(PREF_MEDIAPATH, null));
        updateTitle();
    }

    void showAlertMessage(String title, String message) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               switch(which) {
                   case DialogInterface.BUTTON_POSITIVE:
                   case DialogInterface.BUTTON_NEGATIVE:
                       break;
               }
            }
        };
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .create()
                .show();
    }

    void executeAbout() {
        try {
            String response = Executor.runCommand(ffmpeg.about());
            showAlertMessage("ffmpeg about", response);

        } catch (Exception e) {
            showAlertMessage("ffmpeg about error", e.getMessage());
        }
    }

    void executePlay() {
        if (ffmpeg.getMediaPath() == null) {
            showAlertMessage("play media error", "Please set media file first!");
            return;
        }
        playMedia(ffmpeg.getMediaPath());
    }

    void playMedia(String path) {
        String uriString = "file://" + path;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(uriString), "video/*");
        startActivity(intent);
    }

    void executeChangeMedia() {
        Intent intent = new Intent(this, FileExplorerActivity.class);
        intent.putExtra("mediaPath", ffmpeg.getMediaPath());
        startActivityForResult(intent,CODE_CHANGEMEDIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode==CODE_CHANGEMEDIA && resultCode== Activity.RESULT_OK) {
            String path = intent.getStringExtra("mediaPath");
            ffmpeg.setMediaPath(path);
            updateTitle();

            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_MEDIAPATH, path);
            editor.commit();
        } else if (requestCode==CODE_CLIPMEDIA && resultCode== Activity.RESULT_OK) {
            int code = intent.getIntExtra("code", -1);
            if (code == 0) {
                playMedia(ffmpeg.getTargetPath());

            } else {
                showAlertMessage("ffmpeg clip error", "Clip request returned " + code);
            }
        }
    }

    void updateTitle() {
        String path = ffmpeg.getMediaPath();
        if (path != null) {
            this.setTitle("FFMPEGGo - " + new File(path).getName());
        } else {
            this.setTitle("FFMPEGGO - <Media file not set>");
        }
    }

    String generateTargetPath() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String filename = TAG + df.format(new Date()) + ".mp4";
        return dir+"/"+filename;
    }

    void executeClip() {
        if (ffmpeg.getMediaPath()==null) {
            showAlertMessage("play media error", "Please set media file first!");
            return;
        }
        ffmpeg.setTargetPath(generateTargetPath());
        String command = ffmpeg.clip(0, 10);
        Intent intent = new Intent(this, ExecutorActivity.class);
        intent.putExtra("command", command);
        startActivityForResult(intent,CODE_CLIPMEDIA);
    }

    /* A helper class to bind command id to its name */
    private enum Command {
        CHANGE_MEDIA("Set media file"),
        PLAY("Play media file"),
        CLIP("Clip media file"),
        ABOUT("About ffmpeg");

        private String desc;
        Command(String desc) {
            this.desc = desc;
        }
        public String getDesc() {
            return desc;
        }
    }
}
