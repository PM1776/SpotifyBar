package renderer.images;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import logic.playerlogic.PlayerLogic;
import net.coobird.thumbnailator.Thumbnails;
import renderer.display.Display;

/** A class that uses the <i>Graphics</i> package to create images displayed, 
 * storing them in <i>BufferedImage</i> constants. Creating BufferedImages
 * using the Graphics package allows their scale to be altered simply by changing
 * <b>scale</b> without loosing resolution. The only image loaded from a file is
 * the application's logo icon.
 * 
 * </br></br>P.S. If it can be made with the default tools in Paint.net, it can be made 
 * with the Graphics package quite easily, as nearly all the default tools 
 * in Paint.net represent functions and their parameter inputs from the Graphics 
 * library with a GUI.
 * 
 * @version 2022-2-9
 * @author Paul Meddaugh
 */
public class Images {
	
	/** The width of the background image, and, therefore, the JFrame. */
	private int WIDTH;
	
	/** The height of the background image, and, therefore, the JFrame. */
	private int HEIGHT;
	
	/** The scale that album covers are scaled down to, which is * by the size it will
	 * be displayed into, with 1 equating its displayed size, and 4 being
	 * 4 * that size. A higher scale will increase the quality. */
	private int albumCoverScale;
	
	/** The background <i>BufferedImage</i> of the Spotify player, composed of the black
	 * background, Spotify logo, search bar underline, and anchor symbol on the left. */
	public final BufferedImage background;
	
	/** The 'X' button of the search Bar in a <i>BufferedImage</i>. */
	public final BufferedImage searchX;
	
	/** The 'play' <i>BufferedImage</i> for the Play/Pause button. */
	public final BufferedImage play;
	
	/** The 'pause' <i>BufferedImage</i> for the Play/Pause button. */
	public final BufferedImage pause;
	
	/** A <i>BufferedImage</i> of a track bar with no playing progress on it
	 * or circle at the current position of the track. */
	public final BufferedImage blankTrackBar;
	
	public final BufferedImage closeIcon;
	
	/** The logo for the application loaded from a PNG file. */
	public final BufferedImage icon;
	
	/** The blank album cover BufferedImage for when no album has been loaded. */
	public final BufferedImage blankAlbumCover;
	
	/** The previous BufferedImage for going to the previous track. */
	public final BufferedImage previous;
	
	/** The forward BufferedImage for going to next track. */
	public final BufferedImage forward;
	
	/** The 'que' button in the search Bar to add a song to the que. */
	public final BufferedImage addToQueue;
	
	private final String IMAGE_DIR = ""; // "/resources" for executable jar
	
	/** The multiplier scale that BufferedImages are drawn from their original size. */
	private final int scale;
	
	/** Creates BufferedImages designed for this program to a scale of 1, with 1030x108
	 * dimensions for the background BufferedImage and an album cover scale of 1 for 
	 * blankAlbumCover BufferedImage.
	 */
	public Images () {
		this(1, 1030, 108, 1);
	}
	
