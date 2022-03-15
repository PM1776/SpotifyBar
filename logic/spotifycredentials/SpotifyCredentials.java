package logic.spotifycredentials;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import renderer.approvalbrowser.ApprovalBrowser;

public class SpotifyCredentials  {
	
	/** The instance that loads the authorization URL in a JavaFX WebView and 
	 * parses the query parameters from the tail of the redirectURI when loaded. 
	 * 
	 * @see ApprovalBrowser */
	private ApprovalBrowser ap;
	
	/** A generated String of 43-128 characters that can contain letters, digits,
	 *  (_), (.), (-), or (~). */
	private String codeVerifier = "";
	
	/** The URI sent with the authorization URL that is hyperlinked by the Spotify
	 * Web API after the user approves or denies access to a Spotify account. */
	private String redirectURI;
	
	/** A token given after PKCE authorization that is sent to request another access
	 * token. */
	private String refreshToken;
	
	/** A token given after authorization of some kind to the Spotify Web API that
	 * is sent as a header with all requests to access data. */
	private String accessToken;
	
	/** The functional interface to be implemented when PKCE flow completes */
	private Runnable init;
	
	private static final String spotifyClientID = "7210d51b71f44dfeaccbee554173ffcc";
    private static final String spotifyClientSecret = "77f5f18b3293413192e0ec00013c01c8";
    
    /** A parameter of requestAccessToken(int) which signals returning a PKCE
     * Authorization access token from the Spotify Web API. */
    private static final int PKCE_AUTHORIZATION = 0;
    
    /** A parameter of requestAccessToken(int) which indicates returning a PKCE refresh
     * access token from the Spotify Web API. */
    private static final int PKCE_REFRESH = 1;
    
    /** A parameter of requestAccessToken(int) which indicates returning a Client 
     * Authorization access token from the Spotify Web API. */
    private static final int CLIENT_AUTHORIZATION = 2;
    
    /** Keeps track of the flow of this instance; Client flow (<b>false</b>) or 
     * Authorization Code + PKCE flow (<b>true</b>). */
    private boolean pkceFlow;
    
    /**
	 * Gets an access token from the Spotify Web API by defaulting to the Client Flow 
	 * Authorization (doesn't access a spotify account and plays previews) 
	 * to make requests.
	 */
    public SpotifyCredentials () {
    	this.pkceFlow = false;
    	getAuthByClient();
    }
    
    /**
	 * Gets an access token from the Spotify Web API using the Authorization Code 
	 * + PKCE flow (connects to Spotify user accounts) to make requests. The 
	 * Authorization Code flow first asks the Spotify user to grant permissions and
	 * doesn't finish the flow, so it takes a parameter of Runnable interface 
	 * <b>init</b> to call its run() method when the flow completes.
	 * 
	 * @param init A Runnable interface to call when the Authorization Code flow 
	 * completes, as a Spotify user must first approve of access to their account.
	 */
    public SpotifyCredentials (Runnable init) {
    	this.pkceFlow = true;
    	getAuthByPKCE (init);
    }
    
    /**
	 * Gets an access token from the Spotify Web API by either using the Client Flow 
	 * Authorization (doesn't access a spotify account and plays previews) or the
	 * Authorization Code + PKCE flow (connects to a Spotify user account) to make 
	 * requests. The Authorization Code flow first asks the Spotify user to grant 
	 * permissions, so it runs the Runnable interface <b>init</b> once access or 
	 * denial to an account has been submitted.
	 * 
	 * @param pckeFlow Uses the Authorization Code + PKCE flow if <i>true</i>,
	 * 		the Client Authorization if <i>false</i>.
	 * @param init a Runnable interface to call its run() when the PKCE flow is 
	 * completed, as a Spotify user must first approve of access to it.
	 */
    public SpotifyCredentials (boolean pkceFlow, Runnable init) {
    	this.pkceFlow = pkceFlow;
    	if (pkceFlow) {
    		getAuthByPKCE (init);
    	} else {
    		getAuthByClient();
    	}
    }
    
    /**
	 * Gets an access token from the Spotify Web API using the Client Flow 
	 * (doesn't access a spotify account and plays previews) to make requests.
	 * 
	 * @return the access token received to send as a header with requests.
	 */
    private void getAuthByClient() {
    	requestAccessToken(CLIENT_AUTHORIZATION);
    }
    
