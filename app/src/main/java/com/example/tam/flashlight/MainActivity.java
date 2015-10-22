package com.example.tam.flashlight;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {
    RelativeLayout tvTurnButton;
    boolean isOn;
    private AdView mAdView;
    private Camera cameraObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        mAdView = (AdView) findViewById(R.id.ad_view);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
        final PackageManager packageManager = MainActivity.this.getPackageManager();
        tvTurnButton = (RelativeLayout) findViewById(R.id.tvTurnButton);
        cameraObj = Camera.open();
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            tvTurnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isOn) {
                        isOn = false;
                        Camera.Parameters cameraParams = cameraObj.getParameters();
                        cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        cameraObj.setParameters(cameraParams);
                        cameraObj.stopPreview();

                    } else {
                        isOn = true;
                        Camera.Parameters cameraParams = cameraObj.getParameters();
                        cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        cameraObj.setParameters(cameraParams);
                        cameraObj.startPreview();
                    }


                }
            });
        }else{
            Toast.makeText(this,"Your device doesn't support camera flash",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        try {
            cameraObj = Camera.open();
        } catch (RuntimeException e) {

        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (cameraObj != null) {
            cameraObj.release();
        }
    }
}
