package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingActivity extends AppCompatActivity {
    private ListView appListView;
    private ArrayAdapter<String> adapter;
    private List<ApplicationInfo> installedApps;
    private Set<String> approvedApps;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        appListView = findViewById(R.id.appListView);
        prefs = getSharedPreferences("KidLauncherPrefs", Context.MODE_PRIVATE);
        approvedApps = prefs.getStringSet("approved_apps", new HashSet<>());

        PackageManager pm = getPackageManager();
        installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        String[] appNames = new String[installedApps.size()];
        for (int i = 0; i < installedApps.size(); i++) {
            appNames[i] = installedApps.get(i).loadLabel(pm).toString();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, appNames);
        appListView.setAdapter(adapter);
        appListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        for (int i = 0; i < installedApps.size(); i++) {
            if (approvedApps.contains(installedApps.get(i).packageName)) {
                appListView.setItemChecked(i, true);
            }
        }

        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String packageName = installedApps.get(position).packageName;
                if (approvedApps.contains(packageName)) {
                    approvedApps.remove(packageName);
                } else {
                    approvedApps.add(packageName);
                }
                saveApprovedApps();
            }
        });
    }

    private void saveApprovedApps() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("approved_apps", approvedApps);
        editor.apply();
        Toast.makeText(this, "Approved Apps Updated!", Toast.LENGTH_SHORT).show();
    }
}
