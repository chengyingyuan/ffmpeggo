package org.geekcircle.ffmpeggo;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by leo on 16-4-22.
 *
 * Ref. https://www.learn2crack.com/2014/03/android-executing-shell-commands.html
 * Ref. http://stackoverflow.com/questions/6882248/running-shell-commands-though-java-code-on-android
 *
 */
public class Executor extends AsyncTask<String,String,Integer> {
    private static final int BUFFERSIZE = 128;
    private Context contex;
    private TextView textView;
    private boolean stop;  // Triggered by caller, when termination required
    private boolean done; // Set by task, when job is done
    private int code;

    public Executor(Context contex, TextView textView) {
        this.contex = contex;
        this.textView = textView;
        this.stop = false;
        this.done = false;
        this.code = 0;
    }

    public boolean isDone() { return done; }
    public boolean isStop() { return stop; }
    public int getCode() { return code; }
    public void term() {
        this.stop = true;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Integer doInBackground(String... cmds) {
        final String command = cmds[0];
        byte[] buffer = new byte[BUFFERSIZE];
        byte[] ebuffer = new byte[BUFFERSIZE];
        int nbytes = 0;
        int nebytes = 0;
        try {
            publishProgress("Executing " + command + "\n");
            Process p = Runtime.getRuntime().exec(command);
            InputStream is = p.getInputStream();
            InputStream es = p.getErrorStream();
            while (!this.stop) {
                while(is.available() > 0) {
                    int val = is.read();
                    if (val < 0) { // End of file
                        break;
                    }
                    buffer[nbytes++] = (byte)val;
                    if (val == '\n' || nbytes >= BUFFERSIZE) {
                        publishProgress(new String(buffer,0,nbytes));
                        nbytes = 0;
                    }
                }
                while(es.available() > 0) {
                    int val = es.read();
                    if (val < 0) { // End of file
                        break;
                    }
                    ebuffer[nebytes++] = (byte)val;
                    if (val == '\n' || nebytes >= BUFFERSIZE) {
                        publishProgress(new String(ebuffer,0,nebytes));
                        nebytes = 0;
                    }
                }

                try { // Is process still alive?
                    p.exitValue();
                    break;

                }catch(IllegalThreadStateException e) { // YES
                    Thread.sleep(50);
                }
            }
            // Stopped or no more output from process
            if (nbytes>0) {
                publishProgress(new String(buffer,0,nbytes));
            }
            if (nebytes>0) {
                publishProgress(new String(ebuffer,0,nebytes));
            }
            if (this.stop) { // Process is still running
                p.destroy();
            }
            this.code = p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            this.code = -1;
            //throw new RuntimeException("Error execute " + command + ": " + e.getMessage());
        }

        return this.code;
    }

    @Override
    protected void onProgressUpdate(String... msgs) {
        this.textView.append(msgs[0]);
        scrollTextView();
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (this.stop) {
            this.textView.append("Process stopped by user!\n");

        } else {
            this.textView.append("Process exited with " + result);
        }
        scrollTextView();
        this.done = true;
    }

    void scrollTextView() {
        // find the amount we need to scroll.  This works by
        // asking the TextView's internal layout for the position
        // of the final line and then subtracting the TextView's height
        textView.setFocusable(true);
        if (textView.getLineCount() > 5) { // Scroll is needed for more than 5 lines
            final int scrollAmount = textView.getLineCount()*textView.getLineHeight()-(textView.getBottom()-textView.getTop());
            textView.scrollTo(0, scrollAmount + textView.getLineHeight());
        }
    }

    public static String runCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error execute " + command + ": " + e.getMessage());
        }
        String response = output.toString();
        return response;

    }
}
