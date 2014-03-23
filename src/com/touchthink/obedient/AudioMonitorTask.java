package com.touchthink.obedient;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import com.touchthink.obedient.Config;
import com.touchthink.obedient.Decoder;
import com.touchthink.obedient.Hypothesis;
import com.touchthink.obedient.pocketsphinx;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;




//声音监听线程

public class AudioMonitorTask implements Runnable {
		
	/**
	 * 状态.
	 */
	enum State {
		IDLE, LISTENING,DECODING,WAITE
	};
	/**
	 * 事件.
	 */
	enum Event {
		NONE, START, STOP, PLAY,SHUTDOWN
	};
	
	private static final String DATABASE_PATH = "/data/com.touchthink.obedient/work/";

	private String OBEDIENT_FILE_DIRECT = Environment.getDataDirectory().toString() + DATABASE_PATH;

	//数据队列 
	AudioBufferPool buffer;
	RecognitionListener listener;

	class PlayMessage
	{
		boolean play;
		
		public PlayMessage()
		{
			play= false;
		}
		
		public PlayMessage(boolean play)
		{
			this.play = play;
		}
		
	}
	
	
	//根据条件开启录音，录音数据保存到列表中，录音完毕开始解析，解析结束后休眠1s再重新录音
	class RecodeTask implements Runnable{
		AudioRecord recode_device;

		int sample_size;
		int buff_size;
		int block_size;

		AudioBufferPool pool;
		boolean done;

		RecodeTask(AudioBufferPool pool )
		{
			this.pool = pool;
			init(4096);		
		}
		
		void init(int block_size)
		{
			this.done = false;
			this.buff_size = 8192;
			this.sample_size = 8000;
			this.recode_device = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sample_size,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, 
					buff_size);
		}
		
		public int getBlockSize() {
			return block_size;
		}

		public void setBlockSize(int block_size) {
			this.block_size = block_size;
		}

		public void stop() {
			this.done = true;
		}

		
		@Override
		public void run() {
			Log.d(getClass().getName(), " 采集录音线程启动！");
			
			this.recode_device.startRecording();
			
			try{
			while (!this.done) {
				int nshorts = this.readBlock();
				if (nshorts <= 0)
					break;
			}
			} catch (AudioSpeechException e) {
				e.printStackTrace();
			}
			
			this.recode_device.stop();
			this.recode_device.release();
			
			Log.d(getClass().getName(), " 采集录音线程中止！");

		}
		
