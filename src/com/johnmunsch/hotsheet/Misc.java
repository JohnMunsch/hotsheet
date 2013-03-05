/*
 * Misc.java
 *
 * Created on July 17, 2002, 10:16 PM
 */
package com.johnmunsch.hotsheet;

import java.io.*;
import javax.sound.sampled.*;

import org.apache.log4j.*;

/**
 *
 * @author  John Munsch
 */
public class Misc {
    private static Category cat = Category.getInstance(Misc.class.getName());

    public static void playSound(String filename) {
        try {
            // From file.
            AudioInputStream stream = AudioSystem.getAudioInputStream(
                filename.getClass().getResource(filename));
            
            // At present, ALAW and ULAW encodings must be converted to
            // PCM_SIGNED before they can be played.
            AudioFormat format = stream.getFormat();
            
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    format.getSampleSizeInBits() * 2,
                    format.getChannels(),
                    format.getFrameSize() * 2,
                    format.getFrameRate(),
                    true);                                  // big endian
                stream = AudioSystem.getAudioInputStream(format, stream);
            }
            
            DataLine.Info info = new DataLine.Info(Clip.class,
                stream.getFormat(), ((int) stream.getFrameLength() * 
                format.getFrameSize()));
            
            Clip clip = (Clip) AudioSystem.getLine(info);
            
            // This method does not return until the audio file is completely
            // loaded.
            clip.open(stream);
            
            // Start playing.
            clip.start();
        } catch (IOException ioe) {
            cat.error(ioe);
        } catch (LineUnavailableException lue) {
            cat.error(lue);
        } catch (UnsupportedAudioFileException uafe) {
            cat.error(uafe);
        }
    }
}
