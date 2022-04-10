package david.gergely.preproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.HashMap;
import java.util.Map;

public class SecurityActivity extends AppCompatActivity {

    private WebService service;
    private String id = "security";
    private String[] ad;

    Switch securitySwitch;

    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        service = new WebService(getApplicationContext());
        db = new DataBase(this);

        securitySwitch = findViewById(R.id.switch_securityEnable);

        ad = db.load(id, 1);

        securitySwitch.setChecked(Boolean.parseBoolean(ad[0]));

        securitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Map<String, String> params = new HashMap<>();
                params.put("enable", String.valueOf(isChecked ? "1" : "0"));
                service.postRequest("/securityEnable", params);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        ad[0] = String.valueOf(securitySwitch.isChecked());
        db.save(id, ad);
    }
}