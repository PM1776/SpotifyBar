package logic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.json.JSONException;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class SpotifyCredentials {
	
	// uses client authentication
	public static String getSpotifyAccessToken () {
		
		String spotifyClientID; // the Spotify client ID I received from signing up to be a developer with Spotify
	    String spotifyClientSecret; // the client secret additionally received
	    
		//use Spotify credentials for this registered app 
        String together = spotifyClientID + ":" + spotifyClientSecret;
        //encode ^ in base64 for the authorization format of Spotify
        String authorization = "Basic " + Base64.getUrlEncoder().encodeToString(
        		new String(spotifyClientID + ":" + spotifyClientSecret).getBytes());
        
		//use unirest to receive a perceived JSonNode package holding accessToken
        String accessToken = null;
		try {
			accessToken = Unirest.post("https://accounts.spotify.com/api/token")
					.header("content-type", "application/x-www-form-urlencoded")
					.header("Authorization", authorization)
					.field(encodeInURLFormat("grant_type"), encodeInURLFormat("client_credentials"))
					.asJson()
					.getBody()
				    .getObject()
				    .getString("access_token");
		} catch (UnirestException e) {
			try {
				//if access token cannot be accessed, prints entire Json body
				System.out.println(Unirest.post("https://accounts.spotify.com/api/token")
						.header("content-type", "application/x-www-form-urlencoded")
						.header("Authorization", authorization)
						.field(encodeInURLFormat("grant_type"), encodeInURLFormat("client_credentials"))
						.asJson()
						.getBody()
						.toString()
				);
			} catch (UnirestException e1) {
				e1.printStackTrace();
			}
			
			e.printStackTrace();
		} catch (JSONException je) {
			try {
				//if access token cannot be accessed, prints entire Json body
				System.out.println(Unirest.post("https://accounts.spotify.com/api/token")
						.header("content-type", "application/x-www-form-urlencoded")
						.header("Authorization", authorization)
						.field(encodeInURLFormat("grant_type"), encodeInURLFormat("client_credentials"))
						.asJson()
						.getBody()
						.toString()
				);
			} catch (UnirestException e1) {
				e1.printStackTrace();
			}
			
			je.printStackTrace();
		}
	    
	    return accessToken;
	}
	
	public static void getAuthToSpotifyByPKCE () { //(Proof Key for Code Exchange, most secure)
		
		//generates 'code verifier' (43-128 letter string 
		//containing letters, digits, underscores, periods, hyphens, or tildes)
		int length = (int) (Math.random() * 85) + 43;
		String codeVerifier = "";
		for (int i = 0; i < length; i++) {
			//on ascii table: 
			//hyphens (45), periods (46), digits (48-57), uppercase letters (65-90),
			//underscores (95), lowercase letters (97-122), and tildes (126)
			char ascii = (char) (Math.random() * 66); //66 total characters
			if (ascii <= 1) {
				ascii += 45; //hyphens (45) and periods (46)
			} else if (ascii >= 2 && ascii <= 11) {
				ascii += 48 - 2; //random number range begins at 2, digits (48-57)
			} else if (ascii >= 12 && ascii <= 37) {
				ascii += 65 - 12; //uppercase letters (65-90)
			} else if (ascii == 38) {
				ascii = 95; //ascii += 95 (underscores) - 38 (which is ascii)
			} else if (ascii >= 39 && ascii <= 64) {
				ascii += 97 - 39; //lowercase letters (97-122)
			} else if (ascii == 65) {
				ascii = 126; //ascii += 126 (tildes) - 65
			}
			codeVerifier += ascii;
		}
		
		//creates 'code challange' by encrypting code verifier in SHA-256
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e2) {
			e2.printStackTrace();
		}
		byte[] encodedHash = digest.digest(
		codeVerifier.getBytes(StandardCharsets.UTF_8));
		String codeChallenge = encodedHash.toString();
		
		//encodes code challenge to Base64
		codeChallenge = Base64.getUrlEncoder().encodeToString(codeVerifier.getBytes());
		
		//creates redirectURL (url to go to after auth by user)
		
		//run URL
		String url = "https://accounts.spotify.com/authorize";
		Unirest.post(url)
			//.field("cliend_id", spotifyClientID)
			.field("response_type", "code")
			//.field("redirect_uri", redirectURI)
			.field("code_challenge_method", "S256")
			.field("code_challenge", codeChallenge);
		
		
		
		/*construct URL
		URL myURL;
		URLConnection myURLConnection = null;
		try {
			myURL = new URL(url);
			myURLConnection = myURL.openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		myURLConnection.getContentLength(); // -> calls getHeaderField("content-length")*/
	}
	
	
	
	public static String encodeInURLFormat(String s) {
		try {
		    URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}
}
