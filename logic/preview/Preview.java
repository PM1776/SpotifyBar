package logic.preview;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.swing.JOptionPane;

import renderer.approvalbrowser.RedirectURIListener;

public class Preview implements AutoCloseable {
	
	/** The URL of the sound file to preview. */
	private String previewURL;

	/** Keeps track of how long the track has been playing in seconds. */
	private double currentSec;
	
	/** Keeps track of the seconds between starting the track
	 * and pausing it in seconds. */
	private double pausedTrackSecs;
	
	/** The current AudioInputStream of the player. */
    private AudioInputStream in;
    
    /** The current AudioFormat of the player. */
    private AudioFormat outFormat;
    
    /** The current DataLine.Info of the player. */
    private Info info;
    
    /** The current SourceDataLine created by <b>info</b>. */
    private SourceDataLine line;
    
    /** The Runnable interface to call run() while streaming. */
    private Runnable update;
    
    /** The duration of previews in seconds as a double. */
    private double duration;
    
    /** The state of the preview track, equal to <i>NOTSTARTED</i>, 
     * <i>PLAYING</i>, <i>PAUSED</i>, or <i>FINISHED</i>. */
    private int previewState = 0;
    
    /** Initialized value of <b>previewState</b>. */
    public static final int NOTSTARTED = 0;
    
    /** The value of the playing state of a preview as an int. */
    public static final int PLAYING = 1;
    
    /** The value of the paused state of a preview as an int. */
    public static final int PAUSED = 2;
    
    /** The value of the stopped state of a preview as an int. */
    public static final int STOPPED = 3;
    
    /** The value of the finished state of a preview as an int. */
    public static final int FINISHED = 4;
    
    /** Keeps track of if instance has been closed. */
    private boolean closed;
    
    /** A List of the listeners to call when the preview is halted for some reason 
     * (paused or stopped). */
    private List<HaltedListener> listeners = new ArrayList<HaltedListener>();
    
    public Preview () {}
    
    /** An instance set to play the sound file from the URL of <b>previewURL</b>.
     * 
     * @param previewURL A String of the URL to play.
     */
    public Preview (String previewURL) {
    	this(previewURL, null);
    }
    
    /** An instance set to play the sound file from the URL of <b>previewURL</b>, 
     * and runs the <b>update</b> Runnable interface while playing.
     * 
     * @param previewURL A String of the URL to play.
     * @param update A Runnable interface to call its run() while playing.
     */
    public Preview (String previewURL, Runnable update) {
    	this.previewURL = previewURL;
    	this.update = update;
    }
	
	/** 
	 * Starts playing the preview on a low priority <i>Thread</i> and runs a
	 * Runnable interface <b>update</b> while streaming the preview.
	 */
	public synchronized void playPreview() {
		
		if (!closed) {
			Thread trackThread = new Thread( () -> streamMp3() );
			trackThread.setPriority(3);
			trackThread.start();
		}
		
	}
	
	/** Pauses the preview. */
	public void pausePreview() {
		previewState = PAUSED;
	}
	
	/** Stopped preview and resets values to play again. */
	public void stopPreview () {
		previewState = STOPPED;
	}
    
    /** 
     * Plays the MP3 preview of the Spotify track with the Java Sound library
     * using an MP3 extension from tritonus.
     * 
     * @param previewURL the previewURL of the track returned from the Spotify API.
     * @author oldo (stackoverflow.com)
     * */
    private void streamMp3() {
    	
        try {
        	
        	// Establish audio connections if playing from the beginning
	        if (previewState == NOTSTARTED || previewState == STOPPED) {
        		URL url = new URL(previewURL);
        		in = getAudioInputStream(url);
        		
		        outFormat = getOutFormat(in.getFormat());
		        info = new Info(SourceDataLine.class, outFormat);
		        line = (SourceDataLine) AudioSystem.getLine(info);
		        
		        line.open(outFormat);
            	line.start();
        	}
        	
             /* 
             (2) In order to get the actual duration of the AudioInputStream,
             which can often be calculated using the frame length found in Java 
             Sound file formats, either the MPEGFormatFilerReader.class in the 
             mp3spi-1.9.5-1.jar Maven dependecy must be updated (frameLength always -1),
             or another library must be selected.
             */
	        duration = 30.0;
            
            // checks if current line is valid and still valid
            if (line != null) {
                stream(getAudioInputStream(outFormat, in), line);
            } else {
            	throw new RuntimeException("SourceDataLine 'line' is null");
            }
            
        } catch (MalformedURLException malex) {
        	throw new RuntimeException("The URL of the sound to play is invalid", malex);
        	
        } catch (UnsupportedAudioFileException | LineUnavailableException 
               | IOException e) {
        	throw new IllegalStateException(e);
        }
    }
 
    /** 
     * Returns an AudioFormat object in a format that supports MP3's and OGG Vorbis'.
     * 
     * @param inFormat the AudioFormat object that the new one bases on.
     * @return an AudioFormat object of an MP3, WAV and OGG Vorbis.
     * @author oldo (stackoverflow.com)
     * */
    private AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();

        final float rate = inFormat.getSampleRate();
        return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }
    
    /** 
     * Writes the newly created AudioInputStream in mp3 format to a SourceDataLine
     *  received from AudioSystem.
     * 
     * @param in An AudioInputStream to receive the data from.
     * @param line The SourceDataLine to write the data to.
     * @author oldo (stackoverflow.com)
     * */
    private void stream(AudioInputStream in, SourceDataLine line) 
        throws IOException {
    	
        double startMilli = (pausedTrackSecs == 0) ? System.currentTimeMillis() :
        		System.currentTimeMillis() - pausedTrackSecs * 1_000;
        
		previewState = PLAYING;
        
        int n;
        final byte[] buffer = new byte[4096];
        
        for (n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
        	
        	if (previewState == PLAYING) {
        		
        		line.write(buffer, 0, n);
        		 
        		// Calculates current second in the preview mp3
        		currentSec = (System.currentTimeMillis() - startMilli)
        				/ 1_000;
        		
        		// Updates display with the Runnable interface passed in
        		if (update != null) {
        			update.run();
        		}
        		
        	} else {
        		pausedTrackSecs = currentSec;
        		break;
        		
        	}
        }
        
        // finished playing track
        if (n == -1) {
        	previewState = FINISHED;
        	stopPreview();
        }
        
        // allows replay of preview from the beginning
        if (previewState == STOPPED) {
        	currentSec = 0.0;
    		pausedTrackSecs = 0.0;
        	closeAudioConnections();
        }
        
        for (HaltedListener phl : listeners) {
        	phl.halted();
        }
    }
    
    /** Adds a listener to be called when the preview halts (stops, pauses, or finishes).
     * 
     * @param hl A concrete Functional Interface whose halted() method is called when 
     * the preview halts.
     */
    public void addHaltedListener(HaltedListener hl) {
    	listeners.add(hl);
    }
    
    public String getPreviewURL() {
    	return previewURL;
    }
    public int getPreviewState() {
    	return previewState;
    }
    public double getDuration() {
    	return duration;
    }
    public double getCurrentSec() {
    	return currentSec;
    }

	@Override
	public void close() {
		
		closed = true;
		
		closeAudioConnections();
	}
	
	private void closeAudioConnections () {
		line.drain();
		line.stop();
		line.close();
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isClosed() {
		return closed;
	}
}
