package com.cycon.macaufood.utilities;

import java.io.File;

import com.cycon.macaufood.bean.ImageType;

import android.content.Context;

public class FileCache {

	private File rootDir;
    private File cacheDir;
    private ImageType imageType;
    
    public FileCache(Context context, ImageType imageType){
    	this.imageType = imageType;
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        	rootDir=new File(android.os.Environment.getExternalStorageDirectory(),"MacauFood");
        else
        	rootDir=context.getCacheDir();
        if(!rootDir.exists())
        	rootDir.mkdirs();
        
        cacheDir=new File(rootDir, imageType.getValue());
        if (!cacheDir.exists())
        	cacheDir.mkdirs();
    }
    
    public File getFile(String id){
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