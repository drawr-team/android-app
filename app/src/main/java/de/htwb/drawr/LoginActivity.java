package de.htwb.drawr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.htwb.drawr.util.SessionUtil;
import de.htwb.drawr.view.CustomEditText;

import java.net.HttpURLConnection;

/**
 * Created by Lao on 03.11.2016.
 */

public class LoginActivity extends AppCompatActivity {

    public static final int QR_CAMERA_REQUEST_CODE = 1;
    public static final String KEY_QR_VALUE = "value";

    CustomEditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = (CustomEditText)findViewById(R.id.sessionIdED);

        editText.setListener(new CustomEditText.Listener() {
            @Override
            public void actionPerformed() {
                //start session
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                final String username = prefs.getString(PreferenceActivity.KEY_USERNAME,new String());
                final String host = prefs.getString(PreferenceActivity.KEY_HOST_URL,new String());
                final String port = prefs.getString(PreferenceActivity.KEY_HOST_PORT, "3000");
                final String sessionId = ((EditText)findViewById(R.id.sessionIdED)).getText().toString();
                if(checkCredentials(username, host)) {
                    SessionUtil.validateSessionAtHost(sessionId, host, port, new SessionUtil.AsyncWaiterListener<Integer>() {
                        @Override
                        public void resultDelivered(Integer result) {
                            if(result == HttpURLConnection.HTTP_OK && !sessionId.equals("__test__")) {
                                startMainActivity(true, false, sessionId);
                            }
                        }
                    });
                }
            }
        });

        Button newSession = (Button)findViewById(R.id.login_new_session);
        newSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(R.string.new_session_title);
                builder.setMessage(R.string.new_session_question);
                builder.setPositiveButton(R.string.online, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startMainActivity(true, true, new String());
                    }
                });
                builder.setNegativeButton(R.string.offline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startMainActivity(false, false, new String());
                    }
                });
                builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        final Button cameraButton = (Button)findViewById(R.id.login_camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, QRScanActivity.class);
                startActivityForResult(i, QR_CAMERA_REQUEST_CODE);
            }
        });
    }

    private boolean checkCredentials(String username, String host) {
        Log.d("LoginActivity", "Checking credentials...");
        return !(username.isEmpty() || host.isEmpty());
    }
    private void startMainActivity(boolean online, boolean newSession, String sessionId) {
        Bundle extras = new Bundle(3);
        extras.putBoolean(MainActivity.EXTRAS_KEY_ONLINE, online);
        extras.putBoolean(MainActivity.EXTRAS_KEY_NEW_SESSION, newSession);
        extras.putString(MainActivity.EXTRAS_KEY_SESSION_ID, sessionId);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login_settings:
                Intent intent = new Intent(this, PreferenceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("LoginActivity","Received result for request: "+requestCode+" result: "+resultCode);
        if(requestCode == QR_CAMERA_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                String value = data.getExtras().getString(KEY_QR_VALUE);
                editText.setText(value);
            }
        }
    }
}
