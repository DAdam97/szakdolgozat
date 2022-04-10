package david.gergely.preproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LightActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
    }

    public void startLivingRoomLightActivity(View view){
        Intent intent = new Intent(this, LightActivityLivingRoom.class);
        startActivity(intent);
    }

    public void startKitchenLightActivity(View view){
        Intent intent = new Intent(this, LightActivityKitchen.class);
        startActivity(intent);
    }

    public void startGardenLightActivity(View view){
        Intent intent = new Intent(this, LightActivityGarden.class);
        startActivity(intent);
    }

    public void startBathRoomLightActivity(View view){
        Intent intent = new Intent(this, LightActivityBathRoom.class);
        startActivity(intent);
    }

    public void startChildrensRoomLightActivity(View view){
        Intent intent = new Intent(this, LightActivityChildrensRoom.class);
        startActivity(intent);
    }
}