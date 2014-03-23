package com.touchthink.obedient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.util.ArrayList;
import java.util.Enumeration;

public class CopyDataFileTask implements Runnable{
	  
	class FileInfo{
		  public String file_name;
		  public int file_length;
		  public String sub_path;
		  
		  public FileInfo(String file)
		  {
			file_name = file;
			file_length = 0;
		  }
		  
		  public FileInfo(String file,String path)
		  {
			file_name = file;
			sub_path = path;
			file_length = 0;
		  }
	}

	private OnInstallFileChangeListener listener;
	  
	private static int MAX_PART = 100;    //最多文件数

	private static final String DATABASE_PATH = "/data/com.touchthink.obedient/work/";
	private static final String SOURCE_PATH = "/mnt/sdcard2/Android/data/edu.cmu.pocketsphinx/";

	private String OBEDIENT_FILE_DIRECT = Environment.getDataDirectory().toString() + DATABASE_PATH;
//	private String HMM_FILE_DIRECT = Environment.getDataDirectory().toString() + DATABASE_PATH + "hmm/";
//	private String LM_FILE_DIRECT = Environment.getDataDirectory().toString() + DATABASE_PATH + "lm/";
	
	private String COMPRESS_NAME = "diclm";			//目标压缩文件
	private String ASSETS_NAME = "diclm";			//资源文件		
	
	  
	ArrayList<FileInfo> file_list;      //文件列表
	private String file_path;			//文件路径
	private Context context;			//数据
	private AssetManager asset;
	
	
	
	public CopyDataFileTask(Context context)
	{
		super();
		this.context = context;
		this.listener = (OnInstallFileChangeListener) context;
		this.file_path = Environment.getDataDirectory().toString() + DATABASE_PATH;
		this.asset = this.context.getAssets();
		init();
		
	}
	
	
	private void init()
	{
		//建立文件信息列表
		file_list = new ArrayList<FileInfo>();
		file_list.add(new FileInfo("IintheHere.wav")); 
		
		file_list.add(new FileInfo("feat.params","hmm")); 
		file_list.add(new FileInfo("means","hmm")); 
		file_list.add(new FileInfo("noisedict","hmm")); 
		file_list.add(new FileInfo("transition_matrices","hmm")); 
		file_list.add(new FileInfo("mdef","hmm")); 
		file_list.add(new FileInfo("sendump","hmm")); 
		file_list.add(new FileInfo("variances","hmm")); 

		file_list.add(new FileInfo("test.lm","lm")); 
		file_list.add(new FileInfo("test.dic","lm")); 
	}

