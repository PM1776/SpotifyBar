package renderer.approvalbrowser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import renderer.images.Images;

/** 
 * A class that creates instances of a JFrame with a JavaFX WebView that is loaded to a 
 * specified RESTful API <b>url</b> which asks for user permissions. It can notify 
 * listeners when the <b>redirectURI</b> has been hyperlinked by the RESTful API,
 * and parse <b>queryParams</b> from an added tail on a returned <b>redirectURI</b>.
 *  */
public class ApprovalBrowser extends JFrame {

	private WebEngine webEngine;
	
	Images images = new Images();
	
	private static final int WIDTH = 350;
	private static final int HEIGHT = 425;
	
	private String url;
	
	private List<RedirectURIListener> listeners = new ArrayList<RedirectURIListener>();
	
	private String[] queryValues;
	
	private static boolean closed;
	
	/** 
	 * A JFrame with a JavaFX WebView browser to a specified REST API <b>url</b>
	 * that asks for user permissions. It will notify its listeners when
	 * the <b>redirectURI</b> is loaded.
	 * 
	 * @param url A String of the URL to load.
	 * @param redirectURI A String of the URI that the REST API will redirect to.
	 */
	public ApprovalBrowser (String url, String redirectURI) {
		this(url, redirectURI, null);
	}
	
	/** 
	 * A JFrame with a JavaFX WebView loaded to a specified REST API <b>url</b>
	 * asking for user permissions. It will parse the passed <b>queryParams</b>
	 * from the <b>redirectURI</b> when it is hyperlinked by the API and notify its 
	 * RedirectURIListeners.
	 * 
	 * @param url A String of the URL to load.
	 * @param redirectURI A String of the URI that the REST API will redirect to.
	 * @param queryParams The query parameters to retrieve from the tail of 
	 * 		redirect URI from the REST API.
	 */
	public ApprovalBrowser (String url, String redirectURI, String[] queryParams) {
		
		this.url = url;
		
		// Thread off of main
		SwingUtilities.invokeLater(() -> {
			
			JFXPanel fxPanel = new JFXPanel();
			
			//frame = new JFrame("Spotify Bar");
			this.setTitle("Spotify Bar");
			this.setIconImage(images.icon);
			this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
			this.setLayout(new BorderLayout());
	
			// FX Thread to run its widgets
			Platform.runLater(() -> {
				WebView webView = new WebView();
				webView.getEngine().setUserStyleSheetLocation(getClass().getResource("/WebView.css").toExternalForm());
				
				webEngine = webView.getEngine();
				webEngine.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 12_3_1 "
						+ "like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) "
						+ "Version/12.1.1 Mobile/15E148 Safari/604.1");
				webEngine.load(url);
				
				// listener that fires when successfully loading a page
				webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			            if (Worker.State.SUCCEEDED.equals(newValue)) {
			            	
			            	this.url = webEngine.getLocation();
			            	
			            	if (this.url.contains(redirectURI)) {
			            		
			            		queryValues = parseQueryValues(queryParams,
			            				this.url);
				            	
			            		for (RedirectURIListener r : listeners) {
			            			r.redirected();
			            		}
			            	}
			            }
			        });
				Scene scene = new Scene (webView, WIDTH, HEIGHT);
				fxPanel.setScene(scene);
			});
			
			this.getContentPane().add(fxPanel, BorderLayout.CENTER);
			this.setForeground(Color.BLACK);
			
			this.pack();
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		});
	}
	
	public void addRedirectURIListener (RedirectURIListener listener) {
		listeners.add(listener);
	}
	
	/** Gets the URL of the Approval Browser. If the JFrame component has been
	 * closed, returns the last loaded URL as a String.
	 * 
	 * @return the URL in a String.
	 */
	public String getURL () {
		return url;
	}
	
	/** 
	 * Loads a URL to the Approval Browser.
	 * 
	 * @param url The URL to load as a String.
	 */ 
	public void loadURL (String url) {
		if (!closed) {
			Platform.runLater(() -> {
				webEngine.load(this.url);
			});
			
			this.url = url;
		}
	}
	
	/** 
	 * Loads a URL to the Approval Browser.
	 * 
	 * @param url The URL to load.
	 */ 
	public void loadURL (URL url) {
		if (!closed) {
			loadURL(url.toString());
		}
	}
	
	/** 
	 * Returns a String array with the specified query parameters values
	 * from the <b>url</b>.
	 * 
	 * @param params a String array of the parameters to gather from this.url.
	 * @return the values of the parameters. If user rejected authorization,
	 * 		returns null.
	 */
	private static String[] parseQueryValues (String params[], String url) {
		
		String[] values = new String[params.length];
		
		// user denied permissions
		if (url.contains("?error=access_denied")) {
			
			for (int i = 0; i < values.length; i++) {
				values[i] = "";
			}
			
		} else {
			for (int i = 0; i < params.length; i++) {
				if (i != params.length - 1) {
					values[i] = url.substring(
							url.indexOf(params[i] + "=") + params[i].length() + 1,
							url.indexOf(params[i + 1]) );
				} else {
					values[i] = url.substring(
							url.indexOf(params[i] + "=") + params[i].length() + 1);
				}
			}
		}
		
		return values;
	}
	
	/** 
	 * Returns the parsed values query added by the REST API to the redirectURI,
	 * and returns null if redirectURI has not been loaded yet.
	 * 
	 * @return The query values in a String array.
	 */
	public String[] getQueryValues () {
		if (queryValues == null) {
			throw new RuntimeException("ApprovalBrowser must be redirected to the specified"
					+ "redirectURI in the constructor before parsing the query values"
					+ "from the REST API. An ApprovalBrowser can add a listener"
					+ "for when the redirectURI is loaded.");
		}
		
		return queryValues;
	}
	
	/** 
	 * Closes the JFrame component of the Approval Browser and prohibits any more
	 * loading requests.
	 */
	public void close () {
		Platform.runLater(() -> {
			webEngine.load(null);
		});
		this.dispose();
		closed = true;
	}
}