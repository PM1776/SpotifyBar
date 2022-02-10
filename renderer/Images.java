package renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;

/** A class that uses the <i>Graphics</i> package to create images displayed, 
 * storing them in static <i>BufferedImage</i> constants. Creating BufferedImages
 * using the Graphics package allows their scale to be altered simply by changing
 * <b>scale</b> without loosing resolution.
 * 
 * </br></br>P.S.: If it can be made with the default tools in Paint.net, it can be made 
 * with the Graphics package quite easily, as nearly all the default tools 
 * in Paint.net exactly replicate functions and their parameter inputs from the 
 * Graphics library.
 * 
 * @version 2022-2-9
 * @author Paul Meddaugh
 */
public class Images {
	
	/** The background <i>BufferedImage</i> of the Spotify player, composed of the black
	 * background, Spotify logo, search bar underline, and anchor symbol on the left. */
	static final BufferedImage background = createBackgroundBufImg();
	
	/** The 'X' button of the search Bar in a <i>BufferedImage</i>. */
	static final BufferedImage searchX = createSearchXBufImg();
	
	/** The 'play' <i>BufferedImage</i> for the Play/Pause button. */
	static final BufferedImage play = createPlayBufImg();
	
	/** The 'pause' <i>BufferedImage</i> for the Play/Pause button. */
	static final BufferedImage pause = createPauseBufImg();
	
	/** A <i>BufferedImage</i> of a track bar with no playing progress on it
	 * or circle at the current position of the track. */
	static final BufferedImage blankTrackBar = createTrackBarBufImg(0, 10, false);
	
	/** The multiplier scale that BufferedImages are drawn from their original size. */
	private static final int scale = 4;
	
	private static BufferedImage createBackgroundBufImg() {
		
		BufferedImage bImg = new BufferedImage(Display.WIDTH * scale, 
				Display.HEIGHT * scale,
				BufferedImage.TYPE_INT_ARGB);
		
		// (1) Create the graphics with a scale transform and RenderedHints
		Graphics2D g2d = createQualityGraphics(bImg);
		
		// (2) Paint the black rounded background
		g2d.setColor(Color.BLACK);
		//         (x, y, width, height        , startAngle, arcLength)
		g2d.fillArc(0, 0, 50   , Display.HEIGHT, 90        , 180      ); 
		// 		   (x                 , y, width, height        , startAngle, arcLength)
		g2d.fillArc(Display.WIDTH - 50, 0, 50   , Display.HEIGHT, -90       , 180      );
		//          (x , y, width             , height        )
		g2d.fillRect(24, 0, Display.WIDTH - 48, Display.HEIGHT);
		
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
	
	private static BufferedImage createSearchXBufImg () {
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
	
	private static BufferedImage createPlayBufImg () {
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
	
	private static BufferedImage createPauseBufImg () {
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
	public static BufferedImage createTrackBarBufImg(double currentSec, 
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
	private static Graphics2D createQualityGraphics (BufferedImage bImg) {
		
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
	
	public static int getScale () {
		return scale;
	}
}
