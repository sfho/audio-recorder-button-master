package br.com.safety.audio_recorder;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;


/**
 * @author netodevel
 */
public class AudioRecording {

    private String mFileName;
    private Context mContext;

    private MediaPlayer mMediaPlayer;
    private AudioListener audioListener;
    private MediaRecorder mRecorder;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;

    private File mRecAudioFile, mRecAudioPath;


    public AudioRecording(Context context) {
        mRecorder = new MediaRecorder();
        this.mContext = context;
    }

    public AudioRecording() {
        mRecorder = new MediaRecorder();
    }

    public AudioRecording setNameFile(String nameFile) {
        this.mFileName = nameFile;
        return this;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public AudioRecording start(AudioListener audioListener) {
        String path = mContext.getExternalFilesDir(null)+ "/../audio/";
        this.audioListener = audioListener;

        try {
            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            initRecAudioPath(path);
            mRecAudioFile = new File(path + mFileName);
            mContext.getSharedPreferences("audio", Context.MODE_PRIVATE).edit().putString("audio", mRecAudioFile.getAbsolutePath()).apply();
            mRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            this.audioListener.onError(e);
        }
        return this;
    }

    public void stop(Boolean cancel) {
            try {
                mRecorder.stop();
            } catch (RuntimeException e) {
                deleteOutput();
            }
            mRecorder.release();
            mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);

            RecordingItem recordingItem = new RecordingItem();
            recordingItem.setFilePath(mRecAudioFile.getAbsolutePath());
            recordingItem.setName(mFileName);
            recordingItem.setLength((int) mElapsedMillis);
            recordingItem.setTime(System.currentTimeMillis());

            if (cancel == false) {
                audioListener.onStop(recordingItem);
            } else {
                audioListener.onCancel();
            }
    }

    private void deleteOutput() {
        File file = new File(mRecAudioFile.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }
    }

    public void play(RecordingItem recordingItem) {
        try {
            this.mMediaPlayer = new MediaPlayer();
            this.mMediaPlayer.setDataSource(recordingItem.getFilePath());
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean initRecAudioPath(String path) {
        if (sdcardIsValid()) {

            mRecAudioPath = new File(path);

            if (!mRecAudioPath.exists()) {
                mRecAudioPath.mkdirs();
            }
        } else {
            mRecAudioPath = null;
        }
        return mRecAudioPath != null;
    }

    private boolean sdcardIsValid() {
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
        }
        return false;
    }

}
