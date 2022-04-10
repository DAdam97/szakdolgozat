package david.gergely.preproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    public static AirViewModel airViewModel;
    public static final String FILENAME = "data.txt";

    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        db = new DataBase(this);
        try {
            db.saveAll(db.DataBase());
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        try {
            Manager manager = new Manager(new URI("http://192.168.1.106:8080"));
            Socket socket = manager.socket("/msg");
            socket.on("breachDetected",     breachDetected);
            socket.on("gasDetected",        gasDetected);
            socket.on("temperatureChanged", temperatureChanged);
            socket.on("humidityChanged",    humidityChanged);
            socket.on("coPPMChanged",       coPPMChanged);
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        airViewModel = new ViewModelProvider(this, new AirViewModelFactory()).get(AirViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    public void startLightActivity(View view){
        Intent intent = new Intent(this, LightActivity.class);
        startActivity(intent);
    }

    public void startDoorsActivity(View view){
        Intent intent = new Intent(this, DoorsActivity.class);
        startActivity(intent);
    }

    public void startSecurityActivity(View view){
        Intent intent = new Intent(this, SecurityActivity.class);
        startActivity(intent);
    }

    public void startAirActivity(View view){
        Intent intent = new Intent(this, AirActivity.class);
        startActivity(intent);
    }

    private Emitter.Listener breachDetected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, WarningActivity.class);
                    intent.putExtra("msg", args[0].toString());
                    intent.putExtra("reason", "intruder");
                    startActivity(intent);
                }
            });
        }
    };

    private Emitter.Listener gasDetected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, WarningActivity.class);
                    intent.putExtra("msg", args[0].toString());
                    intent.putExtra("reason", "gas");
                    startActivity(intent);
                }
            });
        }
    };

    private Emitter.Listener temperatureChanged = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("debugol", "Main: " + args[0].toString());
                    airViewModel.getTemperature().setValue(Integer.valueOf(args[0].toString()));
                }
            });
        }
    };

    private Emitter.Listener humidityChanged = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("debugol", "Main: " + args[0].toString());
                    airViewModel.getHumidity().setValue(Integer.valueOf(args[0].toString()));
                }
            });
        }
    };

    private Emitter.Listener coPPMChanged = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("debugol", "Main: " + args[0].toString());
                    airViewModel.getCoppm().setValue(Integer.valueOf(args[0].toString()));
                }
            });
        }
    };
}