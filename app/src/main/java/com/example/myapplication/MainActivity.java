package com.example.myapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private GridView gridView;
    private AppAdapter appAdapter;
    private List<AppModel> appList;
    private List<String> approvedApps;
    private SharedPreferences sharedPreferences;
    private static final String PREF_PIN = "user_pin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.gridView);
        sharedPreferences = getSharedPreferences("LauncherPrefs", MODE_PRIVATE);

        loadApprovedApps();
        loadInstalledApps();

        appAdapter = new AppAdapter(this, appList);
        gridView.setAdapter(appAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String packageName = appList.get(position).getPackageName();
                if (approvedApps.contains(packageName)) {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Access Denied!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkForFirstTimePinSetup();
    }

    private void loadApprovedApps() {
        approvedApps = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("KidLauncherPrefs", MODE_PRIVATE);
        Set<String> savedApps = prefs.getStringSet("approved_apps", new HashSet<>());

        approvedApps.addAll(savedApps);  // Load user-approved apps
    }


    private void loadInstalledApps() {
        appList = new ArrayList<>();
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : availableApps) {
            String appName = resolveInfo.loadLabel(pm).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            appList.add(new AppModel(appName, packageName, resolveInfo.loadIcon(pm)));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            showPinDialog(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void showPinDialog(boolean isPinUpdate) {
        View pinView = getLayoutInflater().inflate(R.layout.dialog_pin, null);
        EditText etPin = pinView.findViewById(R.id.etPin);
        Button btnSubmit = pinView.findViewById(R.id.btnSubmit);
        TextView title = pinView.findViewById(R.id.titleTv1);

        if (isPinUpdate){
            title.setText("Set Your Pin");
        }else {
            title.setText("Enter PIN to Exit");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(pinView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnSubmit.setOnClickListener(v -> {
            String enteredPin = etPin.getText().toString();
            String savedPin = sharedPreferences.getString(PREF_PIN, "0000");

            if (isPinUpdate) {
                sharedPreferences.edit().putString(PREF_PIN, enteredPin).apply();
                Toast.makeText(this, "PIN Updated!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                if (enteredPin.equals(savedPin)) {
                    finish();
                } else {
                    Toast.makeText(this, "Incorrect PIN!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkForFirstTimePinSetup() {
        String savedPin = sharedPreferences.getString(PREF_PIN, "");
        if (savedPin.isEmpty()) {
            showPinDialog(true); // Force user to set a PIN if it's not set
        }
    }

    public void openPinUpdateDialog(View view) {
        showPinDialog(true); // User can update PIN anytime
    }
}
