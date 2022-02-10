# SpotifyPreviewer
This program searches for songs from Spotify servers and plays the preview of the first song returned from the query. It's main method is in PlayerLogic.java.

Its GUI is a draggable, slim bar that always sits on top of applications and is entirely made up of created BufferedImages from the Images class, allowing them to be
scaled easily to a higher resolution and drawn smaller with the Graphics library to the same scale inverted. Graphics can scale down about 4 times the resolution of an image while
making it more defined, and this method is also used with the album art, resulting in high definition (though 4 is somewhat too defined). It's buttons are additionally created
with BufferedImages.

As this program uses the simplest access to Spotify servers, it doesn't really talk with the Spotify application on the device, so this program implements its 
own player, including a pause/play button and a track player bar. It plays the track on a seperate thread with low priority using an AudioInputStream and plays the mp3 previews
using the Java Sound extension library mp3spi-1.9.5-1.jar. This program currently doesn't play sound as an executable jar due to problems with the AudioInputStream, though it works well in Eclipse (logged in PlayerLogic.java).

While I hope to one day convert this program to access Spotify servers with user credentials, which would definitely make it way more 2020's, this program gave good 
practice with REST API's, brought research to produce the best graphics I've ever had in a java program, and provided practice with the new concepts I learned studying 
for the OCA Java SE 8 Programmer I Certification.