	/** 
	 * Proof Key for Code Exchange (PKCE) Authorization, most secure. Plays songs on 
	 * and allows access to Spotify user data with their authorization. This method
	 * only creates a browser for a Spotify user to approve of access to it and
	 * doesn't complete the PKCE flow, so it takes a parameter of Runnable interface 
	 * <b>init</b> to call its run() method when when the PKCE flow completes.
	 * 
	 * @param init a Runnable interface to call its run() when the PKCE flow is 
	 * completed, as a Spotify user must first approve of access to it.
	 */
	private void getAuthByPKCE (Runnable init) { 
		
		this.init = init;
		
		// ---------------------------------------------------------------------------
		
		/* Generates code challenge by first creating code verifier (43-128
		 * letter string containing letters, digits, underscores, periods, hyphens,
		 * or tildes; 66 acceptable chars) to encrypt to the code challenge
		 * */
		StringBuilder sb = new StringBuilder();
		int length = (int) (Math.random() * 85) + 43;
		
		for (int i = 0; i < length; i++) {
			int c =  (int) (Math.random() * 66);
			
			// (0-1) = hyphen and period
			if (c <= 1) {
				c += 45; // (-) 45, (.) 46
				
			// (2-11) = digits
			} else if (c >= 2 && c <= 11) {
				c += 46; // digits on ascii (48-57) - 'c' starting range (2)
				
			// (12-37) = uppercase letters
			} else if (c >= 12 && c <= 37) {
				c += 53; // uppercase on ascii (65-90) - 'c' starting range (12)
				
			// (38) = underscores
			} else if (c == 38) {
				c = 95; // underscore on ascii (95)
				
			// (39-64) = lowercase letters
			} else if (c >= 39 && c <= 64) {
				c += 58; //lowercase on ascii (97-122) - 'c' starting range (39)
				
			// (65) = tildes
			} else if (c == 65) {
				c = 126; // tilde on ascii (126)
			}
			
			sb.append((char) c);
		}
		
		codeVerifier = sb.toString();
		
		// Encrypt code verifier in SHA-256
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e2) {
			e2.printStackTrace();
		}
		byte[] encodedHash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
		
		// Encodes to Base64 to be code challenge
		String codeChallenge = Base64.getUrlEncoder().encodeToString(encodedHash);
		// trims the '=' off
		codeChallenge = codeChallenge.substring(0, codeChallenge.length() - 1);
		
		// -----------------------------------------------------------------------------
		
		try {
			redirectURI = new URI("https://www.helpscout.com/images/blog/2019/"
					+ "dec/how-to-write-a-killer-thank-you-note.png").toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		String scope = "user-read-private "
				+ "user-read-currently-playing "
				+ "user-read-playback-state "
				+ "user-read-recently-played " 
				+ "user-modify-playback-state "
				+ "streaming";
		
		//run URL
		String endPoint = "https://accounts.spotify.com/authorize";
		
		String query = "?client_id=" + spotifyClientID
				 + "&response_type=" + "code"
				 + "&redirect_uri=" + encodeInURLFormat(redirectURI)
				 + "&scope=" + scope
				 + "&code_challenge_method=" + "S256"
				 + "&code_challenge=" + encodeInURLFormat(codeChallenge)
				 + "&show_dialog=" + "false";
		
		query = encodeInURLFormat(query).replace(' ', '+');
		
		ap = new ApprovalBrowser(endPoint + query, redirectURI, new String[] {"code"});
		
		ap.addRedirectURIListener(() -> {
			requestAccessToken(PKCE_AUTHORIZATION);
		});
		
		// wait for Spotify user to approve or deny access, which will load the
		// redirectURI and call requestAccessToken() for auth
		
	}
	
	/** 
	 * <i>Only applicable when using Authorization Code flow</i>. Every
	 * access token received with PKCE flow has an expiration of 6 minutes
	 * in the Spotify Web API, and the program must request another using
	 * the refresh token received during authorization to keep accessing
	 * the API, which is what this method returns.
	 * 
	 * @return an access token received using the refresh token given at
	 * authorization.
	 */
	public String getRefreshToken() {
		if (pkceFlow) {
			requestAccessToken(PKCE_REFRESH);
			return getAccessToken();
		} else {
			// throws error if not Authorization Code + PKCE flow
			throw new RuntimeException("Only the Authorization Code + PKCE flow"
					+ "requires a refresh token.");
		}
	}
    
