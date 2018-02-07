package com.to.let.bd.common;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MyClickableSpan extends ClickableSpan {
	public static final String TAG = MyClickableSpan.class.getSimpleName();

	private MyClickableSpanListener mListener = null;

	private int mRequestedId;
	private String mSpanText;
	private boolean underline = false;
	private boolean bold = false;

	public MyClickableSpan(final String spanText, final int requestedId, final boolean bold) {
		super();
		this.mSpanText=spanText;
		this.mRequestedId = requestedId;
		this.bold = bold;
	}

	public MyClickableSpan(final String spanText, final int requestedId) {
		this(spanText,requestedId, false);
	}

	public void setOnClickListener(final MyClickableSpanListener listener) {
		this.mListener = listener;
	}

	@Override
	public void updateDrawState(final TextPaint ds) {
		super.updateDrawState(ds);
		ds.setUnderlineText(underline);

		if (bold)
			ds.setTypeface(Typeface.DEFAULT_BOLD);
	}

	@Override
	public void onClick(final View widget) {
		if (mListener != null) {
			mListener.onClick(mSpanText,mRequestedId);
		} else {
			Log.w(TAG, "no listener. clicked: " + mRequestedId);
		}
	}

	public static void makeClickable(final TextView textView) {
		// make sure TextView is clickable:
		final MovementMethod m = textView.getMovementMethod();
		if (m == null || !(m instanceof LinkMovementMethod)) {
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
}
