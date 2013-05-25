package com.cycon.macaufood.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.cycon.macaufood.R;

public class Disclaimer extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.disclaimer);
		
		TextView text = (TextView) findViewById(R.id.text);
		text.setText(R.string.disclaimerText);
	}
}
