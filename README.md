## QueueHub
QueueHub is an Android app for interactive music queues.

### Description
Users can choose to create a session or join an existing one. They can add local music files to the queue. Only the user that created the queue will play music from their device. Other users can add to and view the queue.

### App Walkthrough GIF
<img src="qhub.gif" width=250><br>

## Walkthrough
When opening the app you can either join or create a session. You can add mp3 files from your device, or join an already-created session 6709 to view a pre-loaded queue.

Those who create sessions play the music from their device, ideally over a speaker system. The app is designed for groups, so another user might join the session using the session ID provided at the top of the creator's screen. Those who join sessions may only add to the queue and view what is currently playing from the host's device.

Features:

RecyclerView presents the song queue on both the create and join session formats.

Use of Firebase to upload and stream files and metadata.

Use of Firebase to create sessions and join them.

Easily traversable and understandable user interface [dark theme]

### Open-source libraries used 
- [Glide](https://github.com/bumptech/glide) - Image loading and caching library for Androids
- [Sliding Panel](https://github.com/umano/AndroidSlidingUpPanel) - Simple way to add a draggable sliding up panel
