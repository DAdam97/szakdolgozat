package david.gergely.preproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class LightActivityGarden extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener{

    private static final String TAG = "debugolás";
    private String id = "garden";
    private String[] ad;

    Switch lightSwitch;
    Switch autoSwitch;
    SeekBar brightnessSeekBar;
    TextView brightnessValue;
    EditText gardenLightLength;

    private WebService service;
    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_garden);

        service = new WebService(getApplicationContext());
        db = new DataBase(this);

        lightSwitch = findViewById(R.id.switch_garden_light);
        autoSwitch = findViewById(R.id.switch_garden_automode);
        brightnessSeekBar = findViewById(R.id.seekBar_garden_light);
        gardenLightLength = findViewById(R.id.editTextNumber_autoLight_Length);
        brightnessValue = findViewById(R.id.textView_garden_value);

        ad = db.load(id, 5);

        lightSwitch.setChecked(Boolean.parseBoolean(ad[0]));
        autoSwitch.setChecked(Boolean.parseBoolean(ad[1]));
        brightnessSeekBar.setProgress(Integer.parseInt(ad[2]));
        gardenLightLength.setText(ad[3]);
        brightnessValue.setText(ad[4] + "%");

        gardenLightLength.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "300")});

        gardenLightLength.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0){
                    Map<String, String> params = new HashMap<>();
                    params.put("durationVal", s.toString());
                    service.postRequest("/PIRDuration", params);
                }
            }
        });

        lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) { brightnessSeekBar.setProgress(100); }
                else { brightnessSeekBar.setProgress(0); }

                Map<String, String> params = new HashMap<>();
                params.put("brightness", String.valueOf(isChecked ? "100" : "0"));
                params.put("room", "garden");
                service.postRequest("/light", params);
            }
        });


        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Map<String, String> params = new HashMap<>();
                params.put("enable", String.valueOf(isChecked ? "1" : "0"));
                service.postRequest("/PIREnable", params);
            }
        });
        brightnessSeekBar.setOnSeekBarChangeListener(this);
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
        params.put("room", "garden");
        service.postRequest("/light", params);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        ad[0] = String.valueOf(lightSwitch.isChecked());
        ad[1] = String.valueOf(autoSwitch.isChecked());
        ad[2] = String.valueOf(brightnessSeekBar.getProgress());
        ad[3] = gardenLightLength.getText().toString();
        ad[4] = brightnessValue.getText().toString().substring(0, brightnessValue.getText().toString().length()-1);
        db.save(id, ad);
    }
}