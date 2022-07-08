package renderer.display;

import java.awt.image.BufferedImage;

public interface PictureLabelService {
	public BufferedImage getPicture();
	public void setPicture(BufferedImage bufImg);
	public float getRenderingScale();
	public void setRenderingScale(float scale);
}
