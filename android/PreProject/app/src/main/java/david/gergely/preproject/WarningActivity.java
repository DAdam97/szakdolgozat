package david.gergely.preproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class WarningActivity extends AppCompatActivity {

    private WebService service;

    TextView warningReason;

    MediaPlayer mp;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);
        warningReason = findViewById(R.id.textView_warningReason);

        mp = MediaPlayer.create(this, R.raw.alarm);
        mp.setVolume(100, 100);
        mp.start();

        service = new WebService(getApplicationContext());

        extras = getIntent().getExtras();
        String reason = extras.getString("msg");

        if (extras != null) warningReason.setText(reason);
    }

    public void finishWarningActivity(View view){
        mp.stop();
        mp.release();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        Map<String, String> params = new HashMap<>();
        params.put("warn", extras.getString("reason"));
        service.postRequest("/warningOff", params);
    }
}