	/**
	 * Gets an access token from Spotify Web API to make requests to the API. It can
	 * get it using <i>Client Authorization</i> (doesn't access a Spotify 
	 * account), <i>PKCE Authorization</i> (connects to a Spotify user account),
	 * or using the <i>PKCE refresh</i> token from the authorization to get another. 
	 * 
	 * @param flow An <i>int</i> that designates the flow to get the access token. All 
	 * flow values are constants of this class.
	 * 
	 * @return The access token to send as a header with requests.
	 */
	private void requestAccessToken (int flow) {
	   
        // Encode ^ in base64 for the authorization format of Spotify
        String authorization = "Basic " + Base64.getUrlEncoder().encodeToString(
        		(spotifyClientID + ':' + spotifyClientSecret).getBytes());
        
        String grantType = (flow == PKCE_AUTHORIZATION) ? "authorization_code" : 
        	(flow == CLIENT_AUTHORIZATION) ? "client_credentials" :
        		"refresh_token";
        
		// Use Unirest to receive a perceived JSonNode package holding accessToken
        JsonNode response = null;
        
        // Client Access Token
        switch (flow) {
        	case CLIENT_AUTHORIZATION:
	        	try {
					response = Unirest.post("https://accounts.spotify.com/api/token")
							.header("content-type", "application/x-www-form-urlencoded")
							.header("Authorization", authorization)
							.field(encodeInURLFormat("grant_type"), 
									encodeInURLFormat(grantType))
							.asJson()
							.getBody();
				
					accessToken = response.getObject()
						    .getString("access_token");
					
				} catch (UnirestException e) {
					e.printStackTrace();
					
				} catch (JSONException je) {
					System.out.print(response.toPrettyString());
				}
	        	
	        	break;
        	
	        // PKCE Auth Access Token
        	case PKCE_AUTHORIZATION:
        	
	        	String[] queryValues = ap.getQueryValues();
	        	ap.close();
	        	
				// user denied permissions
				if (queryValues == null) {
					throw new RuntimeException ("Permission has been denied.");
				}
				
				String code = queryValues[0];
	        	
	        	try {
	        		
					response = Unirest.post("https://accounts.spotify.com/api/token")
							.header("content-type", "application/x-www-form-urlencoded")
							.header("Authorization", authorization)
							.field(encodeInURLFormat("grant_type"), 
									encodeInURLFormat(grantType))
							.field(encodeInURLFormat("code"), encodeInURLFormat(code))
							.field(encodeInURLFormat("redirect_uri"), 
									encodeInURLFormat(redirectURI))
							.field("client_id", spotifyClientID)
							.field("code_verifier", codeVerifier)
							.asJson()
							.getBody();
				
					accessToken = response.getObject()
						    .getString("access_token");
					refreshToken = response.getObject()
							.getString("refresh_token");
					
					init.run();
					
				} catch (UnirestException e) {
					e.printStackTrace();
					
				} catch (JSONException je) {
					System.out.print(response.toPrettyString());
				}
	        	
	        	break;
        	
        	// PKCE refresh token
        	case PKCE_REFRESH:
        		try {
					response = Unirest.post("https://accounts.spotify.com/api/token")
							.header("Authorization", authorization)
							.header("content-type", "application/x-www-form-urlencoded")
							.field(encodeInURLFormat("grant_type"), 
									encodeInURLFormat(grantType))
							.field(encodeInURLFormat("refresh_token"), 
									encodeInURLFormat(refreshToken))
							.field("client_id", spotifyClientID)
							.asJson()
							.getBody();
				
					accessToken = response.getObject()
						    .getString("access_token");
					
				} catch (UnirestException e) {
					e.printStackTrace();
					
				} catch (JSONException je) {
					System.out.print(response.toPrettyString());
				}
        }
	}
	
	private static String encodeInURLFormat(String s) {
		try {
		    URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public String getAccessToken() {
		if (accessToken == null) {
			throw new RuntimeException ("Must get authorization to Spotify Web API by "
					+ "calling either SpotifyCredentials.getAuthByClient() or "
					+ "SpotifyCredentials.getAuthByPKCE(Runnable init) first.");
		}
		
		return accessToken;
	}
	
	public boolean isAuthCodeFlow() {
		return pkceFlow;
	}
}
