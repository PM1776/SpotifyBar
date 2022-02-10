# SpotifyPreviewer
This program searches for songs on the Spotify servers and plays the preview of the first song returned from the query.

It's GUI is a draggable slim bar that always sits on top of applications and is entirely made up of created BufferedImages from the Images class, which are scaled to a higher 
resolution and drawn smaller with the Graphics API to the according scale. This method is also used with the album art, resulting in high definition. It's buttons 
are additionally created with BufferedImages.

As this program only uses the simplest access to Spotify servers, it doesn't really talk with the Spotify application on the device, so this program implements its 
own player, including a play/pause button and a track player bar. It plays the track on a seperate thread with an AudioInputStream and plays the mp3 previews using the
Java Sound extension library mp3spi-1.9.5-1.jar. This program doesn't currently play sound as an executable jar unfortunately.

While I hope to one day convert this program to access Spotify servers with user credentials, which would definitely make it way more 2020's, this program gave challenging 
and good practice with REST API's, brought research to produce the best graphics I've ever had in a java program, and provided practice with new concepts I learned
studying for the OCA Java SE 8 Programmer Certification.
