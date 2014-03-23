package com.touchthink.obedient;

public class AudioSpeechException extends Exception {
	
	
	static final int SET_BUFF_ERROR = 1;
	static final int GET_BUFF_ERROR = 2;
	
	static final int SET_PACK_ERROR = 3;
	static final int GET_PACK_ERROR = 4;
	
	static final int NEW_BUFF_QUENCE_ERROR = 5;
	static final int NEW_PACK_QUENCE_ERROR = 6;

	static final int NEW_MEMORY_ERROR = 7;
	static final int NEW_ENGINE_ERROR = 8;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int error;
	
	public AudioSpeechException(int error,Exception ex)
	{
		super(ex);
		
		this.error = error;
		
		
	}

}
