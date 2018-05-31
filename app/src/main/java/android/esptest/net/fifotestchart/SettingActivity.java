package android.esptest.net.fifotestchart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity {

    int value1_1;
    int value1_2;
    int value2_1;
    int value2_2;
    int value3_1;
    int value3_2;
    int value4_1;
    int value4_2;
    int value5_1;
    int value5_2;
    int value6_1;
    int value6_2;
    int value7_1;
    int value7_2;
    int value8_1;
    int value8_2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        LoadPreference();
    }

    private void LoadPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        value1_1 = sharedPreferences.getInt("pref_value_1_1", 0);
        value1_2 = sharedPreferences.getInt("pref_value_1_2", 0);
        value2_1 = sharedPreferences.getInt("pref_value_2_1", 0);
        value2_2 = sharedPreferences.getInt("pref_value_2_2", 0);
        value3_1 = sharedPreferences.getInt("pref_value_3_1", 0);
        value3_2 = sharedPreferences.getInt("pref_value_3_2", 0);
        value4_1 = sharedPreferences.getInt("pref_value_4_1", 0);
        value4_2 = sharedPreferences.getInt("pref_value_4_2", 0);
        value5_1 = sharedPreferences.getInt("pref_value_5_1", 0);
        value5_2 = sharedPreferences.getInt("pref_value_5_2", 0);
        value6_1 = sharedPreferences.getInt("pref_value_6_1", 0);
        value6_2 = sharedPreferences.getInt("pref_value_6_2", 0);
        value7_1 = sharedPreferences.getInt("pref_value_7_1", 0);
        value7_2 = sharedPreferences.getInt("pref_value_7_2", 0);
        value8_1 = sharedPreferences.getInt("pref_value_8_1", 0);
        value8_2 = sharedPreferences.getInt("pref_value_8_2", 0);



        EditText etext = findViewById(R.id.value1_1);
        etext.setText(Integer.toString(value1_1));
    }

    private void SavePreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        EditText etext = findViewById(R.id.value1_1);
        value1_1 = Integer.parseInt(etext.getText().toString());
        editor.putInt("pref_value_1_1", value1_1);
        editor.commit();
    }


    public void onClickChart(View view) {
        SavePreference();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}