	@Override
	public void run() {
		if(!"".equals(file_path))
		{
			//首先创建目录
			MakePath();
		
			//以下是建立资源文件所需要的
//			try {
//				CombinationFileBin();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//			try {
//				SplitFileBin(SOURCE_PATH + COMPRESS_NAME, 800000);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			
			//从资源中读取小文件导出到指定目录中组合成大文件
			try {
				mergerZpFile(ASSETS_NAME);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//将导出的组合文件分解成相应的文件及目录
			try {
				Reduction(this.file_path + COMPRESS_NAME);
				
				this.listener.OnStatusChange(1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
	 /**  
	   * 分割文件成固定大小的文件  
	   *   
	   * @param fileName 
	   * @param size    目标文件大小
	   * @throws IOException  
	   */   
	  public  void SplitFileBin(String fileName, int size) throws IOException {   
	    
		  InputStream is = new FileInputStream(fileName);   
	    
		  int len;   
		  int count = 1;   
		  
		  byte[] cbuf = new byte[size];   
	    
		  while ((len = is.read(cbuf, 0, size)) != -1) {   
			  OutputStream os = new FileOutputStream(fileName + "_" + count);   
			  os.write(cbuf, 0, len);   
			  os.close();   
	      
			  count++;   
			  if (count > MAX_PART) {   
				  break;   
			  }   
		  }   
		  
		  is.close();
		  
	  }   
	  

	  /**  
	   *  将列表中的文件全部合并到大文件中
	   *   
	   * @throws IOException  
	   */  
	  public  void CombinationFileBin() throws IOException {   
		    
		
	    OutputStream compress_x1 = new FileOutputStream(SOURCE_PATH  + COMPRESS_NAME + "x1");   
	    OutputStream compress = new FileOutputStream(SOURCE_PATH  + COMPRESS_NAME);   

	    
	    byte[] cbuf = new byte[1024 * 100];   
	    File file;   

		file = new File(this.file_path  + COMPRESS_NAME + "x1");   
		if(file.exists())
		{
			file.delete();

		}
	    
	    int len;
	    for (FileInfo fi : file_list) {
	    	file = new File(SOURCE_PATH + fi.file_name);   
		      
	    	if (!file.exists()) {   
		        break;   
	    	}
	    
	    	int file_len = 0;
	    	InputStream reader = new FileInputStream(file);   
		    while ((len = reader.read(cbuf, 0, cbuf.length)) != -1) {   
		        file_len += len;
		        
		        compress_x1.write(cbuf, 0, len);   
		        compress_x1.flush();
		    }   
		    
		    fi.file_length = file_len;
		    
		    //写入文件信息
//		    compress.write(fi.file_name.getBytes("ISO-8859-1"), 0, 30);
//		    compress.write(fi.sub_path.getBytes("ISO-8859-1"), 0, 10);
		    byte[] flen = intToBytes(file_len);
		    compress.write(flen);
		    compress.flush();
		    
		    flen = null;
		    reader.close();   
	    }
	    
	    
	    compress_x1.close();

	    //复制压缩数据到新的文件中
	    File zp_file = new File(SOURCE_PATH  + COMPRESS_NAME + "x1");   
	    InputStream reader = new FileInputStream(zp_file);   
  	
	    while ((len = reader.read(cbuf, 0, cbuf.length)) != -1) {   
	    	compress.write(cbuf, 0, len);   
	    	compress.flush();
	    }
	    
	    reader.close();
  	
	    compress.close();
	    
	    //删除x1文件
	    zp_file.delete();
	    
	  }
	  
	  
	  /**
	   * 从assert目录将需要合并的小文件复制出来合并成指定文件
	   * @throws IOException
	   */
	  public  void mergerZpFile(String filename) throws IOException {   
		  
		  //先删除掉文件
		  File file = new File(this.file_path + filename);
		  if(file.exists())
		  {
			  file.delete();
		  }
		  file = null;
		  
		  OutputStream writer = new FileOutputStream(this.file_path + filename);   
//		    OutputStream writer = new FileOutputStream(this.SOURCE_PATH + COMPRESS_NAME + "x2");   
		    
		  byte[] cbuf = new byte[1024 * 100];   
		  int len;
		    
		  for (int i = 1; i< 6; i++) {
		    	InputStream inputStream = null ;
		    	  
		    	try {
		    	   inputStream = asset.open(filename + "_" + i);
		    	} catch (IOException e) {
		    		
		    	}		    	
		    	
		    
			    while ((len = inputStream.read(cbuf, 0, cbuf.length)) != -1) {   
			        writer.write(cbuf, 0, len);   
			        writer.flush();
			    }   
			    			    
			    inputStream.close();  
			    inputStream = null;
		  }
		   
		  writer.close();
		  
	  }
		  

	  /**
	   * 还原文件，将目标文件还原成原文件和指定的目录 
	   * @throws IOException
	   */
	  public void Reduction(String filename) throws IOException {
		  
		  ArrayList<FileInfo> files = new ArrayList<FileInfo>();

		  //首先读取文件信息列表
		  InputStream is = new FileInputStream(filename);   
		  int len;   
		  
		  byte[] cbuf = new byte[1024 * 100];   
		  
		  for(FileInfo fi:this.file_list){
			  FileInfo ff = new FileInfo(fi.file_name,fi.sub_path);
			  len = is.read(cbuf,0,4);
			  
			  ff.file_length = bytesToInt(cbuf,0);
			  files.add(ff);
		  }
		  
		  
		  int read_len,remainder_len;
		  OutputStream source_write; 

		  String sub_file;
		  //再根据文件信息分别读取文件流到各自目录
		  for(FileInfo fi : files)
		  {
			  
			  if((fi.sub_path == null) ||  fi.sub_path.equals(""))
			  {
				  sub_file = this.file_path  + fi.file_name;
				  //source_write = new FileOutputStream(this.file_path  + fi.file_name);   
				  
			  } else {
				  sub_file = this.file_path  + fi.sub_path + "/" + fi.file_name;
				  //source_write = new FileOutputStream(this.file_path + fi.sub_path + "/" + fi.file_name);   
			  }
			  File file = new File(sub_file);
			  if(file.exists())
			  {
				  file.delete();
			  }
			  file.delete();
			  
			  source_write = new FileOutputStream(sub_file);   
				 
			    
			  remainder_len = fi.file_length;
			  while(remainder_len>0)
			  {
				  if(remainder_len > cbuf.length )
				  {
					  read_len = cbuf.length;
					  remainder_len -= cbuf.length;
				  } else {
					  read_len = remainder_len;
					  remainder_len = 0;
				  }
				  
				  len = is.read(cbuf, 0, read_len);
				  
				  if(read_len == len)
				  {
					  source_write.write(cbuf,0,len);
					  source_write.flush();
				  }
			  }
			  source_write.close();
		  }
		 
		  is.close(); 
	  }

     /**
      * 解压缩一个文件
      * 
      * @param zipFile
      * 要解压的压缩文件
      * @param folderPath
      * 解压缩的目标目录
      * @throws IOException
      * 当解压缩过程出错时抛出
      */
     public void upZipFile(File zipFile, String folderPath) 
    		  throws ZipException, IOException {
      
    	  File desDir = new File(folderPath);
      
    	  if (!desDir.exists()) {
    		  desDir.mkdirs();
    	  }
     
      
    	  ZipFile zf = new ZipFile(zipFile);
      
    	  for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
      
    		  ZipEntry entry = ((ZipEntry) entries.nextElement());
      
    		  InputStream in = zf.getInputStream(entry);
      
    		  String str = folderPath + File.separator + entry.getName();
      
    		  str = new String(str.getBytes("8859_1"), "GB2312");
      
    		  File desFile = new File(str);
      
    		  if (!desFile.exists()) {
      
    			  File fileParentDir = desFile.getParentFile();
      
    			  if (!fileParentDir.exists()) {
      
    				  fileParentDir.mkdirs();
      
    			  }
      
    			  desFile.createNewFile();
      
    		  }
      
    		  OutputStream out = new FileOutputStream(desFile);
      
    		  byte buffer[] = new byte[1024];
      
    		  int realLength;
      
    		  while ((realLength = in.read(buffer)) > 0) {
      
    			  out.write(buffer, 0, realLength);
      
    		  }
      
    		  in.close();
      
    		  out.close();
      
    	  }
      }

	public String getFileDirector()
	{

		File file = context.getFilesDir();  
		String path = file.getAbsolutePath() + File.pathSeparator + OBEDIENT_FILE_DIRECT + File.pathSeparator;  
		
		return path;
	}
	
	private int MakePath()
	{
		int error = 0;
//		try{
//			String dir = this.file_path;
//			File file_obedient = new File(dir);
//	    
//			boolean is = file_obedient.exists();//判断文件（夹）是否存在  
//			if(is){  
//				file_obedient.delete();  
//			} 
//
//			file_obedient = null;
//		}catch(Exception ex)
//		{
//			ex.printStackTrace();
//		}
		
		try{
			String dir = this.file_path;
			File file_obedient = new File(dir);
	    
			boolean is = file_obedient.exists();//判断文件（夹）是否存在  
			if(!is){  
				file_obedient.mkdir();//创建文件夹  
			} 

			file_obedient = null;
		}catch(Exception ex){
			error = -1;
		}
		
		try{
			String hmm_dir = this.file_path + "hmm";
			File file_hmm = new File(hmm_dir);
	    
			boolean is = file_hmm.exists();//判断文件（夹）是否存在  
			if(!is){  
				file_hmm.mkdir();//创建文件夹  
			}
			
			
			file_hmm = null;
		}catch(Exception ex){
			error = -2;
		}
	    try{
	    	String lm_dir = this.file_path + "lm";
	    	File file_lm = new File(lm_dir);
	    
	    	boolean is = file_lm.exists();//判断文件（夹）是否存在  
	    	if(!is){  
	    		file_lm.mkdir();//创建文件夹  
	    	}
	    	
	    	file_lm = null;
	    }catch(Exception ex){
	    	error = -3;
	    }
	    
	    return error;
	}
	
	/**
	* byte[]转换成int数
	* 
	* @param data
	*            包括int的byte[]
	* @param offset
	*            偏移量
	* @return int数
	*/
	public static int bytesToInt(byte[] data, int offset) {
	   int num = 0;
	   for (int i = offset; i < offset + 4; i++) {
	    num <<= 8;
	    num |= (data[i] & 0xff);
	   }
	   return num;
	}
	
	/**
	* int类型转换成byte[]
	* 
	* @param num
	*            int数
	* @return byte[]
	*/
	public static byte[] intToBytes(int num) {
	     
	     
	     
	   byte[] b = new byte[4];
	   for (int i = 0; i < 4; i++) {
	    b[i] = (byte) (num >>> (24 - i * 8));
	   }
	   return b;
	}
	
	
}
