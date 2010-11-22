package com.connectsy;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;

public class NotificationBarManager {
	@SuppressWarnings("unused")
	private static final String TAG = "NotificationBarManager";

	private Activity activity;
//	private final View notifications;
//	private final View notificationsBody;
	private final int offset;
	private DisplayMetrics m = new DisplayMetrics();

	public NotificationBarManager(final Activity activity) {
		this.activity = activity;
		activity.getWindowManager().getDefaultDisplay().getMetrics(m);
		offset = (int)(m.heightPixels - (65*m.density));
		
//		notifications = activity.findViewById(R.id.notifications);
//		notifications.setPadding(0, offset, 0, 0);
//		
//		notificationsBody = activity.findViewById(R.id.notifications_body);
//		notificationsBody.setLayoutParams(
//				new LinearLayout.LayoutParams(m.widthPixels, offset));
//		
//		activity.findViewById(R.id.notification_bar).setOnClickListener(
//			new OnClickListener(){
//				public void onClick(View v) {
//					doAnimation(!(notifications.getPaddingTop() == 0));
//				}
//			});
		
	}
	
	private void doAnimation(boolean up){
		final int duration = 500;
		final int startOffset = 0;
		final float delta;
		final int top;
		final Interpolator interpol = AnimationUtils.loadInterpolator(activity,
        		android.R.anim.bounce_interpolator);
		if (up){
			delta = -offset;
			top = 0;
		}else{
			delta = offset;
			top = offset;
		}
        Animation a = new TranslateAnimation(
        		Animation.RELATIVE_TO_PARENT, 0.0f, 
        		Animation.RELATIVE_TO_PARENT, 0.0f, 
        		Animation.RELATIVE_TO_PARENT, 0.0f, 
        		Animation.ABSOLUTE, delta);
        a.setDuration(duration);
        a.setStartOffset(startOffset);
//        a.setInterpolator(interpol);
        a.setAnimationListener(new AnimationListener(){
			public void onAnimationEnd(Animation animation) {
//				notifications.setPadding(0, top, 0, 0);
			}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}
        });

//        Animation b;
//        if (up)
//        	b = new ScaleAnimation(0.0f, 0.0f, 0.0f, offset);
//        else
//        	b = new ScaleAnimation(0.0f, 0.0f, 0.0f, -offset);
//        b.setDuration(duration);
//        b.setStartOffset(startOffset);
//        b.setInterpolator(interpol);
//        b.setAnimationListener(new AnimationListener(){
//			public void onAnimationEnd(Animation animation) {
//				notificationsBody.setLayoutParams(
//						new LinearLayout.LayoutParams(m.widthPixels, offset));
//			}
//			public void onAnimationRepeat(Animation animation) {}
//			public void onAnimationStart(Animation animation) {}
//        });
//
//        notificationsBody.startAnimation(b);
//        notifications.startAnimation(a);
	}

}
