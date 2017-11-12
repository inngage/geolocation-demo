package br.com.inngage.tutorialfcm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import br.com.inngage.sdk.GrantPermission;
import br.com.inngage.sdk.InngageIntentService;
import br.com.inngage.sdk.InngagePermissionUtil;
import br.com.inngage.sdk.InngageServiceUtils;
import br.com.inngage.sdk.InngageUtils;
import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    InngageServiceUtils serviceUtils = new InngageServiceUtils(MainActivity.this);

    private InngagePermissionUtil.PermissionRequestObject mBothPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        handleSubscription();
        handleLocation();
        handleNotification();

    }

    /**
     * Handle Inngage subscription process
     */
    private void handleSubscription() {

        JSONObject jsonCustomField = new JSONObject();

        try {
            jsonCustomField.put("primeiro_nome", "Joaquim");
            jsonCustomField.put("cidade", "São Paulo");
            jsonCustomField.put("estado", "SP");

        } catch (JSONException e) {

            e.printStackTrace();
        }

        InngageIntentService.startInit(
                this,
                InngageConstants.inngageAppToken,
                "user02@inngage.com.br",
                InngageConstants.inngageEnvironment,
                InngageConstants.googleMessageProvider,
                jsonCustomField);
    }

    /**
     * Runtime permissions are required on Android M and above to access User's location
     */
    private void handleLocation() {

        if (InngageUtils.hasM() && !(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                        this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            askPermissions();

        } else {

            serviceUtils.startService(
                    InngageConstants.updateInterval,
                    InngageConstants.priorityAccuracy,
                    InngageConstants.displacement,
                    InngageConstants.inngageAppToken);
        }
    }

    /**
     * Ask user for permissions to access GPS location on Android M
     */
    public void askPermissions() {
        mBothPermissionRequest =
                InngagePermissionUtil.with(this).request(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION).onResult(
                        new GrantPermission() {
                            @Override
                            protected void call(int requestCode, String[] permissions, int[] grantResults) {

                                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                                    serviceUtils.startService(
                                            InngageConstants.updateInterval,
                                            InngageConstants.priorityAccuracy,
                                            InngageConstants.displacement,
                                            InngageConstants.inngageAppToken);

                                } else {

                                    Toast.makeText(
                                            MainActivity.this,
                                            InngageConstants.LOCATION_NOT_FOUND,
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                        }).ask(InngageConstants.PERMISSION_ACCESS_LOCATION_CODE);
    }

    /**
     * Handle Inngage push notification
     */
    private void handleNotification() {

        String notifyID = "", title = "", body = "";

        Bundle bundle = getIntent().getExtras();

        if (getIntent().hasExtra("EXTRA_NOTIFICATION_ID")) {

            notifyID = bundle.getString("EXTRA_NOTIFICATION_ID");
        }
        if (getIntent().hasExtra("EXTRA_TITLE")) {

            title = bundle.getString("EXTRA_TITLE");
        }
        if (getIntent().hasExtra("EXTRA_BODY")) {

            body = bundle.getString("EXTRA_BODY");
        }
        if (!"".equals(notifyID) || !"".equals(title) || !"".equals(body)) {

            InngageUtils.showDialog(
                    title,
                    body, notifyID,
                    InngageConstants.inngageAppToken,
                    InngageConstants.inngageEnvironment,
                    this);
        }
    }
}