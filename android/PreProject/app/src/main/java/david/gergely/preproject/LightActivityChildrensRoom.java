package david.gergely.preproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class LightActivityChildrensRoom extends AppCompatActivity {

    private static final String TAG = "debugolás";
    private String id = "children";
    private String[] ad;

    Switch lightSwitch;
    Switch shutterSwitch;
    Switch autoSwitch;
    SeekBar brightnessSeekBar;
    SeekBar shutterSeekBar;
    TextView brightnessValue;
    TextView shutterValue;

    private WebService service;
    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_childrensroom);

        service = new WebService(getApplicationContext());
        db = new DataBase(this);

        lightSwitch = findViewById(R.id.switch_childrensroom_light);
        shutterSwitch = findViewById(R.id.switch_childrensroom_shutter);
        autoSwitch = findViewById(R.id.switch_childrensroom_shutterAuto);
        brightnessSeekBar = findViewById(R.id.seekBar_childrensroom_light);
        shutterSeekBar = findViewById(R.id.seekBar_childrensroom_shutter);
        brightnessValue = findViewById(R.id.textView_childrensroom_lightvalue);
        shutterValue = findViewById(R.id.textView_childrensroom_shuttervalue);

        ad = db.load(id, 7);

        lightSwitch.setChecked(Boolean.parseBoolean(ad[0]));
        shutterSwitch.setChecked(Boolean.parseBoolean(ad[1]));
        autoSwitch.setChecked(Boolean.parseBoolean(ad[2]));
        brightnessSeekBar.setProgress(Integer.parseInt(ad[3]));
        shutterSeekBar.setProgress(Integer.parseInt(ad[4]));
        brightnessValue.setText(ad[5] + "%");
        shutterValue.setText(ad[6] + "%");

        lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) { brightnessSeekBar.setProgress(100); }
                else { brightnessSeekBar.setProgress(0); }

                Map<String, String> params = new HashMap<>();
                params.put("brightness", String.valueOf(isChecked ? "100" : "0"));
                params.put("room", "children");
                service.postRequest("/light", params);
            }
        });

        shutterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) { shutterSeekBar.setProgress(100); }
                else { shutterSeekBar.setProgress(0); }

                Map<String, String> params = new HashMap<>();
                params.put("shutterVal", String.valueOf(isChecked ? "100" : "0"));
                params.put("room", "children");
                service.postRequest("/shutter", params);
            }
        });

        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Map<String, String> params = new HashMap<>();
                params.put("auto", String.valueOf(isChecked ? "1" : "0"));
                params.put("room", "children");
                service.postRequest("/shutterAuto", params);
            }
        });

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessValue.setText(progress + "%");


                if(progress == 0) { lightSwitch.setChecked(false); }
                if(progress > 0) { lightSwitch.setChecked(true); }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: " + String.valueOf(seekBar.getProgress()));
                Map<String, String> params = new HashMap<>();
                params.put("brightness", String.valueOf(seekBar.getProgress()));
                params.put("room", "children");
                service.postRequest("/light", params);
            }
        });

        shutterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                shutterValue.setText(progress + "%");


                if(progress == 0) { shutterSwitch.setChecked(false); }
                if(progress > 0) { shutterSwitch.setChecked(true); }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: " + String.valueOf(seekBar.getProgress()));
                Map<String, String> params = new HashMap<>();
                params.put("shutterVal", String.valueOf(seekBar.getProgress()));
                params.put("room", "children");
                service.postRequest("/shutter", params);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        ad[0] = String.valueOf(lightSwitch.isChecked());
        ad[1] = String.valueOf(shutterSwitch.isChecked());
        ad[2] = String.valueOf(autoSwitch.isChecked());
        ad[3] = String.valueOf(brightnessSeekBar.getProgress());
        ad[4] = String.valueOf(shutterSeekBar.getProgress());
        ad[5] = brightnessValue.getText().toString().substring(0, brightnessValue.getText().toString().length()-1);
        ad[6] = shutterValue.getText().toString().substring(0, shutterValue.getText().toString().length()-1);
        db.save(id, ad);
    }
}