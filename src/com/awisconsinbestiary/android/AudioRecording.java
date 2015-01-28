/*
 * No code on this page is mine. This code was posted by Chetana on October 23, 2012 found
 * on the website http://www.edumobile.org/android/android-development/audio-recording-in-wav-format-in-android-programming/
 */

package com.awisconsinbestiary.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AudioRecording extends Activity {
	private static byte RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "UWOBestiary";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;

	// used to play the song
	MediaPlayer mp;

	//Used to store the filename for the recorded audio
	String pathForAudioFile;
	String oldPathForAudioFile;
	
	Button play;
	Button stop;
	Button start;
	Button save;
	
	int colorOn;
	int colorOff;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_recording);


		start = ((Button) findViewById(R.id.btnStart));
		stop = ((Button) findViewById(R.id.btnStop));
		save = ((Button) findViewById(R.id.btnSave));
		play = ((Button) findViewById(R.id.btnPlay));
		
		colorOff = Color.rgb(146, 185, 212);
		colorOn = Color.rgb(37, 116, 169);
		


		enableButtons(false);

		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

		//New media player
		mp = new MediaPlayer();

		if(NewSubmission.e.getAudioPath() != null)
		{
			pathForAudioFile = NewSubmission.e.getAudioPath();
		}
		else
		{
			pathForAudioFile = null;
		}
		
		setButtonHandlers();

		//Set null file paths
		oldPathForAudioFile = null;
	}

	private void setButtonHandlers() {
		((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.btnSave)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.btnPlay)).setOnClickListener(btnClick);
		
		if(pathForAudioFile == null)
		{
			play.setBackgroundColor(colorOff);
			play.setEnabled(false);
			
			stop.setBackgroundColor(colorOff);
			stop.setEnabled(false);
			
			save.setBackgroundColor(colorOff);
			save.setEnabled(false);
		}
		else
		{
			stop.setBackgroundColor(colorOff);
			stop.setEnabled(false);
			
			play.setBackgroundColor(colorOn);
			play.setEnabled(true);
			
			
		}
	}

	private void enableButton(int id, boolean isEnable) {
		((Button) findViewById(id)).setEnabled(isEnable);
	}

	private void enableButtons(boolean isRecording) {
		enableButton(R.id.btnStart, !isRecording);
		enableButton(R.id.btnStop, isRecording);
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private String getTempFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

		if (tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}

	private void startRecording() {
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);

		int i = recorder.getState();
		if (i == 1)
			recorder.startRecording();

		isRecording = true;

		recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");

		recordingThread.start();
	}

	private void writeAudioDataToFile() {
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int read = 0;

		if (null != os) {
			while (isRecording) {
				read = recorder.read(data, 0, bufferSize);

				if (AudioRecord.ERROR_INVALID_OPERATION != read) {
					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void stopRecording() {
		if (null != recorder) {
			isRecording = false;

			int i = recorder.getState();
			if (i == 1)
				recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;
		}

		copyWaveFile(getTempFilename(), pathForAudioFile);
		deleteTempFile();
	}

	private void deleteTempFile() {
		File file = new File(getTempFilename());

		file.delete();
	}

	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = 2;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

		byte[] data = new byte[bufferSize];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);

			while (in.read(data) != -1) {
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}

	private void deleteOldFile(String path) {
		File fdelete = new File(path);
		if (fdelete.exists()) {
			if (fdelete.delete()) {
				Log.d("here", "deleted " + path);
			} else {
				Log.d("here", "not deleted " + path);
			}
		} else {
			Log.d("doesn't exist", "not deleted " + path);
		}

	}

	private View.OnClickListener btnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnStart: {
				oldPathForAudioFile = pathForAudioFile;
				if(oldPathForAudioFile != null)
				{
					deleteOldFile(oldPathForAudioFile);
				}
				pathForAudioFile = getFilename();
				
				start.setEnabled(false);
				start.setBackgroundColor(colorOff);
				
				stop.setEnabled(true);
				stop.setBackgroundColor(colorOn);
				
				startRecording();

				break;
			}
			case R.id.btnSave: {
				NewSubmission.e.setAudioPath(pathForAudioFile);
				finish();

				break;
			}
			case R.id.btnPlay: {
				//PLAY LOGIC
				try {
					mp.reset(); 
			        mp.setDataSource(pathForAudioFile);
			        mp.prepare();
			        mp.start();
			    } catch (Exception e) {
			        e.printStackTrace();
			    }


				break;
			}
			case R.id.btnStop: {
				enableButtons(false);
				start.setEnabled(true);
				start.setBackgroundColor(colorOn);
				
				play.setEnabled(true);
				play.setBackgroundColor(colorOn);
				
				save.setBackgroundColor(colorOn);
				save.setEnabled(true);
				
				stop.setBackgroundColor(colorOff);
				stop.setEnabled(false);
				stopRecording();

				break;
			}
			}
		}
	};
}