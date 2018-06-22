package ru.speechpro.stcspeechkit.util;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * AudioConverter class contains method for convert PCM to WAV
 *
 * @author Alexander Grigal
 */
public class AudioConverter {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

    private static final String TAG = AudioConverter.class.getSimpleName();

    /**
     * Convert PCM to WAV.
     *
     * @param context application context
     * @param value voice
     * @param sampleRateInHz
     * @return WAV
     * @throws IOException if an error occurred while converting
     */
    public static byte[] rawToWave(Context context, byte[] value, int sampleRateInHz) throws IOException {
        String sdf = SDF.format(new Date());
        String pcmPath = "pcm/pcm_" + sdf + ".pcm";
        String wavPath = "pcm/wav_" + sdf + ".wav";
        writeFile(context, pcmPath, value);

        Logger.INSTANCE.print(TAG, "PCM was saved in cache " + pcmPath);

        File pcmFile = new File(context.getCacheDir() + pcmPath);
        File wavFile = new File(context.getCacheDir() + wavPath);
        try {
            AudioConverter.rawToWave(pcmFile, wavFile, sampleRateInHz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.INSTANCE.print(TAG, "WAV was saved in " + wavFile);

        byte[] wavData = new byte[(int) wavFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(wavFile));
            try {
                input.read(wavData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            if (input != null) {
                input.close();
            }
        }

        return wavData;

    }

    private static void rawToWave(final File rawFile, final File waveFile, final int sampleRateInHz) throws IOException {
        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, sampleRateInHz); // sample rate
            writeInt(output, sampleRateInHz * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private static byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        try {
            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }

    private static void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private static void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private static void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    private static void writeFile(Context context, String path, byte[] value) {
        try {
            File file = new File(context.getCacheDir() + path);
            File folder = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('/')));
            folder.mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(value);
            fos.close();
        } catch (IOException e) {
            Logger.INSTANCE.withCause(TAG, e);
        }
    }

}
