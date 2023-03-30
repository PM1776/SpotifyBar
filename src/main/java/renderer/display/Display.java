package renderer.display;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import logic.playerlogic.PlayerLogic;
import logic.song.Song;
import renderer.images.Images;
import renderer.picturebutton.PictureButtonPanel;
import renderer.picturebutton.PictureLabel;

/** 
 * A Singleton class that creates an instance of the GUI. The display is entirely
 * composed of images from the <i>Images</i> class, and the album cover quality, 
 * using multiplier scale <b>albumCoverScale</b>, which can renders album covers bigger,
 * is also set in this class. This variable also inversely sets the scale the 
 * album cover is rendered in, allowing for much better graphics. Additionally, 
 * this class handles updating the GUI.
 * 
 * @version 2022-2-9
 * @author Paul Meddaugh
 */
public class Display {
	
	/** The singleton instance of this class. */
	private static Display display;
	
	/** The width of the JFrame. */
	static final int WIDTH = 565;
	
	/** The height of the JFrame. */
	static final int HEIGHT = 54;
	
	/** The scale that album covers are scaled down to, which is * by the size it will
	 * be displayed into, with 1 equating its displayed size, and 4 being
	 * 4 * that size. A higher scale will increase the quality. */
	private static final int albumCoverScale = 3;
	
	private static Images images = new Images(4, WIDTH, HEIGHT, albumCoverScale);
	
	/** The invisible JFrame of the GUI. */
	private JFrame frame;
	
	/** The search bar that takes input for searches to Spotify servers. */
	private JTextField searchtf;
	
	/** PictureButtonPanel with searchX of class Images as the picture to clear
	 * searchtf text. */
	private PictureButtonPanel searchX;
	
	/** PictureLabel that switches between "play" and "pause" BufferedImages from 
	 * class Images as picture, and button highlighting enabled. */
	private PictureButtonPanel playPause;
	
	private PictureButtonPanel previous;
	
	private PictureButtonPanel forward;
	
	/** The label that song names will be displayed on. */
	private JTextField nameLabel;
	
	/** The label that song artists will be displayed on. */
	private JTextField artistsLabel;
	
	/** The track progress bar that will update according to the track's current
	 * frame and duration. */
	private PictureLabel trackBar;
	
	/** The PictureLabel that album covers will be displayed on. */
	private PictureLabel albumCoverLabel;
	
	/** The JPanel that catches the mouse events to display the close icon. */
	private JPanel anchorHoverPanel;
	
	/** Keeps track of if the JFrame is being dragged. */
	private boolean frameIsBeingMoved;
	
	/** Stores the X mouse coordinate within the frame. */
	private int offSetMouseX;
	
	/** Stores the Y mouse coordinate within the frame. */
	private int offSetMouseY;
	
	/** The current <i>Song</i> object displayed. */
	private Song displayedSong;
	
	static {
		PictureButtonPanel.setAllRenderingScale((float) 1 / images.getScale());
	}
	
	/** 
	 * Creates a <i>Display</i> instance if none is instantiated, and returns the same
	 * instance if already created. This object comprises a Spotify player bar GUI
	 * created from BufferedImages on an invisible JFrame, and creates "clear search"
	 * and play/pause buttons created from those BufferedImages. It has a search bar to
	 * enter Spotify server queries and displays the song info, cover art and a 
	 * player bar of the track.
	 * 
	 * @return a single display instance.
	 */
	public static Display createDisplayInstance () {
		if (display == null) {
			display = new Display(false);
		}
		
		return display;
	}
	
	/** Creates a <i>Display</i> instance if none is instantiated, and returns the same
	 * instance if already created. This object comprises a Spotify player bar GUI
	 * created from BufferedImages on an invisible JFrame, and creates "clear search"
	 * and play/pause buttons created from those BufferedImages. It has a search bar to
	 * enter Spotify server queries and displays the song info, cover art and a 
	 * player bar of the track.
	 * 
	 * @return a single display instance.
	 */
	public static Display createDisplayInstance (boolean authCodeFlow) {
		if (display == null) {
			if (authCodeFlow) {
				display = new Display(true);
			} else {
				display = new Display(false);
			}
		}
		
		return display;
	}
	
