package renderer.picturebutton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import renderer.display.PictureLabelService;
import renderer.picturebutton.PictureLabel;

/** Adds BufferedImage <b>picture</b> field to a JLabel class,
 * which can be highlighted appropriately when the mouse hovers over
 * or clicks the PictureLabel. */
public class PictureButtonPanel extends JPanel implements PictureLabelService {
	
	/** The PictureLabel the BufferedImage is stored in, which allows for its rendering 
	 * to be scaled. */
	private PictureLabel pl = new PictureLabel();
	
	/** The PictureButtonPanel picture. Included field so as not to call pl.getPicture() 
	 * every time, which creates a new BufferedImage. */
	private BufferedImage picture;
	
	/** The Shape that the button displays highlights in during Mouse events. */
	private RectangularShape highlight;
	
	/** 
	 * A PictureButtonPanel without a picture and a button highlight shape of a
	 * rectangle.
	 * 
	 * @see PictureButtonPanel */
	public PictureButtonPanel () {
		this.setBackground(new Color(0, 0, 0, 0));
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.add(pl, BorderLayout.CENTER);
		
		this.highlight = new Rectangle2D.Float();
		
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				setBackground(new Color(0, 0, 0, 57));
				repaint();
			}
			public void mouseReleased(MouseEvent e) {
				setBackground(new Color(255, 255, 255, 57));
				repaint();
			}
			public void mouseEntered(MouseEvent e) {
				setBackground(new Color(255, 255, 255, 57));
				repaint();
			}
			public void mouseExited(MouseEvent e) {
				setBackground(new Color(0, 0, 0, 0));
				repaint();
			}
		});
	}
	
	/** 
	 * A PictureButtonPanel of picture <b>bufImg</b> and a button highlight 
	 * shape of a rectangle. 
	 * 
	 * @param bufImg The BufferedImage to set as the picture of the Button.
	 * */
	public PictureButtonPanel (BufferedImage bufImg) {
		this();
		this.picture = bufImg;
		pl.setPicture(bufImg);
		highlight.setFrame(pl.getX(), pl.getY(), bufImg.getWidth(), bufImg.getHeight());
	}
	
	/** 
	 * A PictureButtonPanel of picture <b>bufImg</b>, a button highlight shape of a 
	 * rectangle, and paints the picture to the scale of <b>renderingScale</b>, 
	 * which uses the Graphics library (can retains quality
	 * scaling nearly 4x smaller).
	 * 
	 * @param bufImg BufferedImage to set as the picture of the ButtonPanel.
	 * @param renderingScale Sets the scale the picture is rendered to. A lower
	 * 		scale with a high resolution picture will render the picture with 
	 * 		higher quality.
	 * */
	public PictureButtonPanel (BufferedImage bImg, float renderingScale) {
		this (bImg);
		pl.setRenderingScale(renderingScale);
	}
	
	/** 
	 * A ButtonPanel with picture <b>bufImg</b>, and a button highlight shape of 
	 * either a rectangle or an ellipse.
	 * 
	 * @param bufImg BufferedImage to set as the picture of the ButtonPanel.
	 * @param circleButton If true, sets the button highlight to the shape of a
	 * ellipse; if false, a rectangle
	 * */
	public PictureButtonPanel (BufferedImage bImg, boolean circleButton) {
		this (bImg);
		
		if (circleButton) {
			// sets highlight to an Ellipse shape
			this.highlight = new Ellipse2D.Float((int) highlight.getX(), 
					(int) highlight.getY(), bImg.getWidth(), bImg.getHeight());
		}
		
	}
	
	/** 
	 * A ButtonPanel with picture <b>bufImg</b>, a button highlight shape of 
	 * either the shape of a rectangle or an ellipse, and paints the picture to the 
	 * scale of <b>renderingScale</b>, which uses the Graphics library (can retains 
	 * quality scaling nearly 4x smaller).
	 * 
	 * @param bufImg BufferedImage to set as the picture of the ButtonPanel.
	 * @param renderingScale Sets the scale the picture is rendered to. A lower
	 * 		scale with a high resolution picture will render the picture with 
	 * 		higher quality.
	 * @param circleButton If true, sets the button highlight to the shape of a
	 * ellipse; if false, a rectangle
	 * */
	public PictureButtonPanel (BufferedImage bImg, float renderingScale,
			boolean circleButton) {
		
		this (bImg, circleButton);
		pl.setRenderingScale(renderingScale);
	}
	
	public PictureButtonPanel (PictureLabel pl) {
		this (pl.getPicture(), pl.getRenderingScale());
	}
	
	public PictureButtonPanel (PictureLabel pl, boolean circleButton) {
		this (pl.getPicture(), pl.getRenderingScale(), circleButton);
	}
	
	/** 
	 * Returns another BufferedImage of the picture of this ButtonPanel.
	 * 
	 * @return A BufferedImage of picture.
	 */
	public BufferedImage getPicture () {
		if (picture != null) {
			// Creates a new BufferedImage to return to protect its reference
			BufferedImage bufImg = new BufferedImage(picture.getWidth(), 
					picture.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			
			Graphics g = bufImg.getGraphics();
			g.drawImage(picture, 0, 0, null);
			g.dispose();
			
			return bufImg;
		} else {
			return null;
		}
	}
	/** 
	 * Sets the BufferedImage to be painted on the ButtonPanel. 
	 * 
	 * @param bufImg BufferedImage to be painted. */
	public void setPicture (BufferedImage bufImg) {
		pl.setPicture(bufImg);
		this.picture = bufImg;
		pl.repaint();
	}
	
	/** 
	 * Returns the scale that the PictureButtonPanel picture is rendered to. 
	 * 
	 * @return The scale the picture is rendered to.
	 * */
	public float getRenderingScale () {
		return pl.getRenderingScale();
	}
	
	/** 
	 * Sets the scale the picture is rendered to.
	 * 
	 * @param renderingScale The scale of the picture.
	 */
	public void setRenderingScale(float scale) {
		pl.setRenderingScale(scale);
	}
	
	public static void setAllRenderingScale (float scale) {
		PictureLabel.setAllRenderingScale(scale);
	}
	
	public RectangularShape getHighlightShape() {
		return highlight;
	}
	
	/** Sets the button highlight RectangularShape that displays to animate Mouse 
	 * events. The RectangularShape can additionally be moved to the center of the
	 * PictureButtonPanel if <b>centered</b>.
	 * 
	 * @param rs The RectangularShape to display button graphics during Mouse
	 * events. 
	 * @param centered If true, centers the RectangularShape in the PictureButtonPanel.
	 */
	public void setHighlightShape(RectangularShape rs, boolean centered) {
		this.highlight = rs;
		if (centered) {
			highlight.setFrame( pl.getPicture().getWidth() / 2 - highlight.getWidth() / 2,
					pl.getPicture().getHeight() / 2 - highlight.getHeight() / 2,
					highlight.getWidth(), highlight.getHeight());
		}
	}
	
	/** Overrides in order to completely repaint background, working
     * with {@code setOpaque(false)}, which makes sure the
     * parent components paint first, to allow for a background Color
     * that is semi-transparent. */
    @Override
    protected void paintComponent(Graphics g)
    {
    	Graphics2D g2d = (Graphics2D) g;
    	if (pl.isScaled()) {
			g2d.scale(pl.getRenderingScale(), pl.getRenderingScale());
		}
		g2d.drawImage(picture, null, 0, 0);
		g2d.setColor( getBackground() );
		g2d.fill(highlight);
		g2d.dispose();
        super.paintComponent(g);
    }
}