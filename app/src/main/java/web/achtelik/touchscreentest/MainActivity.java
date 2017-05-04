package web.achtelik.touchscreentest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import web.achtelik.wstouchdriver.TouchscreenDriverService;

public class MainActivity extends AppCompatActivity {

    private Intent driverIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        driverIntent = new Intent(getApplicationContext(), TouchscreenDriverService.class);
        startService(driverIntent);

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(driverIntent);
    }
}
