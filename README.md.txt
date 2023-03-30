# Spotify Search Bar
This app prompts the user to login to their spotify account to begin playing songs. If login is unsuccessful or denied, it can additionally fallback to playing previews of available songs on Spotify. The search bar also stays atop of other desktop applications.<br />
<br />
The application can be downloaded at https://paulmeddaugh.github.io/resources/SpotifyBar.zip

### Spotify Login
The app logs in to the Spotify Web API using its most secure OAuth flow, authorization code with a PKCE key, with vanilla Java. If no account credentials are provided, the app fallsback to login using a client authorization flow, which has limited preview URL access to Spotify song data.

### Graphics
This application provides incredibly sharp graphics by creating nearly all UI images in BufferedImages with the Graphics2D API at a scaled high resolution. These BufferedImages are then painted using Graphics2D objects at the inversed resolution scale to provide sharp images no matter the desktop display settings (only Windows tested). A similar technique is used to display the same scaled high quality for song album art.