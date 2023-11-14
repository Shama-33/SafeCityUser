package com.example.usersafecity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {


    private TextView tvLanguageSettings, tvSelectLanguage;
    private RadioGroup radioGroupLanguage;
    private RadioButton radioButtonEnglish, radioButtonBangla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvLanguageSettings = findViewById(R.id.tvLanguageSettings);
        tvSelectLanguage = findViewById(R.id.tvSelectLanguage);
        radioGroupLanguage = findViewById(R.id.radioGroupLanguage);
        radioButtonEnglish = findViewById(R.id.radioButtonEnglish);
        radioButtonBangla = findViewById(R.id.radioButtonBangla);

        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonEnglish:
                    // Handle English language selection
                    Toast.makeText(SettingsActivity.this, "English selected", Toast.LENGTH_SHORT).show();
                    setLocale("en");
                    break;

                case R.id.radioButtonBangla:
                    // Handle Bangla language selection
                    Toast.makeText(SettingsActivity.this, "Bangla selected", Toast.LENGTH_SHORT).show();
                    setLocale("bn-rBD");
                    break;



            }
        });
    }

    private void setLocale(String language) {
        Context context = LocaleHelper.setLocale(this, language);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String currentLanguage = preferences.getString("language", ""); // Get the current language

        if (!currentLanguage.equals(language)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("language", language);
            editor.apply();
            recreate(); // Restart the activity to apply the new language
        }
    }


}
