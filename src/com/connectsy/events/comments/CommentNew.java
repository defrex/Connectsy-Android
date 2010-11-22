package com.connectsy.events.comments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.connectsy.R;

public class CommentNew extends Activity implements OnClickListener {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_new);

        Button post = (Button)findViewById(R.id.comment_new_submit);
        post.setOnClickListener(this);

        Button cancel = (Button)findViewById(R.id.comment_new_cancel);
        cancel.setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.comment_new_submit){
	        String body = ((EditText)findViewById(R.id.comment_new_body))
	        		.getText().toString();
	        if (body.length() == 0){
				Toast t = Toast.makeText(this, 
						"What do you think this is, a no-comment form? Write a comment.", 5000);
				t.setGravity(Gravity.TOP, 0, 20);
				t.show();
	        }else{
				Intent i = new Intent();
				i.putExtra("com.connectsy.event.comment", body);
				setResult(RESULT_OK, i);
				finish();
	        }
			
		}else if (v.getId() == R.id.comment_new_cancel){
			setResult(RESULT_CANCELED);
			finish();
		}
	}
}
