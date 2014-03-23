package com.touchthink.obedient;

import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;


/*
 * 缓冲区控制块，本类主要的工作是实线对于缓冲区的读写互斥控制，避免采集和解析线程因为缓冲区的问题而发生冲突。
 * 
 */


public class AudioBufferPool {

	static final int DEFAULT_BLOCK_SIZE = 1024;
	static final int DEFAULT_PACKET_SIZE = 3;

	
	//定义空缓冲区和数据缓冲区
	LinkedBlockingQueue<short[]> null_quence;
	LinkedBlockingQueue<short[]> data_quence;

	//默认的缓冲区大小
	int block_size;
	int quence_size;
	
	int min_pack_num;
	int min_buff_num;
	

	public AudioBufferPool() throws AudioSpeechException
	{
		init(DEFAULT_PACKET_SIZE * 3,DEFAULT_BLOCK_SIZE * 4);
	}
	
	public AudioBufferPool(int block_size) throws AudioSpeechException
	{
		init(DEFAULT_PACKET_SIZE * 3,block_size);
	}

	public AudioBufferPool(int quence_size,int block_size) throws AudioSpeechException
	{
		init(quence_size,block_size);
	}
	

	private void init(int quence_size,int block_size)			
			throws AudioSpeechException
	{
		this.min_pack_num = DEFAULT_PACKET_SIZE;
		this.min_buff_num = DEFAULT_PACKET_SIZE;

		this.block_size = block_size;
		this.quence_size = quence_size;
		try{
			null_quence = new LinkedBlockingQueue<short[]>(this.quence_size);
		}catch(Exception ex){
			throw new AudioSpeechException(AudioSpeechException.NEW_BUFF_QUENCE_ERROR,ex);
		}
		
		try{
			data_quence = new LinkedBlockingQueue<short[]>(this.quence_size);
		}catch(Exception ex){
			throw new AudioSpeechException(AudioSpeechException.NEW_PACK_QUENCE_ERROR,ex);
		}
		
		try{
		for(int i = 0; i < this.quence_size; i++)
		{
			short[] buf = new short[this.block_size];
			null_quence.add(buf);
		}
		}catch(Exception ex){
			throw new AudioSpeechException(AudioSpeechException.NEW_MEMORY_ERROR,ex);
		}
		
	}
	
	//是否满足数据包需求
	public boolean hasPacket()
	{
		
		Log.d(getClass().getName(), " 采集数据包:"+ this.data_quence.size());

		return this.data_quence.size() > (min_pack_num - 1) ? true:false;	
	}
	//获取数据包
	public synchronized short[] getPacket()
			throws AudioSpeechException
	{

		while (data_quence.size() == 0) {  
            try {  
                wait();  
            } catch (InterruptedException e) {  
    			throw new AudioSpeechException(AudioSpeechException.GET_PACK_ERROR,e);
            }  
        }  
		
		short[] buf = null;
		try{
			buf = this.data_quence.poll();
		}catch(Exception e)
		{
			buf = null;
			throw new AudioSpeechException(AudioSpeechException.GET_PACK_ERROR,e);
		}finally{
			notify();
		}
		return buf;
	}
	//写新的数据包到列表
	public synchronized void setPacket(short[] buf)
			throws AudioSpeechException
	{
		while (data_quence.size() > (this.min_pack_num * 3 - 1)) {  
            try {  
                wait();  
            } catch (InterruptedException e) {  
    			throw new AudioSpeechException(AudioSpeechException.SET_BUFF_ERROR,e);
            }  
        }  

		
		try{
			this.data_quence.add(buf);
		}catch(Exception e)
		{
			throw new AudioSpeechException(AudioSpeechException.SET_PACK_ERROR,e);
		}finally{
			notify();
		}
	}
	
	//是否有足够的空缓冲区
	public boolean hasBuff()
	{
		return this.null_quence.size() > (min_buff_num - 1) ? true:false;	
	}

	//获取空缓冲区
	public synchronized short[] getBuffer()
			throws AudioSpeechException
	{
		while (null_quence.size() == 0) {  
            try {  
                wait();  
            } catch (InterruptedException e) {  
    			throw new AudioSpeechException(AudioSpeechException.GET_BUFF_ERROR,e);
            }  
        }  
		
		short[] buf = null;
		try{
			buf = this.null_quence.poll();
		}catch(Exception e)
		{
			buf = null;
			throw new AudioSpeechException(AudioSpeechException.GET_BUFF_ERROR,e);
		}finally{
			notify();
		}
		
		
		return buf;
	}
	//归还空缓冲区
	public synchronized void setBackBuff(short[] buf) 
			throws AudioSpeechException
	{
		while (null_quence.size() > (this.min_buff_num * 3 - 1)) {  
            try {  
                wait();  
            } catch (InterruptedException e) {  
    			throw new AudioSpeechException(AudioSpeechException.SET_BUFF_ERROR,e);
            }  
        }  
		
		try{
			this.null_quence.add(buf);
		}catch(Exception e)
		{
			throw new AudioSpeechException(AudioSpeechException.SET_BUFF_ERROR,e);
		}finally{
			notify();
		}
	}
	
	@Override
	public void finalize() {
	  System.out.println("I'm ending.");
	  
	  while(null_quence.size()>0)
	  {
		  short[] buf = null_quence.poll();
	  }

	  while(data_quence.size()>0)
	  {
		  short[] buf = data_quence.poll();
	  }
		  
	}
	
}
