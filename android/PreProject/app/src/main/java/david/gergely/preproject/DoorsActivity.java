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

public class DoorsActivity extends AppCompatActivity {

    private static final String TAG = "debugolás";
    private String id = "doors";
    private String[] ad;

    Switch entranceDoorSwitch;
    Switch kitchenDoorSwitch;
    SeekBar entranceDoorSeekBar;
    SeekBar kitchenDoorSeekBar;
    TextView entranceDoorValue;
    TextView kitchenDoorValue;

    private WebService service;
    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doors);

        service = new WebService(getApplicationContext());
        db = new DataBase(this);

        entranceDoorSwitch = findViewById(R.id.switch_entrancedoor);
        kitchenDoorSwitch = findViewById(R.id.switch_kitchendoor);
        entranceDoorSeekBar = findViewById(R.id.seekBar_entrancedoor);
        kitchenDoorSeekBar = findViewById(R.id.seekBar_kitchendoor);
        entranceDoorValue = findViewById(R.id.textView_entrancedoor_value);
        kitchenDoorValue = findViewById(R.id.textView_kitchendoor_value);

        ad = db.load(id, 6);

        entranceDoorSwitch.setChecked(Boolean.parseBoolean(ad[0]));
        kitchenDoorSwitch.setChecked(Boolean.parseBoolean(ad[1]));
        entranceDoorSeekBar.setProgress(Integer.parseInt(ad[2]));
        kitchenDoorSeekBar.setProgress(Integer.parseInt(ad[3]));
        entranceDoorValue.setText(ad[4] + "%");
        kitchenDoorValue.setText(ad[5] + "%");

        entranceDoorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) { entranceDoorSeekBar.setProgress(100); }
                else { entranceDoorSeekBar.setProgress(0); }

                Map<String, String> params = new HashMap<>();
                params.put("angle", String.valueOf(isChecked ? "100" : "0"));
                params.put("room", "livingroom");
                service.postRequest("/door", params);
            }
        });

        kitchenDoorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) { kitchenDoorSeekBar.setProgress(100); }
                else { kitchenDoorSeekBar.setProgress(0); }

                Map<String, String> params = new HashMap<>();
                params.put("angle", String.valueOf(isChecked ? "100" : "0"));
                params.put("room", "kitchen");
                service.postRequest("/door", params);
            }
        });

        entranceDoorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                entranceDoorValue.setText(progress + "%");


                if(progress == 0) { entranceDoorSwitch.setChecked(false); }
                if(progress > 0) { entranceDoorSwitch.setChecked(true); }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: " + String.valueOf(seekBar.getProgress()));
                Map<String, String> params = new HashMap<>();
                params.put("angle", String.valueOf(seekBar.getProgress()));
                params.put("room", "livingroom");
                service.postRequest("/door", params);
            }
        });

        kitchenDoorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                kitchenDoorValue.setText(progress + "%");


                if(progress == 0) { kitchenDoorSwitch.setChecked(false); }
                if(progress > 0) { kitchenDoorSwitch.setChecked(true); }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: " + String.valueOf(seekBar.getProgress()));
                Map<String, String> params = new HashMap<>();
                params.put("angle", String.valueOf(seekBar.getProgress()));
                params.put("room", "kitchen");
                service.postRequest("/door", params);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        ad[0] = String.valueOf(entranceDoorSwitch.isChecked());
        ad[1] = String.valueOf(kitchenDoorSwitch.isChecked());
        ad[2] = String.valueOf(entranceDoorSeekBar.getProgress());
        ad[3] = String.valueOf(kitchenDoorSeekBar.getProgress());
        ad[4] = entranceDoorValue.getText().toString().substring(0, entranceDoorValue.getText().toString().length()-1);
        ad[5] = kitchenDoorValue.getText().toString().substring(0, kitchenDoorValue.getText().toString().length()-1);
        db.save(id, ad);
    }
}