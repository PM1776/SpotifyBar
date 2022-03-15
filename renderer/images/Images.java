package renderer.images;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	
	/** A blank album cover BufferedImage for when no album has been loaded. */
	public final BufferedImage blankAlbumCover;
	
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
		Graphics2D g2d = createQualityGraphics(bImg);
		
		// (2) Paint the black rounded background
		g2d.setColor(Color.BLACK);
		//         (x, y, width, height             , startAngle, arcLength)
		g2d.fillArc(0, 0, 50   , height, 90        , 180      ); 
		// 		   (x                      , y, width, height             , startAngle, arcLength)
		g2d.fillArc(width - 50, 0, 50   , height, -90       , 180      );
		//          (x , y, width             , height        )
		g2d.fillRect(24, 0, width - 48, height);
		
		// (3) Paint Spotify Logo
		int offsetX = 132;
		int offsetY = -71;
		
		// (3a) Paint the green circle
		g2d.setColor(new Color(30, 215, 96, 198));
		g2d.fillOval(offsetX, offsetY, 213, 213); //213x213
		
		// (3b) The first black curved line in the logo
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(20.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		CubicCurve2D.Double cc = new CubicCurve2D.Double(77 + offsetX, 41 + offsetY, //p1
														 106 + offsetX, 58 + offsetY, //p2
														 147 + offsetX, 89 + offsetY, //p3
														 174 + offsetX, 124 + offsetY); //p4
		g2d.draw(cc);
		
		// (3c) The second smaller black line in logo
		g2d.setStroke(new BasicStroke(17.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		CubicCurve2D.Double cc2 = new CubicCurve2D.Double(58 + offsetX, 76 + offsetY, //p1
														 93 + offsetX, 92 + offsetY, //p2
														 126 + offsetX, 119 + offsetY, //p3
														 145 + offsetX, 146 + offsetY); //p4
		g2d.draw(cc2);
		
		// (3d) The third black line in logo
		g2d.setStroke(new BasicStroke(13.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		CubicCurve2D.Double cc3 = new CubicCurve2D.Double(43 + offsetX, 105 + offsetY, //p1
														 71 + offsetX, 117 + offsetY, //p2
														 101 + offsetX, 140 + offsetY, //p3
														 120 + offsetX, 165 + offsetY); //p4
		g2d.draw(cc3);
		
		// (4) Paint drag icon on the left
		g2d.setColor(new Color(214, 214, 214, 70));
		// -------- (x , y , 4x4 )
		g2d.fillRect(10, 22, 4, 4);
		g2d.fillRect(10, 30, 4, 4);
		g2d.fillRect(18, 22, 4, 4);
		g2d.fillRect(18, 30, 4, 4);
		
		// (5) Paint the line under the searchBar
		// -------- (x , y , 237x2 )
		g2d.fillRect(28, 41, 235, 2);
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createSearchXBufImg () {
		BufferedImage bImg = new BufferedImage(18 * scale, 18 * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		// (1) Create the graphics with a scale transform and RenderedHints
		Graphics2D g2d = createQualityGraphics(bImg);
		
		// (2) Draw the bordering of the 'X'
		g2d.setColor(new Color(178, 178, 178, 132));
		g2d.drawRect(0, 0, 17, 17);
		
		// (3) Draw the 'X'
		g2d.setColor(Color.WHITE);
		g2d.setStroke(new BasicStroke(2.0f));
		g2d.drawLine(5, 5, 12, 12); // (5, 5) -> (12, 12)
		g2d.drawLine(5, 12, 12, 5); // (5, 12) -> (12, 5)
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createPlayBufImg () {
		BufferedImage bImg = new BufferedImage(48 * scale, 48 * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		// (1) Create the graphics with a scale transform and RenderedHints
		Graphics2D g2d = createQualityGraphics(bImg);
		
		// (2) Draw the round border of the play button
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.setColor(new Color(255, 255, 255, 184));
		g2d.drawOval(0, 0, 46, 46);
		
		// (3) Draw the Play icon
		g2d.setColor(Color.WHITE);
		g2d.fillPolygon(new int[] {16, 16, 34}, // x points
						new int[] {13, 35, 24}, // y points
						3); // number of points
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage createPauseBufImg () {
		BufferedImage bImg = new BufferedImage(48 * scale, 48 * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		// (1) Create the graphics with a scale transform and RenderedHints
		Graphics2D g2d = createQualityGraphics(bImg);
		
		// (2) Draw the round border of the play button
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.setColor(new Color(255, 255, 255, 184));
		g2d.drawOval(0, 0, 46, 46);
		
		// (3) Draw the pause icon
		g2d.setColor(Color.WHITE);
		g2d.fillRect(16, 14, 4, 20);
		g2d.fillRect(25, 14, 4, 20);
		
		g2d.dispose();
		
		return bImg;
	}
	
	/** Creates a BufferedImage of a player progress bar using the current seconds and
	 *  total seconds, with the section played in white and the section to play in
	 *  gray. It additionally can draw a white circle at the current point
	 *  of the track if <b>withCircle</b> is true.
	 *  
	 * @param currentSec The seconds played from the track.
	 * @param durationSecs The total seconds of the track.
	 * @param withCircle If true, draws a circle at the current point of the track.
	 * @return A BufferedImage of the player bar.
	 */
	public BufferedImage createTrackBarBufImg(double currentSec, 
			double durationSecs, boolean withCircle) {
		
		final int circleRadius = 4;
		final int circleDiameter = circleRadius * 2;
		final int barWidth = 170;
		final int barHeight = 2;
		
		BufferedImage bImg = new BufferedImage(barWidth * scale, circleDiameter * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		// (1) Create the graphics with a scale transform and RenderedHints
		Graphics2D g2d = createQualityGraphics(bImg);
		
		// (2) Draw the line of the track
		// (2a) Draws the current played section of the track in white
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, (10 - barHeight) / 2,                         //x, y
				(int) (currentSec / durationSecs * barWidth), barHeight);   //w, h
		
		// (2b) Draws the section of the track to be played in gray
		g2d.setColor(Color.GRAY);
		g2d.fillRect((int) (currentSec / durationSecs * barWidth), (10 - barHeight) / 2, //x, y
				barWidth, barHeight);                                             //w, h
		
		// (3) Draws the circle that marks where the track is currently at
		if (withCircle) {
			g2d.setColor(Color.WHITE);
			g2d.fillOval((int) (currentSec / durationSecs * barWidth) - circleRadius, //x
					(10 - circleDiameter) / 2, circleDiameter, circleDiameter);				//y, w, h
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
		
		// (1) Create the graphics with a scale transform and RenderedHints
		Graphics2D g2d = createQualityGraphics(bImg);
		
		g2d.setColor(Color.BLACK);
		g2d.fill(closeCircle);
		
		g2d.setColor(Color.WHITE);
		g2d.drawLine(3, 3, 9, 9);
		g2d.drawLine(3, 9, 9, 3);
		
		g2d.dispose();
		
		return bImg;
	}
	
	private BufferedImage loadIcon () {
		BufferedImage bImg = null;
		
		try {
			bImg = ImageIO.read(Images.class.getResourceAsStream("/logo v1.2.png"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return bImg;
	}
	
	private BufferedImage loadBlankAlbum (int albumCoverScale) {
		BufferedImage bImg = null;
		
		try {
			bImg = ImageIO.read(Images.class.getResourceAsStream("/blank_album v2.jpg"));
		} catch (IOException ioe) { 
			ioe.printStackTrace();
		}
		
		// 1350x1350 size
		
		return scaleImage(bImg, 54 * albumCoverScale, 54 * albumCoverScale);
	}
	
	/** 
	 * Scales a BufferedImage to a size using Thumbnailator Library.
	 * 
	 * @param buffI The <i>BufferedImage</i> to scale.
	 * @param WIDTH The width to scale to.
	 * @param HEIGHT The height to scale to.
	 * @return the scaled <i>BufferedImage</i>.
	 */
	public BufferedImage scaleImage(BufferedImage buffI, int WIDTH, int HEIGHT) {
		try {
			buffI = Thumbnails.of(buffI)
			        .size(WIDTH, HEIGHT)
			        .asBufferedImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return buffI;
	}
	
	/** Returns a Graphics2D object created from BufferedImage <b>bImg</b> with a
	 * scale transform set to size <b>scale</b> to create more defined images in 
	 * being bigger. Additionally, the returned Graphics2D object is set with 
	 * quality-oriented RenderingHints (AntiAliasing, Quality Rendering, and Pure 
	 * Stroke Control).
	 * 
	 * @param bImg The BufferedImage to create the graphics from.
	 *  
	 *  @return The Graphics2D object prepared from <b>bImg</b>.
	 * */
	private Graphics2D createQualityGraphics (BufferedImage bImg) {
		
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
	
	public int getScale () {
		return scale;
	}
}
