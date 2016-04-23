package org.geekcircle.ffmpeggo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ExecutorActivity extends AppCompatActivity {
    private static final String TAG = "EXECUTOR_ACTIVITY";
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executor);

        Intent intent = getIntent();
        String command = intent.getStringExtra("command");

        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        this.executor = new Executor(this, textView);
        this.executor.execute(command);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_executor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case R.id.action_return:
                if (this.executor.isDone()) {
                    Intent intent = new Intent();
                    intent.putExtra("code", executor.isStop()?-1:executor.getCode());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    this.executor.term();
                    Log.w(TAG, "Executing still running.");
                }
                break;
        }

        return super.onOptionsItemSelected(item);
        //Intent myIntent = new Intent(getApplicationContext(),MainActivity.class);
        //startActivityForResult(myIntent, 0);
        //return true;
    }
}
