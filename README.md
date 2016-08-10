#Group 57: Team San Fransokyo
##Members:
Sydney Ko, Thomas Lilly, Lucy Xiao
##Mentor:
Derek Sollenberger

#Features:
##Command Line/UX:
- Search queries and return images
- Image URL is copied to clipboard from GUI upon clicking image

##Image Search Functionality:
- Initially use Google Vision API to search for relevant images
- Ultimate goal is to be able to return relevant image results in a timely manner

##Expanding Beyond Wikipedia:
- Using Twitter as source of images, particularly useful because Twitter doesn't have an image search function
- Using Twitter Profiles of Political figures, starting at Barack Obama

#Instructions for Running:
Run these commands while in the root directory of the repository:
~~~
$ mvn -q clean compile assembly:single
$ java -cp target/final-project-1.0-SNAPSHOT-jar-with-dependencies.jar:src/main/resources/: view.ImageDisplayGUI
~~~
Users should have GOOGLE_APPLICATION_CREDENTIALS set as an environment variable, and have access to a Google Cloud Vision API key. We have noticed that there are fewer issues when not running on Windows.
