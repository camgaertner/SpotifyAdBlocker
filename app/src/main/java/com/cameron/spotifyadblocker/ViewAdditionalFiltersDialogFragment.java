package com.cameron.spotifyadblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by leapwill on 2017-07-05.
 */

public class ViewAdditionalFiltersDialogFragment extends DialogFragment {

    ViewAdditionalFiltersDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Tap to delete");
        builder.setItems(getArguments().getCharSequenceArray("additionalFilters"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onFilterClick(dialogInterface, i);
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (ViewAdditionalFiltersDialogListener) activity;
    }

    public static ViewAdditionalFiltersDialogFragment newInstance(CharSequence[] additionalFilters) {

        Bundle args = new Bundle();
        args.putCharSequenceArray("additionalFilters", additionalFilters);
        ViewAdditionalFiltersDialogFragment fragment = new ViewAdditionalFiltersDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    interface ViewAdditionalFiltersDialogListener {
        void onFilterClick(DialogInterface dialogInterface, int i);
    }
}
