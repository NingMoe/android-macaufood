package com.cycon.macaufood.utilities;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.Resources;
import android.util.TypedValue;

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
    
    
    public static int getPixelsFromDip(float dips, Resources res){
  	  return  Math.round(dips * res.getDisplayMetrics().density);
    }  
    
    public static float getDipFromPixels(int pixel, Resources res) {
  	  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, res.getDisplayMetrics());
    }
    
    public static FlushedInputStream flushedInputStream(InputStream is) {
    	return new FlushedInputStream(is);
    }
    
	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int bytes = read();
					if (bytes < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
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
