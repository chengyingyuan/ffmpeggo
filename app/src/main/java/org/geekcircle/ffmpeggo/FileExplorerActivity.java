package org.geekcircle.ffmpeggo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;


public class FileExplorerActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fileexplorer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // TODO: show explanation
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        Intent intent = getIntent();
        String path = intent.getStringExtra("mediaPath");
        if (path==null) {
            path = "/";
        } else {
            path = new File(path).getAbsoluteFile().getParent();
        }
        doOpenDirectory(path, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }


    private void doOpenDirectory(String path, boolean addToBackStack) {
        Fragment newFragment = FileListFragment.newInstance(path);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.body, newFragment);

        if (addToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    public void finishExplorer(String path) {
        File f = new File(path);
        try {
            f = f.getAbsoluteFile();
            f = f.getCanonicalFile();
            if (TextUtils.isEmpty(f.toString()))
                f = new File("/");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (f.isDirectory()) {
            doOpenDirectory(f.toString(), true);
        } else if (f.exists()) {
            Intent intent = new Intent();
            intent.putExtra("mediaPath", f.toString());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_fileexplorer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
