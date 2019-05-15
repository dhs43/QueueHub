## qHub
QueueHub is an Android app for interactive music queues.

### Description
Users can choose to create a session or join an existing one. They can add local music files to the queue. Only the user that created the queue will play music from their device. Other users can only add to and view the queue.

### App Walkthrough GIF
<img src="qhub.gif" width=250><br>

## Walkthrough
If youre opening the application using an emulator you can either join or create a session. You can add mp3 files from your device, or join the already created session 6709 to view a pre-loaded queue.

Right now users can start using this app by creating a session, those who create sessions are those who would have music playing on their devices. The app is meant for multiple users so another device might join said created session using the session ID provided at the top of the screen of the creator. Those who join sessions may only add to queue and view what is currently playing from the host's device.

To sum up, creater sessions may add music to queue, play music, give out the session ID, and skip songs; joined sessions may view what the host is playing and add music to queue.

Features:

RecyclerView presents the song queue on both the create and join session formats.

Use of Firebase to upload and stream files and metadata.

Use of Firebase to create sessions and join them.

Easily traversable and understandable user interface [dark theme]

### Open-source libraries used 
- [Glide](https://github.com/bumptech/glide) - Image loading and caching library for Androids
- [Sliding Panel](https://github.com/umano/AndroidSlidingUpPanel) - Simple way to add a draggable sliding up panel
