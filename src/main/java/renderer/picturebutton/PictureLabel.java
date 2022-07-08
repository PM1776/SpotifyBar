package renderer.picturebutton;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

import renderer.display.PictureLabelService;

/** Adds BufferedImage <b>picture</b> field to a JLabel class, which can be configured 
 * to paint it to a scale.
 * 
 * @author Paul Meddaugh
 */
public class PictureLabel extends JLabel implements PictureLabelService {
	
	private BufferedImage originalPicture;
	private static float allScale = 1.0f;
	private boolean scaled = (allScale != 1.0f) ? true : false;
	private float renderingScale = allScale;
	
	/** A ButtonLabel without a picture, with scaling at default <b>false</b>.
	 * 
	 * @see ButtonLabel */
	public PictureLabel () {}
	
	/** A PictureLabel with the picture <b>bufImg</b> and scaling at default <i>false</i>. */
	public PictureLabel (BufferedImage bufImg) {
		this.originalPicture = bufImg;
	}
	
	/** Stores <b>bufImg</b> and determines if a circleButton. Additionally,
	 * can allow button highlighting functionality as well as change scaling to
	 * false. It is set to true by default to allow high quality pictures to
	 * retain their quality in scaling the picture with the Graphics library, 
	 * which can scale about 4x smaller without loosing much quality.
	 * 
	 * @param bufImg BufferedImage to set as the picture of the ButtonLabel.
	 * @param buttonHighlight If true, will highlight the ButtonLabel when the mouse
	 * 		hovers over and clicks the PictureLabel. 
	 * @param scaled Sets the scaling instance Pictures are painted with. A lower
	 * 		scale with a high resolution picture will render the picture with 
	 * 		higher quality.
	 * */
	public PictureLabel (BufferedImage bImg, float renderingScale) {
		this (bImg);
		setRenderingScale(renderingScale);
	}
	
	/** Returns another BufferedImage of the picture of this PictureLabel.
	 * 
	 * @return A BufferedImage of picture.
	 */
	public BufferedImage getPicture () {
		if (originalPicture != null) {
			// Creates a new BufferedImage to return to protect its reference
			BufferedImage bufImg = new BufferedImage(originalPicture.getWidth(), 
					originalPicture.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			
			Graphics g = bufImg.getGraphics();
			g.drawImage(originalPicture, 0, 0, null);
			g.dispose();
			
			return bufImg;
		} else {
			return null;
		}
	}
	/** Sets the BufferedImage to be painted on the PictureLabel. 
	 * 
	 * @param bufImg BufferedImage to be painted. */
	public void setPicture (BufferedImage bufImg) {
		originalPicture = bufImg;
		this.repaint();
	}
	
	public float getRenderingScale () {
		return renderingScale;
	}
	public void setRenderingScale(float scale) {
		if (scale != 1.0f) {
			this.scaled = true;
			this.renderingScale = scale;
		} else {
			this.scaled = false;
			this.renderingScale = 1.0f;
		}
	}
	public static void setAllRenderingScale(float scale) {
		allScale = scale;
	}
	public boolean isScaled() {
		return scaled;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		if (scaled) {
			g2d.scale(renderingScale, renderingScale);
		}
		g2d.drawImage(originalPicture, null, 0, 0);
		g2d.dispose();
	}
}