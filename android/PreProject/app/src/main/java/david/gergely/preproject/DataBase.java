package david.gergely.preproject;

import android.content.Context;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;

import androidx.annotation.LongDef;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class DataBase {

    private Context context;
    private String[] datas  = null;

    public DataBase(Context context) {
        this.context = context;
    }

    public String DataBase() {
        String data = "air" + ";" + "false" + ";" + "20" + ";" +
                "doors" + ";" + "false" + ";" + "false" + ";" + "0" + ";" + "0" + ";" + "0" + ";" + "0" + ";" +
                "bathroom" + ";" + "false" + ";" + "false" + ";" + "false" + ";" + "0" + ";" + "0" + ";" + "0" + ";" + "0" + ";" +
                "children" + ";" + "false" + ";" + "false" + ";" + "false" + ";" + "0" + ";" + "0" + ";" + "0" + ";" + "0" + ";" +
                "garden" + ";" + "false" + ";" + "false" + ";" + "0" + ";" + "0" + ";" + "0" + ";" +
                "kitchen" + ";" + "false" + ";" + "0" + ";" + "0" + ";" +
                "living" + ";" + "false" + ";" + "0" + ";" + "0" + ";" +
                "security" + ";" + "false";
        return data;
    }

    public void saveAll(String dbase) throws IOException {

        String db = dbase;
        FileOutputStream fos = null;

        try {
            fos = context.openFileOutput(MainActivity.FILENAME, Context.MODE_PRIVATE);
            fos.write(db.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public String[] load(String id, int x) {
        FileInputStream fis = null;

        String[] temp = new String[x];
        try {
            fis = context.openFileInput(MainActivity.FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br =  new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text);
            }

            String allData = sb.toString();
            datas = allData.split(";");

            int ID = 0;

            for (int i = 0; i < datas.length; i++) {
                if (datas[i].equals(id)) {
                    ID = i + 1;
                }
            }
            for (int i = ID; i < ID + x; i++) {
                temp[i-ID] = datas[i];

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public void save(String id, String[] params){

        int ID = 0;

        for (int i = 0; i < datas.length; i++) {
            if (datas[i].equals(id)) {
                ID = i + 1;
                break;
            }
        }


        for (int i = 0; i < params.length; i++) {
            datas[ID + i] = params[i];
            Log.d("debugol", datas[ID + i]);
        }

        String temp = "";

        for (int i = 0; i < datas.length-1; i++) {
            temp += datas[i] + ";";
        }
        temp += datas[datas.length - 1];

        try {
            saveAll(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
