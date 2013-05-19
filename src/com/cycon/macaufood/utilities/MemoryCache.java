package com.cycon.macaufood.utilities;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;

public class MemoryCache {
    private HashMap<String, SoftReference<Bitmap>> cache=new HashMap<String, SoftReference<Bitmap>>();
//    private HashMap<String, Bitmap> cache=new HashMap<String, Bitmap>();
    
    public Bitmap get(String id){
        if(!cache.containsKey(id))
            return null;
        SoftReference<Bitmap> ref=cache.get(id);
        return ref.get();
    }
    
    public void put(String id, Bitmap bitmap){
        cache.put(id, new SoftReference<Bitmap>(bitmap));
    }
    
//    public Bitmap get(String id){
//        if(!cache.containsKey(id))
//            return null;
//        Bitmap ref=cache.get(id);
//        return ref;
//    }
//    
//    public void put(String id, Bitmap bitmap){
//        cache.put(id, bitmap);
//    }

    public void clear() {
        cache.clear();
    }
}


