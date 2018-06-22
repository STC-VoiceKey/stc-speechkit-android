package ru.speechpro.stcspeechkit.media;

/**
 * @author Alexander Grigal
 */
public interface AudioListener {
    void onStart();
    void onProcess(short amplitude);
    void onVoiceStream(byte[] stream);
    void onStop(byte[] result);
    void onCancel();
}
