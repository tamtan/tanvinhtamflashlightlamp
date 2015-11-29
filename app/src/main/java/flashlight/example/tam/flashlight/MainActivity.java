package flashlight.example.tam.flashlight;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

public class MainActivity extends AppCompatActivity {
    RelativeLayout tvTurnButton;
    ImageView imgFlash;
    boolean isOn;
    private CameraPreview mPreview;
//    private Camera cameraObj;
    private StartAppAd startAppAd = new StartAppAd(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, "109802829", "209319874", true);
        setContentView(R.layout.activity_main);
        final PackageManager packageManager = MainActivity.this.getPackageManager();
        tvTurnButton = (RelativeLayout) findViewById(R.id.tvTurnButton);
        imgFlash = (ImageView) findViewById(R.id.imgFlash);
//        cameraObj = Camera.open();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            tvTurnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isOn) {
                        isOn = false;
                        mPreview.mCamera.stopPreview();
                        tvTurnButton.removeView(mPreview);
                        onPreviewClose();

                    } else {
                        isOn = true;
                        onPreviewOpen();
                        createCameraPreview();
                    }


                }
            });
        } else {
            Toast.makeText(this, "Your device doesn't support camera flash", Toast.LENGTH_LONG).show();
        }
    }
    private void createCameraPreview() {
        mPreview = new CameraPreview(this, 0, CameraPreview.LayoutMode.FitToParent);
        RelativeLayout.LayoutParams previewLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        previewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvTurnButton.addView(mPreview, 0, previewLayoutParams);
//        mPreview.startCameraPreview();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        startAppAd.onResume();
        try {
            mPreview.mCamera.startPreview();

        } catch (RuntimeException e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        startAppAd.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mPreview.mCamera != null){
            mPreview.mCamera.release();
        }
//        if (cameraObj != null) {
//            cameraObj.release();
//        }
    }
    public void onPreviewOpen()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            tvTurnButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.black_background));
        }
        imgFlash.setVisibility(View.INVISIBLE);

    }
    public void onPreviewClose()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            tvTurnButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.back_ground_1));
        }
        imgFlash.setVisibility(View.VISIBLE);
    }
}
