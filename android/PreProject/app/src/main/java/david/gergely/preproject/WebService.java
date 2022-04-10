package david.gergely.preproject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class WebService {
    public static final String baseUrl = "http://192.168.1.106:8080";  // Egyetem
    private static final String TAG = "debugolás";

    private Context context;

    private RequestQueue queue;
    String value = "";

    public String getValue() {
        return value;
    }

    public WebService(Context context) {
        this.context = context;
        queue = Volley.newRequestQueue(context);
    }

    public void getRequest(String path, final VolleyCallback callback) {

        StringRequest request = new StringRequest(Request.Method.GET, baseUrl + path,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(TAG, "onErrorResponse: " + error.getMessage());

                        value = "Hiba a szerveren";

                        if (error instanceof TimeoutError) {
                            Toast.makeText(context, "TimeoutError", Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NoConnectionError) {
                            Toast.makeText(context, "NoConnectionError", Toast.LENGTH_SHORT).show();

                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(context, "AuthFailureError", Toast.LENGTH_SHORT).show();

                        } else if (error instanceof ServerError) {
                            Toast.makeText(context, "ServerError", Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NetworkError) {
                            Toast.makeText(context, "NetworkError", Toast.LENGTH_SHORT).show();

                        } else if (error instanceof ParseError) {
                            Toast.makeText(context, "ParseError", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(request);

        Log.d(TAG, "GetRequest: " + value);
    }

    public void postRequest(String path, Map<String, String> params) {
        StringRequest request = new StringRequest(Request.Method.POST, baseUrl + path,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        queue.add(request);

    }
}
