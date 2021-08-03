package com.example.app_android_v1.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.app_android_v1.R;

public class PermissionCheckerActivity extends AppCompatActivity {


    private int nbPermissionAccepted = 0;
    private String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        ActivityCompat.requestPermissions(this, permissions, 200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200:
                for(int i = 0; i < permissions.length; i++)
                {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        nbPermissionAccepted++;
                }
                break;
        }
        if (nbPermissionAccepted != permissions.length)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                finishAffinity();
            } else {
                finish();
            }
        }
        else
        {
            Intent i = new Intent(PermissionCheckerActivity.this, MusiqueActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
