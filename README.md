# Overview
This project creates an innovative image classification app designed to amplify the VIVID Sydney festival experience by spotlighting the artists behind the art installations. The app's primary motivation is to provide a platform for these talented artists to be recognized and appreciated for their remarkable contributions. By utilizing a robust convolutional neural network (CNN) architecture, the app enables users to quickly identify famous landmarks and discover additional works by the respective artists showcased during VIVID Sydney.

In this repository, please view SqueezeNet.ipynb and MobileNet.ipynb for the python notebook. Further details of the report can be seen in the word document.

# 🚀 Main Feature
Upload a picture, and it will show you the location and details of the installations.

# 🛠️ Functionality
- **Find the location of an art installation**
  Users can upload a picture of an art installation found online, and the app will pinpoint its exact location on the map.
- **Learn more about an art installation**
  Users can take or upload a picture, and the app will display details such as the artist, background story, and inspiration behind the work.

# 🔍 How It Works
- Captured 360° videos of buildings and installations featured in the VIVID display.
- Extracted individual frames from the videos to build a rich image dataset.
- Trained SqueezeNet and MobileNet (CNN architectures) to recognize installations or landmarks from uploaded photos.
- Set up a Firebase database to store detailed information about each landmark or installation.
- When a user uploads a picture, the trained model identifies the installation and retrieves related information from Firebase.
