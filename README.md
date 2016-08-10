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


##Running the project
- Must have GOOGLE_APPLICATION_CREDENTIALS environment variable to authenticate api requests

1. Navigate to root directory of project
2. run command: 'mvn -q clean compile assembly single'
3. Unix command: 'java -cp target/final-project-1.0-SNAPSHOT-jar-with-dependencies.jar:src/main/resources/: view.ImageDisplayGUI' or Windows Command: 'java -cp target/final-project-1.0-SNAPSHOT-jar-with-dependencies.jar;src/main/resources/; view.ImageDisplayGUI'
4. Wait about 1-2 minutes depending on the internet connection to allow the program to crawl and label the images
5. Search!