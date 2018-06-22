package ru.speechpro.stcspeechkit.media;

import android.media.AudioRecord;
import android.os.Handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.speechpro.stcspeechkit.util.Logger;

import static android.media.AudioRecord.STATE_INITIALIZED;

/**
 * @author Alexander Grigal
 */
public class AudioRecorder {

    private static final String TAG = AudioRecorder.class.getSimpleName();

    private static final int THREAD_COUNT = 3;

    private final int mBufferSize = 4096;

    private AudioRecord mAudioRecord;
    private AudioListener mAudioListener;
    private ByteArrayOutputStream mPcm;

    private boolean mStreaming;

    private ExecutorService mService = Executors.newFixedThreadPool(THREAD_COUNT);
    private Handler mHandler = new Handler();
    private Runnable mAudioRunnable;

    private volatile boolean mIsRecording;
    private volatile boolean mIsCanceled;
    private volatile boolean isReadyAudioListener;

    public void setAudioListener(AudioListener audioListener) {
        this.mAudioListener = audioListener;
    }

    public void removeAudioListener() {
        this.mAudioListener = null;
    }

    public void setStreamingMode(boolean isStreaming) {
        mStreaming = isStreaming;
    }

    public boolean isStreamingMode() {
        return mStreaming;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void start(int audioSource, int sampleRate, int channels, int audioEncoding) {
        Logger.INSTANCE.print(TAG, "AudioRecorder is started...");

        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channels, audioEncoding);
        int extBufferSize = minBufferSize * 4;
        Logger.INSTANCE.print(TAG, "Buffer size for recording is " + extBufferSize);

        mAudioRecord = new AudioRecord(audioSource, sampleRate, channels, audioEncoding, extBufferSize);

        if (mAudioRecord.getState() == STATE_INITIALIZED) {
            Logger.INSTANCE.print(TAG, "AudioRecord is initialized.");
        } else {
            Logger.INSTANCE.print(TAG, "AudioRecord is not initialized!");
        }

        mPcm = new ByteArrayOutputStream();
        mIsRecording = true;
        mIsCanceled = false;

        mService.execute(new Runnable() {
            @Override
            public void run() {
                mAudioListener.onStart();
                Logger.INSTANCE.print(TAG, "Recording is starting...");
                try {
                    mAudioRecord.startRecording();
                    write();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        Logger.INSTANCE.print(TAG, "AudioRecorder is stopped...");
        mIsRecording = false;
    }

    public void cancel() {
        Logger.INSTANCE.print(TAG, "AudioRecorder is canceled...");
        mIsCanceled = true;
        mHandler.removeCallbacks(mAudioRunnable);
    }

    public void release() {
        Logger.INSTANCE.print(TAG, "AudioRecorder is releasing...");
        mAudioRecord.release();
    }

    private short getShort(byte argB1, byte argB2) {
        return (short) (argB1 | (argB2 << 8));
    }

    private void write() {
        mHandler.postDelayed(mAudioRunnable = new Runnable() {
            @Override
            public void run() {
                isReadyAudioListener = true;
            }
        }, 300);

        do {
            final byte bData[] = new byte[mBufferSize];

            int amplitudeForChannel = 0;

            int read = mAudioRecord.read(bData, 0, mBufferSize);

            for (int i = 0; i < read / 2; i++) {
                short curSample = getShort(bData[i * 2], bData[i * 2 + 1]);
                if (curSample > amplitudeForChannel) {
                    amplitudeForChannel = curSample;
                }
            }

            if (mAudioListener != null) {
                if (mStreaming) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mIsCanceled) {
                                mAudioListener.onVoiceStream(bData);
                            }
                        }
                    });
                }

                if (isReadyAudioListener) {
                    final int amplitude = amplitudeForChannel;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mIsCanceled) {
                                mAudioListener.onProcess((short) amplitude);
                            }
                        }
                    });
                }
            }

            mPcm.write(bData, 0, mBufferSize);
        } while (mIsRecording && !mIsCanceled);

        try {
            Logger.INSTANCE.print(TAG, "Audio processing...");
            if (mAudioListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsCanceled) {
                            mAudioListener.onCancel();
                        } else {
                            mAudioListener.onStop(mPcm.toByteArray());
                        }
                    }
                });
            }
        } finally {
            Logger.INSTANCE.print(TAG, "Finalize...");
            mAudioRecord.release();
            Logger.INSTANCE.print(TAG, "Recorder is stopped and released.");
            try {
                mPcm.close();
            } catch (IOException e) {
                Logger.INSTANCE.withCause(TAG, "PCM stream cannot be closed: ", e);
            }
        }
    }
}
