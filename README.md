## qHub
QueueHub is an Android app for interactive music queues.

### Description
Users can choose to create a session or join an existing one. They can add local music files to the queue. By default, only the user that created the queue will play music from their device. A user that joins can also choose to "Tune In" and play the music from their device as well. This is ideal when users are contributing to the same queue, but are not in the same physical location to listen together.

### App Walkthrough GIF
<img src="YOUR_GIF_URL_HERE" width=250><br>

## Walkthrough
If youre opening the application using an emulator you can either join or create a session but unfortunately we dont have any local mp3s in the app that you could add to the queue from; So if your emulator dosen't have any local files feel free to use the already filled session id 6709 to demonstrate that on regular phones you can add songs. 

Right now users can start using this app by creating a session, those who create sessions are those who would have music playing on their devices. The app is meant for multiple users so another device might join said created session using the session ID provided at the top of the screen of the creator. Those who join sessions may only add to queue and view what is currently playing on the host (creater session).

To sum up, creater sessions may add music to queue, play music, give out the session ID, and skip songs; joined sessions may view what the host is playing and add music to queue.

Features:

Recycler View: Presents the song queue on both the create and join session formats.

Use of firebase to upload and stream files and metadata.

Use of firebase to create sessions and join them.

Use of mediaplayer.

Easily traversable and understandable user interface [dark theme]

### Notes
As of right now we have disabled the "Tune In" feature for the join session option because its a bit buggier than we had hoped and would like to work on it a bit further before we present it here, or more importantly the app store.

### Open-source libraries used 
- [Glide](https://github.com/bumptech/glide) - Image loading and caching library for Androids
- [Sliding Panel](https://github.com/umano/AndroidSlidingUpPanel) - Simple way to add a draggable sliding up panel