	private Display (boolean authCodeFlow) {
		
		//create invisible JFrame
		frame = new JFrame();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(screenSize.width - WIDTH - 30, 40, WIDTH, HEIGHT + 50);
		frame.setTitle("Spotify Bar");
		frame.setIconImage(images.icon);
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.setAlwaysOnTop(true);
		
		// --------------------------------------------------------------------
		
		//create JLayeredPane component
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(new BorderLayout());
		layeredPane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
		layeredPane.repaint();
		
		// ---------------------------------------------------------------------
		
		//create background image and allow it to be dragged
		PictureLabel background = new PictureLabel(images.background);
		background.setSize(new Dimension(images.background.getWidth() / images.getScale(),
				images.background.getHeight() / images.getScale()));
		background.addMouseMotionListener(new MouseAdapter() {
		     public void mouseDragged(MouseEvent e) {
				//sets frame position when mouse dragged
				frame.setLocation(e.getXOnScreen() - offSetMouseX,
						e.getYOnScreen() - offSetMouseY);
		     }
		});
		background.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				offSetMouseX = e.getX(); //e.get-() is the mouse
				offSetMouseY = e.getY(); //location in the frame
			}
		});
		
		// -------------------------------------------------------------------------
		
		// search Textfield (see-through with white text)
		searchtf = new JTextField();
		searchtf.setBorder(null);
		searchtf.requestFocusInWindow();
		searchtf.setBackground(new Color(0, 0, 0, 0));
		searchtf.setOpaque(false);
		searchtf.setForeground(Color.WHITE);
		searchtf.setCaretColor(Color.WHITE);
		searchtf.setSelectionColor(Color.WHITE);
		searchtf.setSelectedTextColor(new Color(165, 165, 165, 255));
		searchtf.addKeyListener(new KeyAdapter() {
	          public void keyPressed(KeyEvent e) {
	            //System.out.println("Pressed " + e.getKeyChar());
	            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
	            	PlayerLogic.SearchEnter(searchtf.getText());
	            }
	          }
	    });
		
		// searchX button
		searchX = new PictureButtonPanel(images.searchX);
		searchX.setPreferredSize(new Dimension(
				images.searchX.getWidth() / images.getScale(),
				images.searchX.getHeight() / images.getScale()));
		searchX.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				searchtf.setText("");
			}
		});
		
		JPanel searchPanel = new JPanel() {
		    
		    /** Overrides in order to completely repaint background, working
		     * with {@code setOpaque(false)}, which makes sure the
		     * parent components paint first, to allow for a background Color
		     * that is semi-transparent. */
		    @Override
		    protected void paintComponent(Graphics g)
		    {
		        g.setColor( getBackground() );
		        g.fillRect(0, 0, getWidth(), getHeight());
		        super.paintComponent(g);
		    }
		};
		searchPanel.setLayout(new BorderLayout());
		searchPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(214, 214, 214, 70)),
				new EmptyBorder(5, 5, 5, 5)));
		searchPanel.add(searchtf, BorderLayout.CENTER);
		searchPanel.add(searchX, BorderLayout.EAST);
		searchPanel.setOpaque(false);
		searchPanel.setBackground(new Color(0, 0, 0, 0));
		MouseAdapter ma = new MouseAdapter() {
			  public void mouseEntered(MouseEvent e) {
	        	  	searchPanel.setBackground(new Color(255, 255, 255, 105));
	          }
	          public void mouseExited(MouseEvent e) {
	        	  	searchPanel.setBackground(new Color(0, 0, 0, 0));
	          }
		};
		searchtf.addMouseListener(ma); // search components sit
		searchX.addMouseListener(ma);  // on top of panel
		searchPanel.addMouseListener(ma);
		
		// Panel that centers the searchPanel (searchtf + searchX)
		JPanel searchPanelCentered = new JPanel(new GridBagLayout());
		searchPanelCentered.setBorder(new EmptyBorder(0, 5, 0, 5));
		searchPanelCentered.setBackground(new Color(0, 0, 0, 0));
		searchPanelCentered.setOpaque(false);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
		searchPanelCentered.add(searchPanel, gbc);
		
		// ------------------------------------------------------------------------
		
		// place Play/Pause button in the right location and add listeners
		playPause = new PictureButtonPanel(images.play, true);
		playPause.setPreferredSize(new Dimension(
				images.play.getWidth() / images.getScale(), 
				images.play.getHeight() / images.getScale()));
		playPause.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (!(PlayerLogic.getPlayerState() == PlayerLogic.PLAYING)) {
					PlayerLogic.play();
					playPause.setPicture(images.pause);
				} else {
					PlayerLogic.pause();
					playPause.setPicture(images.play);
				}
			}
		});
		
		JPanel playerControlPanel = new JPanel(new GridBagLayout());
		playerControlPanel.setOpaque(false);
		playerControlPanel.setBackground(new Color(0, 0, 0, 0));
		
		if (authCodeFlow) {
			
			// adds previous button
			previous = new PictureButtonPanel(images.previous);
			previous.setPreferredSize(new Dimension(
					images.previous.getWidth() / images.getScale(),
					images.previous.getHeight() / images.getScale() ));
			previous.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					PlayerLogic.previous();
				}
			});
			//previous.setHighlightBorderColor(Color.GRAY);
			gbc.gridx = 0;
			playerControlPanel.add(previous, gbc);
			
			// adds forward button
			forward = new PictureButtonPanel(images.forward);
			forward.setPreferredSize(new Dimension(
					images.forward.getWidth() / images.getScale(),
					images.forward.getHeight() / images.getScale() ));
			//forward.setHighlightBorderColor(Color.GRAY);
			forward.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					PlayerLogic.next();
				}
			});
			gbc.gridx = 2;
			playerControlPanel.add(forward, gbc);
			
			gbc.gridx = 1;
		}
		
		playerControlPanel.add(playPause, gbc);
		
		// -------------------------------------------------------------------------
		
		// Place song name label where it will be, and change font
		nameLabel = new JTextField();
		nameLabel.setBorder(null);
		nameLabel.setEnabled(false);
		nameLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
		nameLabel.setBackground(new Color(0, 0, 0, 0));
		nameLabel.setFont(new Font("Calibri Light", Font.BOLD, 12));
		nameLabel.setDisabledTextColor(Color.WHITE);
		
		// Place artists label where it will be, and change font
		artistsLabel = new JTextField();
		artistsLabel.setBorder(null);
		artistsLabel.setEnabled(false);
		artistsLabel.setBackground(new Color(0, 0, 0, 0));
		artistsLabel.setFont(new Font("Calibri Light", Font.BOLD, 12));
		artistsLabel.setDisabledTextColor(Color.LIGHT_GRAY);
		
		// Places the player track bar where it will be
		trackBar = new PictureLabel(images.blankTrackBar);
		trackBar.setPreferredSize(new Dimension(0, 10)); // expands width with GridBag
		trackBar.setBorder(new EmptyBorder(0, 0, 10, 0));
		
		JPanel songInfoPanel = new JPanel();
		songInfoPanel.setLayout(new GridBagLayout());
		songInfoPanel.setBackground(new Color(0, 0, 0, 0));
		songInfoPanel.setOpaque(false);
		
		gbc.gridy = 0;
		songInfoPanel.add(nameLabel, gbc);
		gbc.gridy = 1;
		songInfoPanel.add(artistsLabel, gbc);
		gbc.gridy = 2;
		songInfoPanel.add(trackBar, gbc);
		
		gbc.gridy = 0;
		
		//place albumCoverLabel where cover art will be,
		albumCoverLabel = new PictureLabel(images.blankAlbumCover);
		albumCoverLabel.setRenderingScale(1.0f / albumCoverScale);
		albumCoverLabel.setPreferredSize(new Dimension(
				images.blankAlbumCover.getWidth() / albumCoverScale,
				images.blankAlbumCover.getHeight() / albumCoverScale));
		
		// panel used to honor setPreferredSize of albumCoverLabel
		JPanel albumCoverPanel = new JPanel(new BorderLayout());
		albumCoverPanel.setBackground(new Color(0, 0, 0, 0)); // (60x54) for some reason
		albumCoverPanel.add(albumCoverLabel, BorderLayout.CENTER);
		
		JPanel songPanel = new JPanel();
		songPanel.setLayout(new BorderLayout());
		songPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
		songPanel.setBackground(new Color(0, 0, 0, 0));
		songPanel.setOpaque(false);
		songPanel.add(albumCoverPanel, BorderLayout.EAST);
		songPanel.add(songInfoPanel, BorderLayout.CENTER);
		
		// -------------------------------------------------------------------------
		
		/* Centers searchPanel and songInfoPanel with BorderLayout so they fill the space
		 * if previous and next buttons not visible in playerControlPanel
		  */
		GridBagLayout gbl = new GridBagLayout();
		JPanel mainPanel = new JPanel(gbl);
		mainPanel.setOpaque(false);
		mainPanel.setPreferredSize(new Dimension(WIDTH - 50, HEIGHT));
		
		gbc.gridx = GridBagConstraints.RELATIVE;
		mainPanel.add(searchPanelCentered, gbc);
		gbc.weightx = 0.0;
		mainPanel.add(playerControlPanel, gbc);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(songPanel, gbc);
		
		// -------------------------------------------------------------------------
		
		Box mainBox = Box.createHorizontalBox();
		mainBox.setSize(new Dimension(WIDTH + 6, HEIGHT));
		mainBox.add(Box.createRigidArea(new Dimension(27, 0)));
		mainBox.add(mainPanel);
		
		// -----------------------------------------------------------------------
		
		//add components to layeredPane (the higher, the further on the bottom)
		layeredPane.add(mainBox, BorderLayout.CENTER, 11);
		layeredPane.add(background, BorderLayout.CENTER, 20);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(layeredPane);
		frame.setVisible(true); 
	}
	
	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}

	/** Sets the play or pause image to playPause button.
	 * 
	 * @param play <i>true</i> is the play image; <i>false</i> is pause.
	 */
	public void setPlayPauseImage (boolean play) {
		if (play) {
			playPause.setPicture(images.play);
		} else {
			playPause.setPicture(images.pause);
		}
	}
	
	/** Displays the title and album artwork of the song on the player,
	 * but does not play it.
	 * 
	 * @param song The song to display on the player.
	 */
	public void displaySong(Song song) {
		if (song != null) {
			
			// Name label
			if (song.getName() != null) {
				String name = song.getName();
				nameLabel.setText(name);
			} else {
				throw new RuntimeException("No name set to \"song\".");
			}
			
			// Artists label
			if (song.getArtists() != null) {
				String artists = "";
				for (String artist : song.getArtists()) {
					artists += artist.concat(", ");
				}
				artists = artists.substring(0, artists.length() - 2);
				artistsLabel.setText(artists);
			} else {
				throw new RuntimeException("No artists set to \"song\".");
			}
			
			// AlbumCover label
			if (song.getAlbumCover() != null) {
				
				// scales albumCover if not correct dimensions
				if (song.getAlbumCover().getWidth() == 54 * display.getAlbumCoverScale() &&
					song.getAlbumCover().getHeight() == 54 * display.getAlbumCoverScale()) {
					
					albumCoverLabel.setPicture(song.getAlbumCover());
				} else {
					
					BufferedImage scaledAlbumCover = display.scaleToAlbumCoverDisplaySize(
							song.getAlbumCover());
					albumCoverLabel.setPicture(scaledAlbumCover);
				}
			} else {
				albumCoverLabel.setPicture(images.blankAlbumCover);
			}
			
			// Updates the track bar
			display.updateTrackBar(song.getProgress(), song.getDuration());
			
			displayedSong = song;
			
		} else {
			System.out.println("No song to display on player.");
		}
	}
	
	/** Displays the title and album artwork of the song on the player,
	 * but does not play it.
	 * 
	 * @param song The song to display on the player.
	 * @param playingButton Sets play/pause button to playing (shows pause image) if
	 * <i>false</i>, and sets it to paused (shows play image) if <i>true</i>.
	 */
	public void displaySong(Song song, boolean playingButton) {
		displaySong(song);
		setPlayPauseImage(playingButton);
	}
	
	/** 
	 * Scales the BufferedImage to the dimensions that album covers are scaled to.
	 * 
	 * @param buffI The <i>BufferedImage</i> to scale.
	 * @return the scaled <i>BufferedImage</i>.
	 */
	public BufferedImage scaleToAlbumCoverDisplaySize(BufferedImage buffI) {
		return images.scaleImage(buffI, 54 * albumCoverScale, 54 * albumCoverScale);
	}
	
	/** Returns the scale albums are resized down to, with 1 being its
	 * display size, and 4 being 4 times that display size.
	 * 
	 * @return The album cover scale.
	 */
	public int getAlbumCoverScale() {
		return albumCoverScale;
	}
	
	/** Updates the track bar of the player according to the seconds played and seconds
	 * total. Setting <b>current</b> and <b>duration</b> both to 0 will set a blank
	 * track bar BufferedImage to trackBar.
	 * 
	 * @param current The current second in the track as a double.
	 * @param duration The length of the track in seconds as a double.
	 */
	public void updateTrackBar (double current, double duration) {
		if (current == 0 && duration == 0) {
			trackBar.setPicture(images.blankTrackBar);
		}
		
		BufferedImage bImg = images.createTrackBarBufImg(current, duration, false);
		trackBar.setPicture(bImg);
	}
	
	/** Redispatches a MouseEvent to a <b>targetComp</b> from a <b>sourceComp</b>.
	 * 
	 * @param targetComp The component to dispatch the MouseEvent to.
	 * @param sourceComp The component that forwards its MouseEvent.
	 * @param e The MouseEvent.
	 * @param repaint Repaints the <b>targetComp</b> if true.
	 */
	private void redispatchMouseEvent(Component targetComp, Component sourceComp,
			MouseEvent e, boolean repaint) {
		
		Point sourcePoint = e.getPoint();
		Point targetPoint = SwingUtilities.convertPoint(
		              sourceComp,
		              sourcePoint,
		              targetComp);
		
		if (targetPoint.y >= 0 && targetPoint.y <= targetComp.getHeight() &&
				targetPoint.x >= 0 && targetPoint.x <= targetComp.getWidth() ) {
		//The mouse event is probably over the content pane.
		//Find out exactly which component it's over.
//		Component component =
//		SwingUtilities.getDeepestComponentAt(
//		              targetComp,
//		              targetPoint.x,
//		              targetPoint.y);
//		
//			if ((component != null)
//			&& (component.equals(targetComp))) {
				//Forward events over the targetComponent
				Point componentPoint = SwingUtilities.convertPoint(
				                  sourceComp,
				                  sourcePoint,
				                  targetComp);
				targetComp.dispatchEvent(new MouseEvent(targetComp,
				                           e.getID(),
				                           e.getWhen(),
				                           e.getModifiers(),
				                           componentPoint.x,
				                           componentPoint.y,
				                           e.getClickCount(),
				                           e.isPopupTrigger()));
			}
		//}
		
	    if (repaint) {
	        targetComp.repaint();
	    }
	}
	
	/**
	 * A wrapper for the windowClosing event of Display.
	 * 
	 * @param r The runnable interface to run in the windowClosing event.
	 */
	public void addWindowCloseListener(Runnable r) {
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				r.run();
			}
		});
	}
}