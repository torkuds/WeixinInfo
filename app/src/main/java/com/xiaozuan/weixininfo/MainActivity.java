package com.xiaozuan.weixininfo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isPermisstionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!isPermisstionGranted(Manifest.permission.READ_CONTACTS)) {
            requestPermission(Manifest.permission.READ_CONTACTS);
        }

        if (isPermisstionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) && isPermisstionGranted(Manifest.permission.READ_CONTACTS)){
            startService(new Intent(this, WxInfoService.class));
        }

        findViewById(R.id.btn_start).setOnClickListener(this);

//        startService(new Intent(this, WxInfoService.class));
    }

    private boolean isPermisstionGranted(String permission) {
        int selfPermission = ContextCompat.checkSelfPermission(this, permission);
        return selfPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] mPermissionList = new String[]{permission};
            ActivityCompat.requestPermissions(MainActivity.this, mPermissionList, 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isPermisstionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) && isPermisstionGranted(Manifest.permission.READ_CONTACTS)){
            startService(new Intent(this, WxInfoService.class));
        }
    }

    @Override
    public void onClick(View view) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
