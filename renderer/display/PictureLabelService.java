package renderer.display;

import java.awt.image.BufferedImage;

public interface PictureLabelService {
	public BufferedImage getPicture();
	public void setPicture(BufferedImage bufImg);
	public double getRenderingScale();
	public void setRenderingScale(double scale);
}
