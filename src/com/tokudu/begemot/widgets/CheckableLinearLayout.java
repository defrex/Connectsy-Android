package com.tokudu.begemot.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

/*
 * This class is useful for using inside of ListView that needs to have checkable items.
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private CheckedTextView _checkbox;
    	
    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
	}
    
    @Override
    protected void onFinishInflate() {
    	super.onFinishInflate();
    	// find checked text view
		_checkbox = findCheckedChild(this);
		if (_checkbox == null)
			Log.d("CheckableLinearLayout", "_checkbox is null");
    }
    
    private CheckedTextView findCheckedChild(ViewGroup view){
		int childCount = view.getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View v = getChildAt(i);
			if (v instanceof CheckedTextView) {
				return (CheckedTextView)v;
			}else if (v instanceof ViewGroup){
				Log.d("CheckableLinearLayout", "recursing");
				CheckedTextView check = findCheckedChild((ViewGroup) v);
				if (check != null) return check;
			}
		}
		return null;
    }
    
    public boolean isChecked() { 
        return _checkbox != null ? _checkbox.isChecked() : false; 
    }
    
    public void setChecked(boolean checked) {
    	if (_checkbox != null) {
    		_checkbox.setChecked(checked);
    	}
    }
    
    public void toggle() { 
    	if (_checkbox != null) {
    		_checkbox.toggle();
    	}
    } 
} 