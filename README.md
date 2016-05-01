# Picender Android Client
Android client app for Picender


PICENDER > Image backup system which sync's pics to a local server at
your home. Main motivation is to free up space on the phone

Composed of
- android client (picender_android_client : https://github.com/mehtarupesh/picender_android_client)
   - Scans all media directories which hold images.
   - allows to 'select', 'upload', 'delete' images
   - 'delete' always stores to server before deleting.

  App is based on top of :
   - https://github.com/Suleiman19/Gallery
   - Description : 'Made a simple Gallery app quickly with Android Studio 1.4's default Activity Templates. Used Glide for image loading and caching'

- server (picender : https://github.com/mehtarupesh/picender)
python based server.
