package com.connectsy2.utils;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.connectsy2.R;

public class TimePickerDialog extends AlertDialog implements OnClickListener, 
        OnTimeChangedListener, android.view.View.OnClickListener {

    public interface OnTimeSetListener {
        void onTimeSet(Long timestamp);
    }

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    
    private final TimePicker mTimePicker;
    private final OnTimeSetListener mCallback;
    private Calendar cal;
    private View view;

    public TimePickerDialog(Context context, OnTimeSetListener callBack, 
    		Long timestamp) {
        super(context);
        mCallback = callBack;
        
        cal = Calendar.getInstance();
        if (timestamp != null){
        	cal.setTime(new Date(timestamp));
        }else{
        	cal.add(Calendar.HOUR_OF_DAY, 1);
        	cal.set(Calendar.MINUTE, 0);
        }
        
        setButton("OK", this);
        setButton2("Cancel", (OnClickListener) null);
        
        LayoutInflater inflater = (LayoutInflater) 
        		context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.time_picker_dialog, null);
        setView(view);
        mTimePicker = (TimePicker) view.findViewById(R.id.time_picker_picker);
        
        Button today = (Button) view.findViewById(R.id.time_picker_today);
        Button tomorrow = (Button) view.findViewById(R.id.time_picker_tomorrow);
        
        tomorrow.setOnClickListener(this);
        today.setOnClickListener(this);
        
        Calendar tmpCal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_MONTH) == tmpCal.get(Calendar.DAY_OF_MONTH))
        	today.setSelected(true);
        tmpCal.add(Calendar.DAY_OF_MONTH, 1);
        if (cal.get(Calendar.DAY_OF_MONTH) == tmpCal.get(Calendar.DAY_OF_MONTH))
        	tomorrow.setSelected(true);

        // initialize state
        mTimePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
        mTimePicker.setIs24HourView(false);
        mTimePicker.setOnTimeChangedListener(this);
    }
    
    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
            mTimePicker.clearFocus();
            mCallback.onTimeSet(cal.getTime().getTime());
        }
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
    }
    
    public void updateTime() {
        mTimePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
    }
    
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, mTimePicker.getCurrentHour());
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        return state;
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
        mTimePicker.setOnTimeChangedListener(this);
    }

	public void onClick(View v) {
		cal.set(Calendar.DAY_OF_MONTH, new Date().getDate());
		if (v.getId() == R.id.time_picker_today){
			view.findViewById(R.id.time_picker_today).setSelected(true);
			view.findViewById(R.id.time_picker_tomorrow).setSelected(false);
		}else if (v.getId() == R.id.time_picker_tomorrow){
			cal.add(Calendar.DAY_OF_MONTH, 1);
			view.findViewById(R.id.time_picker_today).setSelected(false);
			view.findViewById(R.id.time_picker_tomorrow).setSelected(true);
		}
			
	}
}
