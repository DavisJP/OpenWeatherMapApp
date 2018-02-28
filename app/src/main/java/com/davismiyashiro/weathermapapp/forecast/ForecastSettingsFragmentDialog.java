package com.davismiyashiro.weathermapapp.forecast;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import com.davismiyashiro.weathermapapp.R;

import static com.davismiyashiro.weathermapapp.forecast.ForecastListActivity.TEMPERATURE_KEY;

/**
 * Created by Davis Miyashiro.
 */

public class ForecastSettingsFragmentDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_temperature_unit)
                .setItems(R.array.pref_temperature_units, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor edit = pref.edit();

                        edit.putInt(TEMPERATURE_KEY, position);
                        edit.apply();
                    }
                });

        return builder.create();
    }

    public static DialogFragment newInstance() {
        return new ForecastSettingsFragmentDialog();
    }
}
