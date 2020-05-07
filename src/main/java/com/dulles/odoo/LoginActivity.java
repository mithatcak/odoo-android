package com.dulles.odoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity  extends AppCompatActivity {
    private static final String LOGIN_KEY = "LOGIN_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);
        GetResponse();
//        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
//        if (pref.getBoolean(LOGIN_KEY, false)) {
//            //has login
//            startActivity(new Intent(this, MainActivity.class));
//            //must finish this activity (the login activity will not be shown when click back in main activity)
//            finish();
//        } else {
//            // Mark login
//            pref.edit().putBoolean(LOGIN_KEY, true).apply();
//
//            // Do something
//        }

        Button clickButton = (Button) findViewById(R.id.loginButton);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* TODO Auto-generated method stub */
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                v.getContext().startActivity(intent);
                //must finish this activity (the login activity will not be shown when click back in main activity)
                finish();
            }
        });
    }

    private void GetResponse(){
        String url ="https://erp-dev.dullesglass.com/web/login";
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(url, (JSONObject) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                /*Add your headers here*/
                return super.getHeaders();
            }
        };

// Add the request to the RequestQueue.
        queue.add(request);

    }



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */

//    public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
//
//        @Override
//        protected void onPostExecute(final Boolean success) {
//            if (success == true) {
//                //Do whatever your app does after login
//
//            } else {
//                //Let user know login has failed
//            }
//        }
//
//        @Override
//        protected Boolean doInBackground(String... login) {
//            URL url = new URL("https://erp-dev.dullesglass.com/login");
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            int statusCode = urlConnection.getResponseCode();
//            if (statusCode == 200) {
//                InputStream it = new BufferedInputStream(urlConnection.getInputStream());
//                InputStreamReader read = new InputStreamReader(it);
//                BufferedReader buff = new BufferedReader(read);
//                StringBuilder dta = new StringBuilder();
//                String chunks;
//                while ((chunks = buff.readLine()) != null) {
//                    dta.append(chunks);
//                }
//            } else {
//                //Handle else
//            }
//        }



}