	/** Creates BufferedImages designed for this program with 1030x108 dimensions for
	 * the background BufferedImage and an album cover scale of 1 for blankAlbumCover 
	 * BufferedImage. All images, except the blankAlbumCover, are multiplied by the 
	 * <b>scale</b> parameter to allow for enhancement of their quality.
	 * 
	 * @param scale The scale by which all BufferedImages are multiplied to allow 
	 * quality enhancement, except the blankALbumCover.
	 */
	public Images (int scale) {
		this(scale, 1030, 108, 1);
	}
	/** Creates BufferedImages designed for this program to the scale of the <b>scale</b> 
	 * parameter, additionally rendering the background BufferedImage to the <b>width</b> and 
	 * <b>height</b> parameters (further multiplied by <b>scale</b>) and the 
	 * blankAlbumCover BufferedImage scaled to the <b>albumCoverScale</b> parameter
	 * (not multiplied by <b>scale</b>).
	 * 
	 * @param scale The scale by which all BufferedImages are multiplied to allow 
	 * quality enhancement, except the blankALbumCover.
	 * @param width The width of the created background BufferedImage.
	 * @param height The height of the created background BufferedImage.
	 * @param albumCoverScale The scale by which the blankAlbumCover is multiplied by.
	 * */
	public Images (int scale, int width, int height, int albumCoverScale) {
		
		if (scale <= 0) {
			throw new IllegalArgumentException("Parameter \"scale\" must be greater "
					+ "than 0");
		} else if (width <= 0) {
			throw new IllegalArgumentException("Parameter \"width\" must be greater "
					+ "than 0");
		} else if (height <= 0) {
			throw new IllegalArgumentException("Parameter \"height\" must be greater "
					+ "than 0");
		}
		
		this.scale = scale;
		this.background = createBackgroundBufImg(width, height);
		this.searchX = createSearchXBufImg();
		this.play = createPlayBufImg();
		this.pause = createPauseBufImg();
		this.blankTrackBar = createTrackBarBufImg(0, 10, false);
		this.closeIcon = createCloseIconBufImg();
		this.previous = createPreviousBImg();
		this.forward = createForwardBImg();
		this.addToQueue = createAddToQueueBImg();
		this.icon = loadIcon();
		this.blankAlbumCover = loadBlankAlbum(albumCoverScale);
	}
	
	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}

	public int getAlbumCoverScale() {
		return albumCoverScale;
	}

	private BufferedImage createBackgroundBufImg(int width, int height) {
		
		BufferedImage bImg = new BufferedImage(width * scale, 
				height * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		// (1) Create the graphics with a scale transform and RenderedHints
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		// (2) Paint the black rounded background
		g2d.setColor(Color.BLACK);
		//         (x, y, width, height             , startAngle, arcLength)
		g2d.fillArc(0, 0, 50   , height, 90        , 180      ); 
		// 		   (x                      , y, width, height             , startAngle, arcLength)
		g2d.fillArc(width - 50, 0, 50   , height, -90       , 180      );
		//          (x , y, width             , height        )
		g2d.fillRect(24, 0, width - 48, height);
		
		// (3) Paint the Spotify Logo at offset
		int offsetX = 132;
		int offsetY = -71;
		
		// (3-a) Paint the green circle
		g2d.setColor(new Color(30, 215, 96, 198));
		g2d.fillOval(offsetX, offsetY, 213, 213); //213x213
		
		// (3-b) The first black curved line in the logo
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(20.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		CubicCurve2D.Double cc = new CubicCurve2D.Double(77 + offsetX, 41 + offsetY, //p1
														 106 + offsetX, 58 + offsetY, //p2
														 147 + offsetX, 89 + offsetY, //p3
														 174 + offsetX, 124 + offsetY); //p4
		g2d.draw(cc);
		
		// (3-c) The second smaller black line in logo
		g2d.setStroke(new BasicStroke(17.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		CubicCurve2D.Double cc2 = new CubicCurve2D.Double(58 + offsetX, 76 + offsetY, //p1
														 93 + offsetX, 92 + offsetY, //p2
														 126 + offsetX, 119 + offsetY, //p3
														 145 + offsetX, 146 + offsetY); //p4
		g2d.draw(cc2);
		
		// (3-d) The third black line
		g2d.setStroke(new BasicStroke(13.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		CubicCurve2D.Double cc3 = new CubicCurve2D.Double(43 + offsetX, 105 + offsetY, //p1
														 71 + offsetX, 117 + offsetY, //p2
														 101 + offsetX, 140 + offsetY, //p3
														 120 + offsetX, 165 + offsetY); //p4
		g2d.draw(cc3);
		
		// (4) Paint drag icon on the left
		g2d.setColor(new Color(214, 214, 214, 70));
		//          (x , y )
		g2d.fillRect(10, 22, 4, 4);
		g2d.fillRect(10, 30, 4, 4);
		g2d.fillRect(18, 22, 4, 4);
		g2d.fillRect(18, 30, 4, 4);
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createSearchXBufImg () {
		BufferedImage bImg = new BufferedImage(18 * scale, 18 * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		// (1) Draw the box around the 'X'
		g2d.setColor(new Color(178, 178, 178, 132));
		g2d.drawRect(0, 0, 17, 17);
		
		// (2) Draw the 'X'
		g2d.setColor(Color.WHITE);
		g2d.setStroke(new BasicStroke(1.5f));
		//          { p1  } - {  p2  }
		g2d.drawLine(5, 5 ,    12, 12);
		g2d.drawLine(5, 12,    12, 5 );
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createPlayBufImg () {
		
		final int padding = 2;
		final int size = 48;
		
		BufferedImage bImg = new BufferedImage(size * scale + (padding * 2), 
				size * scale + (padding * 2), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		// (1) Draw the round border of the play button
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.setColor(new Color(255, 255, 255, 184));
		g2d.drawOval(padding, padding, size - 2, size - 2);
		
		// (2) Draw the Play icon
		g2d.setColor(Color.WHITE);

		/* (1)
		 *  | \
		 *  | (3)
		 *  | /
		 * (2)
		 *               (1)          (2)           (3)   	 */
		int[] xs = {16 + padding, 16 + padding, 34 + padding};
		int[] ys = {13 + padding, 35 + padding, 24 + padding};
		g2d.fillPolygon(xs, ys, 3);
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createPauseBufImg () {
		
		final int padding = 2;
		final int size = 48;
		
		BufferedImage bImg = new BufferedImage(size * scale + (padding * 2), 
				size * scale + (padding * 2), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		// (1) Draw the round border of the pause button
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.setColor(new Color(255, 255, 255, 184));
		g2d.drawOval(padding, padding, size - 2, size - 2);
		
		// (2) Draw the pause icon
		g2d.setColor(Color.WHITE);
		g2d.fillRect(16 + padding, 14 + padding, 4, 20);
		g2d.fillRect(25 + padding, 14 + padding, 4, 20);
		
		g2d.dispose();
		
		return bImg;
	}
	
	/** 
	 * Creates a BufferedImage of a player progress bar using the current seconds and
	 *  total seconds, with the section played in white and the section to play in
	 *  gray. It additionally can draw a white circle at the current point
	 *  of the track if <b>withCircle</b> is true.
	 *  
	 * @param currentSec The seconds played from the track.
	 * @param totalSecs The total seconds of the track.
	 * @param withCircle If true, draws a circle at the current point of the track.
	 * @return A BufferedImage of the player bar.
	 */
	public BufferedImage createTrackBarBufImg(double currentSec, double totalSecs, 
			boolean withCircle) {
		
		final int circleRadius = 4;
		final int circleDiameter = circleRadius * 2;
		final int barWidth = 170;
		final int barHeight = 2;
		final int bufHeight = 10;
		
		BufferedImage bImg = new BufferedImage(barWidth * scale, circleDiameter * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		/* |  WHITE  ||  GRAY  |
		 * ==========O==========
		 */
		
		// (1) Draw the track line
		// (1-a) The current played section of the track in white
		g2d.setColor(Color.WHITE);
		g2d.fillRect(
				0, (bufHeight - barHeight) / 2,
				(int) (currentSec / totalSecs * barWidth), barHeight);
		
		// (1-b) The section to be played in gray
		g2d.setColor(Color.GRAY);
		g2d.fillRect(
				(int) (currentSec / totalSecs * barWidth), (bufHeight - barHeight) / 2,
				barWidth, barHeight);
		
		// (2) The circle that marks where the track is currently at
		if (withCircle) {
			g2d.setColor(Color.WHITE);
			g2d.fillOval(
					(int) (currentSec / totalSecs * barWidth) - circleRadius,
					(bufHeight - circleDiameter) / 2,
					circleDiameter, circleDiameter);
		}
		
		g2d.dispose();
		
		return bImg;
	}
	
	/** 
	 * Creates a BufferedImage of a player progress bar using the current seconds and
	 *  total seconds, with the section played in white and the section to play in
	 *  gray. It additionally can draw a white circle at the current point
	 *  of the track if <b>withCircle</b> is true.
	 *  
	 * @param currentSec The seconds played from the track.
	 * @param totalSecs The total seconds of the track.
	 * @param withCircle If true, draws a circle at the current point of the track.
	 * @param barWidth The length to draw the bar.
	 * @return A BufferedImage of the player bar.
	 */
	public BufferedImage createTrackBarBufImg(double currentSec, double totalSecs, 
			boolean withCircle, int barWidth) {
		
		final int circleRadius = 4;
		final int circleDiameter = circleRadius * 2;
		final int barHeight = 2;
		final int bufHeight = 10;
		
		BufferedImage bImg = new BufferedImage(barWidth * scale, circleDiameter * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		
		/* 
		 * |    WHITE   ||	  GRAY    |
		 * =============O==============
		 */
		
		// (1) Draw the track line
		// (1-a) The current played section of the track in white
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, (bufHeight - barHeight) / 2,
				(int) (currentSec / totalSecs * barWidth), barHeight);
		
		// (1-b) The section to be played in gray
		g2d.setColor(Color.GRAY);
		g2d.fillRect((int) (currentSec / totalSecs * barWidth), (bufHeight - barHeight) / 2,
				barWidth, barHeight);
		
		// (2) The circle that marks where the track is currently at
		if (withCircle) {
			g2d.setColor(Color.WHITE);
			g2d.fillOval((int) (currentSec / totalSecs * barWidth) - circleRadius,
					(bufHeight - circleDiameter) / 2, circleDiameter, circleDiameter);
		}
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createCloseIconBufImg () {
		//points to place close : (8, -30)
		Ellipse2D.Double closeCircle = new Ellipse2D.Double(0, 0, 14, 14);
		
		BufferedImage bImg = new BufferedImage((int) closeCircle.width * scale,
				(int) closeCircle.height * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		g2d.setColor(Color.BLACK);
		g2d.fill(closeCircle);
		
		g2d.setColor(Color.WHITE);
		g2d.drawLine(3, 3, 9, 9);
		g2d.drawLine(3, 9, 9, 3);
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createPreviousBImg() {
		
		int size = 25;
		
		BufferedImage bImg = new BufferedImage(size * scale, size * scale,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		g2d.setColor(Color.WHITE);
		
		/*  ||   (2) ----
		 *  ||  / |	 
		 *  ||(1) |  side (x4)
		 *  ||  \ |  
		 *  ||   (3) ----          
		 */
		 
		int side = 16;
		int barWidth = 3;
		int xMargin = (size - side) / 2;
		int yMargin = xMargin;
		
		// (I) Bar
		g2d.fillRect(xMargin, yMargin, barWidth, side);
		
		//					 (1)      		      (2)             (3) 
		int[] xs = {xMargin + barWidth - 2, xMargin + side, xMargin + side};
		int[] ys = {size / 2			  , yMargin		  , yMargin + side};
		g2d.fillPolygon(xs, ys, 3);
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createForwardBImg() {
		
		int size = 25;
		
		BufferedImage bImg = new BufferedImage(size * scale, size * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = getQualityGraphics2D(bImg);
		
		g2d.setColor(Color.WHITE);
		
		/* (1)   ||  ----
		 *  | \  ||  
		 *  | (3)||  side (x4)
		 *  | /  ||  
		 * (2)   ||  ----   
		 */
		
		int side = 16;
		int barWidth = 3;
		int xMargin = (size - side) / 2;
		int yMargin = xMargin;
		
		//            (1)        (2)                     (3)      		     
		int[] xs = {xMargin, xMargin     , xMargin + side - barWidth + 2};
		int[] ys = {yMargin, yMargin + side, size / 2};
		g2d.fillPolygon(xs, ys, 3);
		
		g2d.fillRect(xs[2] - 2, yMargin, barWidth, side);
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createAddToQueueBImg() {
		
		int size = 18;
		
		BufferedImage bImg = new BufferedImage(size * scale, size * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = getQualityGraphics2D(bImg, false);
		
		// (1) Draw the box around the que icon
		g2d.setColor(new Color(178, 178, 178, 132));
		g2d.drawRect(0, 0, 17, 17);
		
		/* (2) The '+' in the icon
		 *          { p1 } - { p2 }				*/
		g2d.drawLine(4, 2,    4, 6); //  |
		g2d.drawLine(2, 4,    6, 4); // ---
		
		/* (3) The item lines in the icon
		 *     	    { p1 } - { p2 }				*/
		g2d.drawLine(7, 4,    16, 4);
		g2d.drawLine(7, 8,    16, 8);
		g2d.drawLine(7, 12,    16, 12);
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage loadIcon () {
		BufferedImage bImg = null;
		try {
			bImg = ImageIO.read(Images.class.getResourceAsStream(
					IMAGE_DIR + "/logo v1.2.png"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return bImg;
	}
	
	private BufferedImage loadBlankAlbum (int albumCoverScale) {
		return scaleImageFromFile(IMAGE_DIR + "/blank_album v2.jpg", 
				54 * albumCoverScale,
				54 * albumCoverScale);
	}
	
	/** 
	 * Scales a BufferedImage to a size using Thumbnailator Library.
	 * 
	 * @param buffI The <i>BufferedImage</i> to scale.
	 * @param width The width to scale to.
	 * @param height The height to scale to.
	 * @return the scaled <i>BufferedImage</i>.
	 */
	public BufferedImage scaleImage(BufferedImage buffI, int width, int height) {
		try {
			buffI = Thumbnails.of(buffI)
			        .size(width, height)
			        .asBufferedImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return buffI;
	}
	
	/** 
	 * Loads a <i>BufferedImage</i> from a size and scales it to a size using Thumbnailator 
	 * Library.
	 * 
	 * @param filePath The path to the file to load the BufferedImage from as a String.
	 * @param width The width to scale to.
	 * @param height The height to scale to.
	 * @return the scaled BufferedImage.
	 */
	public BufferedImage scaleImageFromFile (String filePath, int width, int height) {
		BufferedImage bImg = null;
		
		try {
			bImg = ImageIO.read(Images.class.getResourceAsStream(filePath));
		} catch (IOException ioe) { 
			ioe.printStackTrace();
		}
		
		return scaleImage(bImg, width, height);
	}
	
	/** Returns a Graphics2D object created from a BufferedImage with a
	 * scale transform set to size of class constant <b>scale</b> to create bigger 
	 * images for enabling more definition. Additionally, the Graphics2D object is set 
	 * to quality-oriented RenderingHints (AntiAliasing, Quality Rendering, and Pure 
	 * Stroke Control).
	 * 
	 * @param bImg The BufferedImage to create the graphics from.
	 *  
	 *  @return The Graphics2D object prepared from <b>bImg</b>.
	 * */
	private Graphics2D getQualityGraphics2D (BufferedImage bImg) {
		
		Graphics2D g2d = bImg.createGraphics();
		
		// ANTIALIASING
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		// QUALITY RENDERING
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
        		RenderingHints.VALUE_RENDER_QUALITY);
        // PURE STROKE CONTROL
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
        		RenderingHints.VALUE_STROKE_PURE);
		
		g2d.scale(scale, scale);
		
		return g2d;
	}
	
	/** Returns a Graphics2D object created from a BufferedImage with the option to 
	 * invoke no scale transform to Graphics object to allow definition of the smaller
	 * pixels to be scaled. It is additionally set to quality-oriented RenderingHints 
	 * (AntiAliasing, Quality Rendering, and Pure Stroke Control).
	 * 
	 * @param bImg The BufferedImage to create the graphics from.
	 * @param withoutScale Sets no scale to the constant <b>scale</b> of this class.
	 *  
	 *  @return The Graphics2D object prepared from <b>bImg</b>.
	 * */
	private Graphics2D getQualityGraphics2D (BufferedImage bImg, boolean withoutScale) {
		
		Graphics2D g2d = bImg.createGraphics();
		
		// ANTIALIASING
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		// QUALITY RENDERING
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
        		RenderingHints.VALUE_RENDER_QUALITY);
        // PURE STROKE CONTROL
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
        		RenderingHints.VALUE_STROKE_PURE);
        
        if (!withoutScale) {
        	g2d.scale(scale, scale);
        }
		
		return g2d;
	}
	
	public int getScale () {
		return scale;
	}
}
