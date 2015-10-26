package flashlight.example.tam.flashlight;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

public class MainActivity extends AppCompatActivity {
    RelativeLayout tvTurnButton;
    boolean isOn;
    private Camera cameraObj;
    private StartAppAd startAppAd = new StartAppAd(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, "109802829", "209319874", true);
        setContentView(R.layout.activity_main);
        final PackageManager packageManager = MainActivity.this.getPackageManager();
        tvTurnButton = (RelativeLayout) findViewById(R.id.tvTurnButton);
        cameraObj = Camera.open();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
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
        } else {
            Toast.makeText(this, "Your device doesn't support camera flash", Toast.LENGTH_LONG).show();
        }
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
            cameraObj = Camera.open();
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

        if (cameraObj != null) {
            cameraObj.release();
        }
    }
}