		int readBlock() throws AudioSpeechException {
			short[] buf = this.pool.getBuffer();
			int nshorts = this.recode_device.read(buf, 0, buf.length);
			if (nshorts > 0) {
				Log.d(getClass().getName(), " 采集" + nshorts + "数据到队列! ");
				this.pool.setPacket(buf);
			}
			return nshorts;
		}
		
	}
	
	//解码线程
	class DecoderTask implements Runnable {
		Config sphinx_config;
		Decoder sphinx_ps;
		boolean done;
		AudioBufferPool pool;
		RecognitionListener listener;
		int packet;

		DecoderTask(AudioBufferPool pool,RecognitionListener listener)
		{
			
			this.pool = pool;
			this.listener = listener;
			try {
				init();
			} catch (AudioSpeechException e) {
				e.printStackTrace();
			}
		}
		
		void init()
			throws AudioSpeechException
		{
			done = false;
			this.packet = -1;
			Log.d(getClass().getName(), " 解码线程初始化！");

			OBEDIENT_FILE_DIRECT = Environment.getDataDirectory().toString() + DATABASE_PATH;

			try{
				pocketsphinx.setLogfile("/mnt/sdcard2/Android/data/edu.cmu.pocketsphinx/pocketsphinx.log");
				this.sphinx_config  = new Config();

				this.sphinx_config.setString("-hmm",OBEDIENT_FILE_DIRECT  + "hmm");
				this.sphinx_config.setString("-dict",OBEDIENT_FILE_DIRECT  + "lm/test.dic");
				this.sphinx_config.setString("-lm",OBEDIENT_FILE_DIRECT  + "lm/test.lm");
				this.sphinx_config.setString("-rawlogdir", OBEDIENT_FILE_DIRECT);

				this.sphinx_config.setFloat("-samprate", 8000.0);
				this.sphinx_config.setInt("-maxhmmpf", 4000);
				this.sphinx_config.setInt("-maxwpf", 5);
				this.sphinx_config.setInt("-pl_window", 2);
				this.sphinx_config.setBoolean("-backtrace", true);
				this.sphinx_config.setBoolean("-bestpath", false);
				
				this.sphinx_ps = new Decoder(this.sphinx_config);
			}catch(Exception ex)
			{
				throw new AudioSpeechException(AudioSpeechException.NEW_ENGINE_ERROR,ex);
			}
		}

		public void stop()
		{
			done = true;
			//notifyAll();
		}
		
		@Override
		public void run() {
			Log.d(getClass().getName(), " 解码线程启动！");

			while(!done)
			{
				if(packet<0){
					if(this.pool.hasPacket())
					{
						packet = 0;
						Log.d(getClass().getName(), " 采集到足够录音！");

					} else {
						packet = -1;
						try {
                            Thread.sleep(1000);
						}catch (Exception e) {
						
						}
					}
				} else {
					packet++;
					if(packet>4)
					{
						//本轮采集结束，解析也完毕了，
						packet = -1;
						this.sphinx_ps.endUtt();
						
						Hypothesis hyp = this.sphinx_ps.getHyp();
						if (this.listener != null) {
							
							Bundle b = new Bundle();
							
							if (hyp == null) {
								Log.d(getClass().getName(), "Recognition failure");
//								
								b.putString("hyp", "NULL");
//								this.rl.onResults(b);								
								this.listener.onError(-1);
								
							}
							else {
								Log.d(getClass().getName(), "Final hypothesis: " + hyp.getHypstr());
								b.putString("hyp", hyp.getHypstr());
								
								this.listener.onResults(b);
							}
						}

						
					} else if(packet == 1){
						//设置开始说话点
						this.sphinx_ps.startUtt();
					} else {
						try{
							short[] buf = this.pool.getPacket();
							Log.d(getClass().getName(), "从列表读取 " + buf.length + "数据");
							this.sphinx_ps.processRaw(buf, buf.length, false, false);
							
							//归还数据到空缓冲区列表
							this.pool.setBackBuff(buf);
							
							
						}catch(AudioSpeechException ase)
						{
							ase.printStackTrace();
						}	
					}
					
				}
			}

			Log.d(getClass().getName(),"解码线程结束!");

		}
	}
	
	
	//播放录音线程
	class PlayTask implements Runnable{

		String voice_file;
		
		boolean done;
		AudioTrack audio_device;
		int sample_size;
		int buff_size;
		byte[] music_buff;
		int music_length;

		ArrayList<PlayMessage> messages;

		Timer time;
		
		public PlayTask(Event play_event){
			init();
		}
		
		
		private void init()
		{
			voice_file = OBEDIENT_FILE_DIRECT  + "IintheHere.wav";
			//voice_file = "/mnt/sdcard2/Android/data/edu.cmu.pocketsphinx/IintheHere.wav";
			
			this.sample_size = 8000;

			this.buff_size = 8192;
	        time = new Timer(true);
			this.messages = new ArrayList<PlayMessage>();

			this.audio_device = new AudioTrack(AudioManager.STREAM_MUSIC,
					this.sample_size, 
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
	                AudioFormat.ENCODING_PCM_16BIT, 
	                this.buff_size,
	                AudioTrack.MODE_STREAM);
			
	        
 
			Ready2TrackBack();

		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			Log.d(getClass().getName(),"播放线程启动");
			
			while(!done)
			{
				
				if(messages.size()>0)
				{
					PlayMessage msg = messages.remove(0);
					if(msg.play){
						this.audio_device.play();
						this.audio_device.write(this.music_buff, 0, this.music_length);
					}
					
				}
				
				try {
                    Thread.sleep(1000);
				}catch (Exception e) {
				
				}

			}
			this.audio_device.stop();
			this.audio_device.release();

		}
		
		public void stop()
		{
			this.done = true;
			//notify();
		}
		
		
		public void AddMessage(PlayMessage msg){
			this.messages.add(msg);
		}
		
 	    private void Ready2TrackBack() {
	    	  // Get the file we want to playback.
	    	  File file = new File(voice_file + "");
	    	  Log.d(getClass().getName(),"读取播放声音文件!");
	    	  
	    	  this.music_length = (int)(file.length());

	    	  
	    	  this.music_buff = new byte[this.music_length];
	    	  
	    	  try {
	    		  // Create a DataInputStream to read the audio data back from the saved file.
	    		  InputStream is = new FileInputStream(file);
	    		  BufferedInputStream bis = new BufferedInputStream(is);
	    		  DataInputStream dis = new DataInputStream(bis);
	    	    
	    		  // Read the file into the music array.
	    		  int i = 0;
	    	    
	    		  while (dis.available() > 0) {
	    			  this.music_buff[i] = dis.readByte();
	    			  i++;
	    		  }
	    	    
	    		  // Close the input streams.
	    	    
	    		  dis.close();    
	    	  } catch (Throwable t) {
	    	    Log.e("AudioTrack","Playback Failed");
	    	  }
	    	}
	}
	
	/**
	 * 当前事件.
	 */
	Event mailbox;
	
	boolean done;
	
	DecoderTask decoder;
	RecodeTask recoder;
	PlayTask player;
	
	Thread decoder_thread;  //解码线程
	Thread recoder_thread;  //录音线程
	Thread player_thread; 	//播音线程
	
	AudioMonitorTask(RecognitionListener listener)
	{
		this.done = false;
		this.listener = listener;
		
		mailbox = Event.NONE;
		try {
			this.buffer = new AudioBufferPool();
		} catch (AudioSpeechException e) {
			this.mailbox = Event.SHUTDOWN;
		}		
		
		//解码线程
		decoder = new DecoderTask(this.buffer,this.listener);
		decoder_thread = new Thread(decoder);  
		//录音线程
		recoder = new RecodeTask(this.buffer);
		recoder_thread = new Thread(recoder);  
		//
		player = new PlayTask(mailbox);
		player_thread = new Thread(player);  
	}
	
	
	@Override
	public void run() {
		
		decoder_thread.start();
		recoder_thread.start();
		player_thread.start();
		
		while(!done)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		decoder.stop();
		recoder.stop();
		player.stop();
		
		try {
			decoder_thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			recoder_thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			player_thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public void stop()
	{
		this.done = true;
//		notifyAll();
	}
	
	public void start()
	{
		synchronized(mailbox){
			this.mailbox = Event.START;
			//notifyAll();
		}
	}
	
	public void play(String message)
	{
		if("叶子云".equals(message) || "叶静怡".equals(message) || "我的手机".equals(message))
		{
			this.player.AddMessage(new PlayMessage(true));
		}
	}
	

}
