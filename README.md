# SpotifyBar
This program can get authorization to the Spotify Web API through either the Client Authorization (previews) or Authorization Code + PKCE flow (user access), which it uses to make search, play, pause, transfer playback to another device, as well as other requests. I didn't quite realize how easily accessable other libraries were that already implemented all this, but it was a really good experience for learning the OAuth 2.0 client side authorization and SO much more.

It's basically a program that sits on top of other applications and easily allows for quick song searches. The main method is in PlayerLogic.java and an executable jar is still in the works, as it requires vm arguments to execute now, but an older, only Client Authorization flow, version is the SpotifyPreviewer.jar.

Its GUI is a draggable, slim bar that is almost entirely made up of created BufferedImages from the Images class, allowing images to easily be created with a higher resolution and drawn smaller with the Graphics library to the same scale inverted. Graphics can scale down about 4 times the resolution of an image while making it more defined, and this method is also used with the album art, resulting in higher definition (though 4 is somewhat too defined for album art). Its play and pause buttons, as well as the track progress bar, are additionally created from BufferedImages.

The program implements its own player for the Client Authorization flow (previews), playing the track on a seperate thread set to low priority with an AudioInputStream and plays them using the Java Sound extension library mp3spi-1.9.5-1.jar.

This program has provided good practice with REST API's, microservices, OAuth 2 authorization, good graphics in java, experience with a powerful sound API, and provided practice
with the new concepts I learned studying for the OCA Java SE 8 Programmer I Certification and for best practice Java development.
