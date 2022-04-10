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

public class LightActivityKitchen extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "debugolás";
    private String id = "kitchen";
    private String[] ad;

    Switch lightSwitch;
    SeekBar brightnessSeekBar;
    TextView brightnessValue;

    private WebService service;
    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_kitchen);

        service = new WebService(getApplicationContext());
        db = new DataBase(this);

        lightSwitch = findViewById(R.id.switch_kitchen_light);
        brightnessSeekBar = findViewById(R.id.seekBar_kitchen_light);
        brightnessValue = findViewById(R.id.textView_kitchen_value);

        ad = db.load(id, 3);

        lightSwitch.setChecked(Boolean.parseBoolean(ad[0]));
        brightnessSeekBar.setProgress(Integer.parseInt(ad[1]));
        brightnessValue.setText(ad[2] + "%");

        lightSwitch.setOnCheckedChangeListener(this);
        brightnessSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) { brightnessSeekBar.setProgress(100); }
        else { brightnessSeekBar.setProgress(0); }

        Map<String, String> params = new HashMap<>();
        params.put("brightness", String.valueOf(isChecked ? "100" : "0"));
        params.put("room", "kitchen");
        service.postRequest("/light", params);
    }

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
        params.put("room", "kitchen");
        service.postRequest("/light", params);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        ad[0] = String.valueOf(lightSwitch.isChecked());
        ad[1] = String.valueOf(brightnessSeekBar.getProgress());
        ad[2] = brightnessValue.getText().toString().substring(0, brightnessValue.getText().toString().length()-1);
        db.save(id, ad);
    }
}