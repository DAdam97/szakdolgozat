package david.gergely.preproject;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AirViewModel extends ViewModel {
    private MutableLiveData<Integer> temperature;
    private MutableLiveData<Integer> humidity;
    private MutableLiveData<Integer> coppm;

    public MutableLiveData<Integer> getTemperature() {
        if (temperature == null) temperature = new MutableLiveData<>();
        return temperature;
    }

    public MutableLiveData<Integer> getHumidity() {
        if (humidity == null) humidity = new MutableLiveData<>();
        return humidity;
    }

    public MutableLiveData<Integer> getCoppm() {
        if (coppm == null) coppm = new MutableLiveData<>();
        return coppm;
    }
}
