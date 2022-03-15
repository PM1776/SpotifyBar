package renderer.picturelabel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;

import renderer.display.PictureLabelService;
import renderer.images.Images;

/** Adds BufferedImage <b>picture</b> field to a JLabel class,
 * which can be highlighted appropriately when the mouse hovers over
 * or clicks the PictureLabel. */
public class PictureLabel extends JLabel implements PictureLabelService {
	
	/** The dependency of the Images class. */
	private static Images images = new Images(4, 565, 54, 1);
	
	private BufferedImage originalPicture = null;
	private BufferedImage picture2Paint = null;
	
	private boolean circleButton = false;
	private boolean scaled = true;
	
	private double renderingScale = 1.0 / images.getScale();
	
	/** A PictureLabel without a picture, with scaling at default <b>true</b>.
	 * 
	 * @see PictureLabel */
	public PictureLabel () {}
	
	/** A PictureLabel with scaling at default <b>true</b>. */
	public PictureLabel (BufferedImage bufImg) {
		this.originalPicture = bufImg;
		this.picture2Paint = originalPicture;
		
		if (bufImg.equals(images.play)) {
			circleButton = true;
		}
	}
	
	/** Stores <b>bufImg</b> and determines if a circleButton. Additionally,
	 * can allow button highlighting functionality. 
	 * 
	 * @param bufImg BufferedImage to set as the picture of the PictureLabel.
	 * @param buttonHighlight If true, will highlight the PictureLabel when the mouse
	 * hovers over and clicks the PictureLabel. 
	 * */
	public PictureLabel(BufferedImage bufImg, boolean buttonHighlight) {
		this(bufImg);
		
		if (buttonHighlight) {
			this.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					setPaintedPicture(highlightPicture(new Color(0, 0, 0, 57)));
					repaint();
				}
				public void mouseReleased(MouseEvent e) {
					setPaintedPicture(highlightPicture(new Color(255, 255, 255, 57)));
				}
				public void mouseEntered(MouseEvent e) {
					setPaintedPicture(highlightPicture(new Color(255, 255, 255, 57)));
					repaint();
				}
				public void mouseExited(MouseEvent e) {
					setPaintedPicture(originalPicture);
				}
			});
		}
	}
	
	/** Stores <b>bufImg</b> and determines if a circleButton. Additionally,
	 * can allow button highlighting functionality as well as change scaling to
	 * false. It is set to true by default to allow high quality pictures to
	 * retain their quality in scaling the picture with the Graphics library, 
	 * which can scale about 4x smaller without loosing much quality.
	 * 
	 * @param bufImg BufferedImage to set as the picture of the PictureLabel.
	 * @param buttonHighlight If true, will highlight the PictureLabel when the mouse
	 * 		hovers over and clicks the PictureLabel. 
	 * @param scaled Sets the scaling instance Pictures are painted with. A lower
	 * 		scale with a high resolution picture will render the picture with 
	 * 		higher quality.
	 * */
	public PictureLabel (BufferedImage bImg, boolean buttonHighlight, 
			double renderingScale) {
		this (bImg, buttonHighlight);
		
		this.renderingScale = renderingScale;
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
		picture2Paint = originalPicture;
		this.repaint();
	}
	
	private void setPaintedPicture (BufferedImage bufImg) {
		picture2Paint = bufImg;
		this.repaint();
	}
	
	public double getRenderingScale () {
		return renderingScale;
	}
	public void setRenderingScale(double scale) {
		this.renderingScale = scale;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		if (scaled) {
			g2d.scale(renderingScale, renderingScale);
		}
		g2d.drawImage(picture2Paint, null, 0, 0);
		g2d.dispose();
	}
	
	/** Overlays a highlighted area of Color <b>hlColor</b> onto the 
	 * <b>originalPicture</b> BufferedImage. Intended to used to create a button
	 * from a base picture.
	 * 
	 *  @param hlColor The Color to highlight the button area with. 
	 *  @return The highlighted BufferedImage. */
	private BufferedImage highlightPicture (Color hlColor) {
		// (1) Creates a BufferedImage in size of originalPicture
		BufferedImage hlBufImg = new BufferedImage(
				originalPicture.getWidth(), originalPicture.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		
		// (2) Paints originalPicture onto the BufferedImage
		Graphics g = hlBufImg.getGraphics();
		g.drawImage(originalPicture, 0, 0, null);
		
		// (3) Fills the intended button area with param hlColor,
		// either with a circle or square based on circleButton
		g.setColor(hlColor);
		if (!circleButton) {
			g.fillRect(1, 2, originalPicture.getWidth() - 6,
					originalPicture.getHeight() - 5);
		} else {
			g.fillOval(0, 0, originalPicture.getWidth() - 6,
					originalPicture.getHeight() - 6);
		}
		g.dispose();
		
		return hlBufImg;
	}
}