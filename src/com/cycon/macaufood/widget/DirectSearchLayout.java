/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.cycon.macaufood.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.cycon.macaufood.Home;
import com.cycon.macaufood.Search;

public class DirectSearchLayout extends LinearLayout {
	
	private Search activity;
	
	public DirectSearchLayout(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public DirectSearchLayout(final Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setActivity(Search activity) {
		this.activity = activity;
	}
	
	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		
		InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm.isActive() && event.getAction() == KeyEvent.ACTION_DOWN &&  
            		event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            
            	activity.expand();
            }
		return super.dispatchKeyEventPreIme(event);
	}
	
}
	