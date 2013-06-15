package com.cycon.macaufood.utilities;

import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Color;

import com.cycon.macaufood.R;

public class MFUtil {

	
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
//    public static int getRowDrawable(int row) {
//    	int remainder = row % 5;
//    	switch (remainder) {
//    	case 1:
//    		return R.drawable.row1_color;
//    	case 2:
//    		return R.drawable.row2_color;
//    	case 3:
//    		return R.drawable.row3_color;
//    	case 4:
//    		return R.drawable.row4_color;
//    	case 0:
//    		return R.drawable.row0_color;
//    				
//    	}
//		return 0;
//    }
}
