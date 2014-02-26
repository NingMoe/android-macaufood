package com.cycon.macaufood.utilities;

import java.io.File;

import android.content.Context;
import android.util.Log;

import com.cycon.macaufood.bean.ImageType;

public class FileCache {

	private File rootDir;
    private File cacheDir;
    private ImageType imageType;
    
    public FileCache(Context context, ImageType imageType){
    	this.imageType = imageType;
        //Find the dir to save cached images
    	
    	//should not save it in ext file, should save in file dir, wont clear when uninstall
        rootDir = context.getFilesDir();
        
        if(!rootDir.exists())
        	rootDir.mkdirs();
        
        cacheDir=new File(rootDir, imageType.getValue());
        if (!cacheDir.exists())
        	cacheDir.mkdirs();
    }
    
    public File getFile(String id){
    	Log.e("ZZZ", "get file id = " + id);
        String filename= id;
        File f = new File(cacheDir, filename + "-" + imageType.getValue());
        return f;
    }
    
    public void clearFile(String id) {
        String filename= id;
        File f = new File(cacheDir, filename + "-" + imageType.getValue());
        f.delete();
    }
    
    public void clear(){
        File[] files=cacheDir.listFiles();
        for(File f:files)
            f.delete();
    }

}