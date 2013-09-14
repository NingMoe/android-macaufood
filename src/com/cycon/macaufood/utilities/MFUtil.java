package com.cycon.macaufood.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;

import com.cycon.macaufood.bean.Cafe;

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
    
    public static String getDishesStringFromCafe(Cafe cafe) {
    	StringBuilder sb = new StringBuilder();
    	int dishes1 = Integer.parseInt(cafe.getType0() == null ? "0" : cafe.getType0());
    	int dishes2 = Integer.parseInt(cafe.getType1());
    	int dishes3 = Integer.parseInt(cafe.getType2());
    	int index1 = Arrays.asList(MFConstants.dishesId).indexOf(dishes1);
    	int index2 = Arrays.asList(MFConstants.dishesId).indexOf(dishes2);
    	int index3 = Arrays.asList(MFConstants.dishesId).indexOf(dishes3);
    	if (index1 != 0) {
    		sb.append(MFConstants.dishesType[index1]);
    	}
    	if (index2 != 0) {
    		sb.append(',');
    		sb.append(MFConstants.dishesType[index2]);
    	}
    	if (index3 != 0) {
    		sb.append(',');
    		sb.append(MFConstants.dishesType[index3]);
    	}
    	
    	return sb.toString();
    	
    }
    
    
    public static int getPixelsFromDip(float dips, Resources res){
  	  return  Math.round(dips * res.getDisplayMetrics().density);
    }  
    
    public static float getDipFromPixels(int pixel, Resources res) {
  	  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, res.getDisplayMetrics());
    }
    
    public static Bitmap getBitmapFromCache(FileCache fileCache, String imageId) {
    	File f=fileCache.getFile(imageId);
    	try {
			return BitmapFactory.decodeStream(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			return null;
		}
    }
    
    public static String getStringFromCache(FileCache fileCache, String stringId) {
    	String str = null;
    	File f = fileCache.getFile(stringId);
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			return null;
		}
		BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
		try {
			str = rd.readLine().trim();
			rd.close();
			return str;
		} catch (IOException e) {
			return null;
		}
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
