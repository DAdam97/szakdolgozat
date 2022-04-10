package david.gergely.preproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class AirActivity extends AppCompatActivity {

    private String id = "air";
    private String[] ad;

    private EditText temperatureShow;
    private EditText humidityShow;
    private EditText coPPMShow;
    private TextView desiredTempText;
    private EditText desiredTemperature;
    private Switch termostat;

    private WebService service;
    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air);

        service = new WebService(getApplicationContext());
        db = new DataBase(this);

        temperatureShow = findViewById(R.id.editText_temperatureText);
        humidityShow = findViewById(R.id.editText_humidityText);
        coPPMShow = findViewById(R.id.editText_coPPMText);
        desiredTempText = findViewById(R.id.textView_wantedtemperature);

        termostat = findViewById(R.id.switch_termostat);
        desiredTemperature = findViewById(R.id.editText_desiredTemperature);

        ad = db.load(id, 2);

        termostat.setChecked(Boolean.parseBoolean(ad[0]));
        desiredTemperature.setText(ad[1]);

        if (!termostat.isChecked()) {
            desiredTemperature.setEnabled(false);
            desiredTemperature.setVisibility(View.INVISIBLE);
            desiredTempText.setVisibility(View.INVISIBLE);
        }

        termostat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    desiredTemperature.setEnabled(true);
                    desiredTemperature.setVisibility(View.VISIBLE);
                    desiredTempText.setVisibility(View.VISIBLE);
                } else {
                    desiredTemperature.setEnabled(false);
                    desiredTemperature.setVisibility(View.INVISIBLE);
                    desiredTempText.setVisibility(View.INVISIBLE);
                }

                Map<String, String> params = new HashMap<>();


                params.put("termostat", isChecked ? "1" : "0");
                service.postRequest("/termostat", params);
            }
        });

        final Observer<Integer> temperatureObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                temperatureShow.setText(String.valueOf(integer) + " °C");
            }
        };
        MainActivity.airViewModel.getTemperature().observe(this, temperatureObserver);



        final Observer<Integer> humidityObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                humidityShow.setText(String.valueOf(integer) + "%");
            }
        };
        MainActivity.airViewModel.getHumidity().observe(this, humidityObserver);



        final Observer<Integer> coppmObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                coPPMShow.setText(String.valueOf(integer) + " ppm");
            }
        };
        MainActivity.airViewModel.getCoppm().observe(this, coppmObserver);

        temperatureShow.setEnabled(false);
        humidityShow.setEnabled(false);
        coPPMShow.setEnabled(false);

        desiredTemperature.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "30") });
        desiredTemperature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Map<String, String> params = new HashMap<>();
                params.put("desiredTemperature", s.toString());
                service.postRequest("/desiredTemperature", params);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        ad[0] = String.valueOf(termostat.isChecked());
        ad[1] = desiredTemperature.getText().toString();
        db.save(id, ad);
    }
}