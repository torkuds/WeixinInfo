package com.xiaozuan.weixininfo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isPermisstionGranted()) {
            requestPermission();
        }

        startService(new Intent(this, AccessbilityService.class));
    }

    private boolean isPermisstionGranted() {
        int selfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return selfPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(MainActivity.this, mPermissionList, 123);
        }
    }
}
