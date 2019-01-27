package castro.octavi.techtest_showoff;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements AuthenticationListener {

    private String token = null;
    private AppPreferences appPreferences = null;
    private AuthenticationDialog authenticationDialog = null;
    private Button button = null;
    private View info = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.btn_login);
        info = findViewById(R.id.info);
        appPreferences = new AppPreferences(this);

        //check already have access token
        token = appPreferences.getString(AppPreferences.TOKEN);
        if (token != null) {
            getUserInfoByAccessToken(token);
        }
    }
    
    public void login() {
        button.setText("LOGOUT");
        info.setVisibility(View.VISIBLE);
        ImageView pic = findViewById(R.id.pic);
        Picasso.with(this).load(appPreferences.getString(AppPreferences.PROFILE_PIC)).into(pic);
        TextView id = findViewById(R.id.id);
        ImageView recentpic = findViewById(R.id.recentpic);
        Picasso.with(this).load(appPreferences.getString(AppPreferences.RECENT_PIC)).into(recentpic);
        id.setText(appPreferences.getString(AppPreferences.USER_ID));
        TextView name = findViewById(R.id.name);
        name.setText(appPreferences.getString(AppPreferences.USER_NAME));
    }

    public void logout() {
        button.setText("INSTAGRAM LOGIN");
        token = null;
        info.setVisibility(View.GONE);
        appPreferences.clearing();
    }

    @Override
    public void onTokenReceived(String auth_token) {
        if (auth_token == null)
            return;
        appPreferences.putString(AppPreferences.TOKEN, auth_token);
        token = auth_token;
        getUserInfoByAccessToken(token);
    }

    public void onClick(View view) {
        if(token!=null) {
            logout();
        } else {
            authenticationDialog = new AuthenticationDialog(this, this);
            authenticationDialog.setCancelable(true);
            authenticationDialog.show();
        }
    }

    private void getUserInfoByAccessToken(String token) {
        new RequestInstagramAPI().execute(getResources().getString(R.string.get_user_info_url) + token);
        new RequestInstagramAPI().execute(getResources().getString(R.string.get_user_recent_url) + token);
    }

    private int i = 0;

    private class RequestInstagramAPI extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 0) {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(params[0]);
                try {
                    HttpResponse response = httpClient.execute(httpGet);
                    HttpEntity httpEntity = response.getEntity();
                    return EntityUtils.toString(httpEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            i++;
            if (i == 1) {
                showUserData(response);
            }
            if (i == 2) {
                showUserRecent(response);
            }
        }
    }

    private void showUserData(String userData) {
        if (userData != null) {
            try {
                JSONObject jsonObject = new JSONObject(userData);
                JSONObject jsonData = jsonObject.getJSONObject("data");
                if (jsonData.has("id")) {
                    appPreferences.putString(AppPreferences.USER_ID, jsonData.getString("id"));
                    appPreferences.putString(AppPreferences.USER_NAME, jsonData.getString("username"));
                    appPreferences.putString(AppPreferences.PROFILE_PIC, jsonData.getString("profile_picture"));

                    login();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),"Login failed!",Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void showUserRecent(String userRecent){
        if (userRecent != null) {
            try {
                JSONObject jsonObject = new JSONObject(userRecent), jsonImage, jsonVideo;
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                jsonImage = jsonArray.getJSONObject(0).getJSONObject("images").getJSONObject("low_resolution");
                appPreferences.putString(AppPreferences.RECENT_PIC, jsonImage.getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),"Login failed!",Toast.LENGTH_LONG);
            toast.show();
        }
    }

}
