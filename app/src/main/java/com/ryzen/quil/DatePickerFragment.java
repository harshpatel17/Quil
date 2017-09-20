package com.ryzen.quil;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment implements View.OnClickListener {

    private Button okBtn;

    DateCommunicator mDateCommunicator;

    DatePicker mDatePicker;

    int month;
    int day;
    int year;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDateCommunicator = (DateCommunicator) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_date_picker, null);

        mDatePicker = (DatePicker) view.findViewById(R.id.datePicker);

        okBtn = (Button) view.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.okBtn){
            month = mDatePicker.getMonth();
            day = mDatePicker.getDayOfMonth();
            year = mDatePicker.getYear();
            mDateCommunicator.dateMessage(month, day, year);
            dismiss();
        }
    }

    interface DateCommunicator{
        void dateMessage(int month, int day, int year);
    }
}
