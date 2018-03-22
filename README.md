# all_these_faces
Currently the app goes through the basic pipeline for face detection i.e. 
1. Capturing images from a stream that is also shown as a preview onscreen.
2. Processing the image and passing it to the Tensorflow Object Detection API.
3. The API will run a given model on the image and return locations of bounding boxes with confidence.
4. These will be drawn on the camera preview.

To be done - 
1. The processing is lacking and needs to be improved to get detections.
2. Skipping the pipeline if an image is already in process.
