package savedCodeSnipits;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javazoom.jl.player.advanced.AdvancedPlayer;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class learningAboutAPIs {
	
	public static void learningAboutAPIs () {	
		
		// Host url
	    String host = "https://community-open-weather-map.p.rapidapi.com/weather";
	    
	    // Headers for a request
	    String x_rapidapi_host = "community-open-weather-map.p.rapidapi.com";
	    String x_rapidapi_key = "c41285f392msh07bcee071c1c27fp1eb3fejsnbb6cf7c8996f";
	    
	    // Params
        String q = "London, uk";
        
	    // Format query for preventing encoding problems
        String charset = "UTF-8";
        try {
        	q = String.format("q=%s",
		    URLEncoder.encode(q, charset));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		//use Unirest to receive a perceived JSonNode package
        HttpResponse<JsonNode> response = null;
		try {
			response = Unirest.get(host + "?" + q)
			.header("x-rapidapi-host", x_rapidapi_host)
			.header("x-rapidapi-key", x_rapidapi_key)
			.asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		// Code to do it without unirest
//	 	HttpRequest request = HttpRequest.newBuilder()
//			.uri(URI.create("https://community-open-weather-map.p.rapidapi.com/forecast?q=san%20francisco%2Cus"))
//			.header("x-rapidapi-key", "c41285f392msh07bcee071c1c27fp1eb3fejsnbb6cf7c8996f")
//			.header("x-rapidapi-host", "community-open-weather-map.p.rapidapi.com")
//			.method("GET", HttpRequest.BodyPublishers.noBody())
//			.build();
//		HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
//		System.out.println(response.body());

		// Print Content-Type of api response (in this case, a json Object [could be an image, etc.])
		System.out.print(response.getHeaders().get("Content-Type"));
		
		// gson library prettifies the Json String and prints in console
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    JsonElement je = new JsonParser().parse(response.getBody().toString());
	     // ref (without unirest) - 	       (response.body());
	    String prettyJsonString = gson.toJson(je);
	    
	    System.out.println(prettyJsonString);
	    
	    //to get a specific the string of a specific JsonObject,
	    JsonObject rootObject = je.getAsJsonObject();
	    String visibility = rootObject.get("visibility").getAsString(); // get property "visibility"
	    JsonObject windObject = rootObject.getAsJsonObject("wind"); // get weather object
	    String windspeed = windObject.get("speed").getAsString(); // get property "description"
	    
	    System.out.println("visibility: " + visibility); 
	    System.out.println("wind speed: " + windspeed);
	    
	    //if content-type were perhaps an image,
	    /* HttpResponse response would end with property .asBinary()
	       
	       //Image saving
      	   InputStream is = httpResponse.getRawBody();
      	   BufferedImage inputStreamImage = ImageIO.read(is);
      	   File image = new File("image.jpg");
      	   ImageIO.write(inputStreamImage, "jpg", image);
	     */
	    
	    
	    // Code to convert path to URL by converting to a URI first ("file:D:/",
	    // switches to forward slashes)
		
	    /*    
			url = Paths.get(savDir, "previewMP3.mp3").toUri().toURL(); 				 
		*/
	}
	
	public void learningAboutAudioPlayers() {
		
		//plays audio from a URL (only supports WAV, AU, and AIFF however)
		
		AudioInputStream din = null;
		URL url = null; //url to play
		
        try {
        	AudioInputStream in = AudioSystem.getAudioInputStream(url);
        	AudioFormat baseFormat = in.getFormat();
        	AudioFormat decodedFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
            baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
            false);
        	din = AudioSystem.getAudioInputStream(decodedFormat, in);
        	DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
        	SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        	if (line != null) {
        		line.open(decodedFormat);
        		byte[] data = new byte[4096];
        		int nBytesRead = 0;
        		line.start();
        		while ((nBytesRead = din.read(data, 0, data.length)) != -1) {
        			line.write(data, 0, nBytesRead);
		            line.drain();
		            line.stop();
		            line.close();
		            din.close();
		            try { din.close(); } catch(IOException e) { 
		            	System.out.println("Failed");
		            }
        		}
        	}
        } catch (Exception e) {
        	System.out.println("Could not play audio from preivew URL");
        	e.printStackTrace();
        }
        
        //----------------------------------------------------------------------------
        
        // MediaPlayer of JavaFX plays mp3s
        
        /*
	        String bip = "bip.mp3";
	        Media hit = new Media(new File(bip).toURI().toString());
	        MediaPlayer mediaPlayer = new MediaPlayer(hit);
	        mediaPlayer.play();
        /*
        
        // with imports:
         */
        /* 
            import java.io.File;
			import javafx.scene.media.Media;
			import javafx.scene.media.MediaPlayer;
         */
        
        //----------------------------------------------------------------------------
        
        //Additionally, JLayer plays mp3s from files
        
        // Dependency in Maven:
        
        /*
	         <dependency>
	           <groupId>javazoom</groupId>
	           <artifactId>jlayer</artifactId>
	           <version>1.0.1</version>
	  		 </dependency>
         */
        
        // The code (pause functionality not working):
        
        /*
	        static int pausedOnFrame = 0;
			static AdvancedPlayer player;
			static boolean paused = false;
	        
	        FileInputStream fin;
			try {
				fin = new FileInputStream(previewFile);
				player = new AdvancedPlayer(fin);
				player.setPlayBackListener(new PlaybackListener() {
					@Override
				    public void playbackFinished(PlaybackEvent event) {
				        pausedOnFrame = event.getFrame();
				    }
				});
				if (pausedOnFrame != 0) {
					player.play(pausedOnFrame, 363885);
				} else {
					player.play(pausedOnFrame, 363885);
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (JavaLayerException e) {
				e.printStackTrace();
			}
        */
        
        // ---------------------------------------------------------------
        
        //BigClip class developed by Andrew Thomson (stackoverflow.com), a Clip
        //class that can play an audio size only limited to memory.
        
        // ---------------------------------------------------------------
        
        // Applied method to play mp3s
        
        // Dependency in Maven: 
        
        /*
          	   <!-- 
			    We have to explicitly instruct Maven to use tritonus-share 0.3.7-2 
			    and NOT 0.3.7-1, otherwise vorbisspi won't work.
			   -->
				<dependency>
			  	  <groupId>com.googlecode.soundlibs</groupId>
			  	  <artifactId>tritonus-share</artifactId>
			  	  <version>0.3.7-2</version>
				</dependency>
				<dependency>
			  	  <groupId>com.googlecode.soundlibs</groupId>
			  	  <artifactId>mp3spi</artifactId>
			  	  <version>1.9.5-1</version>
				</dependency>
				<dependency>
			  	  <groupId>com.googlecode.soundlibs</groupId>
			  	  <artifactId>vorbisspi</artifactId>
			  	  <version>1.0.3-1</version>
				</dependency>
         */
        
        // The code (the simple implementation, as PlayerLogic.java can pause as well):
        
        /*
         	import java.io.File;
			import java.io.IOException;
			
			import javax.sound.sampled.AudioFormat;
			import javax.sound.sampled.AudioInputStream;
			import javax.sound.sampled.AudioSystem;
			import javax.sound.sampled.DataLine.Info;
			import javax.sound.sampled.LineUnavailableException;
			import javax.sound.sampled.SourceDataLine;
			import javax.sound.sampled.UnsupportedAudioFileException;
			
			import static javax.sound.sampled.AudioSystem.getAudioInputStream;
			import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
			
			public class AudioFilePlayer {
			 
			    public static void main(String[] args) {
			        final AudioFilePlayer player = new AudioFilePlayer ();
			        player.play("something.mp3");
			        player.play("something.ogg");
			    }
			 
			    public void play(String filePath) {
			        final File file = new File(filePath);
			 
			        try (final AudioInputStream in = getAudioInputStream(file)) {
			             
			            final AudioFormat outFormat = getOutFormat(in.getFormat());
			            final Info info = new Info(SourceDataLine.class, outFormat);
			 
			            try (final SourceDataLine line =
			                     (SourceDataLine) AudioSystem.getLine(info)) {
			 
			                if (line != null) {
			                    line.open(outFormat);
			                    line.start();
			                    stream(getAudioInputStream(outFormat, in), line);
			                    line.drain();
			                    line.stop();
			                }
			            }
			 
			        } catch (UnsupportedAudioFileException 
			               | LineUnavailableException 
			               | IOException e) {
			            throw new IllegalStateException(e);
			        }
			    }
			 
			    private AudioFormat getOutFormat(AudioFormat inFormat) {
			        final int ch = inFormat.getChannels();
			
			        final float rate = inFormat.getSampleRate();
			        return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
			    }
			 
			    private void stream(AudioInputStream in, SourceDataLine line) 
			        throws IOException {
			        final byte[] buffer = new byte[4096];
			        for (int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
			            line.write(buffer, 0, n);
			        }
			    }
			}
         */
        
        //----------------------------------------------------------------------------
        
        // Mp3 playing method chosen because I knew AudioInputStream from Java Sound
        // could play from a URL rather than a File, and the external libraries were
        // simply an extension for Java Sound to play the imported dependency extensions.
        // Java Sound is a powerful library that was designed to be able to create 
        // music studio applications.
	}
	
	private static final int BUFFER_SIZE = 4096;
	 
    /**
     * Downloads a file from a URL.
     * 
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     * @return downloaded file as File
     */
    public static File downloadFile(String fileURL, String saveDir)
            throws IOException {

    	File file = null;
    	
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        String saveFilePath = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {
        	String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                    
                }
            } else {
                /* extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());*/
            	fileName = "previewMP3.mp3";
            }
            
            // Prints file info
            //System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);//
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            saveFilePath = saveDir + File.separator + fileName;
            file = new File(saveFilePath);
            
            file = File.createTempFile(fileName, ".mp3");
           	
	        // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

	        int bytesRead = -1;
	        byte[] buffer = new byte[BUFFER_SIZE];
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	        	outputStream.write(buffer, 0, bytesRead);
	        }
	 
	        outputStream.close();
	        inputStream.close();
	        
	        System.out.println("File downloaded");
            
	        // To download a file with NIO (possible multiple channels at once)
    		/*InputStream is = songPreview.getRawBody();
    		ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
    		FileOutputStream fileOutputStream = new FileOutputStream("\\song.mp3");
    		FileChannel fileChannel = fileOutputStream.getChannel();
    		fileOutputStream.getChannel()
    		  .transferFrom(readableByteChannel, 0, Long.MAX_VALUE); */

        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
        
        file = new File(saveFilePath);
        return file;
    }
    
    /**
     * Converts a given Image into a BufferedImage.
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        
    	if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
    
    static JEditorPane browser = new JEditorPane();
    
    /** 
     * Creates a browser (only runs HTML, not CSS or JavaScript) that goes to
     * specified <b>url</b>.
     * 
     * @param url the URL to load.
     */
    public static void createABrowser (URL url) {
    	
    	JFrame frame = new JFrame("Browser");
    	
		frame.setPreferredSize(new Dimension(400, 400));
		frame.setLayout(new BorderLayout());
		
		browser = new JEditorPane();
		browser.setEditable(false); // to allow it to generate HyperlinkEvents
		browser.addHyperlinkListener(new HyperlinkListener() {
		    public void hyperlinkUpdate(HyperlinkEvent e) {
		        if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
		            browser.setToolTipText(e.getDescription());
		        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
		            browser.setToolTipText(null);
		        } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		            try {
		                browser.setPage(e.getURL());
		            } catch (IOException ex) {
		                ex.printStackTrace();
		            }
		        }
		    }
		});
		
		try {
			browser.setPage(url);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		frame.getContentPane().add(browser, BorderLayout.CENTER);
		
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
    }
}
