package com.cycon.macaufood.utilities;

import android.util.Log;

public class ETLog {
	public static  boolean LOG = true;
	
	 public static final void e(String tag, String msg, Throwable ex){
	    	if( LOG )
	    	    Log.e(tag, msg, ex);
	    }
	 
    public static final void e(String tag, String msg){
    	if( LOG )
    	    Log.e(tag, msg);
    }
    public static final void i(String tag, String msg){
    	if( LOG )
    	    Log.i(tag, msg);
    }
    public static final void d(String tag, String msg){
    	if( LOG )
    	    Log.d(tag, msg);
    }
    public static final void w(String tag, String msg){
    	if( LOG )
    	    Log.w(tag, msg);
    }
    public static final void w(String tag, Throwable ex){
    	if( LOG )
    	    Log.w(tag, ex);
    } 
}
