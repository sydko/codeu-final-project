package view;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

import model.*;

public class ImageDisplayGUI extends Application {
        //spacing is the space between two images
        private int spacing = 5;
        private int numImgPerRow = 5;
        private int screenWidth;
        private int screenHeight;
        ArrayList<String> entries = new ArrayList<String>();    
        GridPane grid = new GridPane();
        GridPane grid2 = new GridPane();
        ScrollPane sp = new ScrollPane();
        Map<String, Map<String, Double>> index;
 
    @Override
    public void start(Stage primaryStage) throws NoSuchAlgorithmException, KeyManagementException, IOException, GeneralSecurityException {
        //Initialize the main grid layout
        grid = initializeGridLayout(grid);

        //Initializes user's screenWidth and screenHeight
        getScreenWidthHeight();
        Scene scene = new Scene(grid, screenWidth, screenHeight);
        scene.getStylesheets().add(this.getClass().getResource("../css/ImageDisplayGUI.css").toExternalForm());
        
        //Some links have a certificate issue, this will bypass most of them
        fixHttpsIssue();
        
        //Initialize Array of URL strings
        String source = "https://twitter.com/BarackObama";
        TwitterCrawler wc = new TwitterCrawler(source);
        wc.crawl(false, 50);
        System.out.println("Found " + wc.numberOfImages() + " images!");
        index = ImageTermFactory.getTermMap(wc.getImages());

        System.out.println("Finished indexing");
        entries.addAll(new ArrayList<>(index.keySet()));


//        grid2.setGridLinesVisible(true);
//        grid.setGridLinesVisible(true);

        //Tells the ScrollPane to scroll only grid2
        //grid2 is the grid for images
        sp.setContent(grid2);
        grid.add(sp, 0, 2);
        grid.setHgrow(sp, Priority.ALWAYS);
        
        //Adds search form
        TextField txt = new TextField();
        txt.setPromptText("Search");
        txt.setPrefSize(screenWidth*0.6,20);
        txt.getStyleClass().add("form-control");

        //Adds search button
        Button searchButton = new Button("Search");
        searchButton.setPrefSize(100, 20);
        searchButton.getStyleClass().add("button-search");
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                updateList(txt.getCharacters().toString());
                addImgToGrid(primaryStage);
                sp.setContent(grid2);
            }
        });
        
        //Creates the container for search
        HBox searchBox = new HBox();
        searchBox.getChildren().addAll(txt,searchButton);    
        grid.add(searchBox, 0, 1);  
        searchBox.setAlignment(Pos.CENTER);
        
        //Creates page title label
        Label scenetitle = new Label("Twitter Image Search");
        scenetitle.setFont(Font.font("Tamoha", FontWeight.NORMAL, 72));
        StackPane p = new StackPane();
        p.setPrefSize(700,100);
        p.getChildren().add(scenetitle);
        StackPane.setAlignment(scenetitle, Pos.CENTER);
        grid.add(p, 0, 0);

        addImgToGrid(primaryStage);
        sp.setContent(grid2);
        //Sets up page
        primaryStage.setTitle("San Fransokyo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    //Called everytime Search is clicked
    //Updates list of images relevant to search term

    public void updateList(String newVal) {
        // Break out all of the parts of the search text
        // by splitting on white space
        String[] parts = newVal.toLowerCase().split(" ");
 
        // Filter out the entries that don't contain the entered text
        ArrayList<String> subentries = new ArrayList();
        Map<String, Double> presort = ImageTermFactory.getRelevantURLs(index, parts[0]);
        List<Entry<String, Double>> sorted  = new LinkedList<>(presort.entrySet());
             sorted.sort((a, b) -> {
                 double test = b.getValue() - a.getValue();
                 if (test < 0) {
                     return -1;
                 } else if (test > 0) {
                     return 1;
                 } else {
                     return 0;
                 }
             });
        for (Entry<String, Double> e : sorted) {
            subentries.add(e.getKey());
        }
        entries = subentries;
    }
    
    //Updates image grid
    public void addImgToGrid(Stage primaryStage) {
        // Need to create a new grid every call/search
        grid2 = new GridPane();
        grid2.setVgap(spacing);
        // hbRow : Grid Row Number
        int hbRow = 1;
        // hbWidth : HBox Width so far
        int hbWidth = 0;
        HBox hb = new HBox(spacing);

        int count = 0;
        
        // Calculates width for images to fit numImgPerRow
        // ScrollBar has width 17 pixels, grid2 has inset 10 pixel each side
        // Double counted spacing for last image in row so we have to subtract spacing
        double perfectImgWidth = (screenWidth - spacing * 4.0 - 17 - 20)/numImgPerRow;
            
        for (Object imgUrlObj : entries) {
           String imgUrl = (String) imgUrlObj;
           //Set settings to make images load faster
           Image img = new Image(imgUrl, perfectImgWidth, 0, true, false, true);
           ImageView imgView = new ImageView();
           imgView.setCache(true);
           imgView.setCacheHint(CacheHint.SPEED);
           imgView.setImage(img);

            // If image is clicked, copy URL to clipboard
            imgView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(imgUrl);
                    content.putHtml("<b>"+imgUrl+"</b>");
                    clipboard.setContent(content);  

                    //Displays a quick and temporary modal 
                    Label label = new Label("Copied URL to Clipboard");
                    label.setFont(Font.font("Tamoha", FontWeight.NORMAL, 36));
                    StackPane centeredLabel = new StackPane(label);
                    centeredLabel.setStyle("-fx-background-color: white; -fx-opacity: 0.8;");

                    StackPane stack = new StackPane(centeredLabel);
                    stack.setPrefSize(100, 50);
                    stack.setPadding(new Insets(10));

                    grid.add(stack, 0, 2);
                    stack.setAlignment(Pos.CENTER);

                    // Creates a sleeper task to close modal automatically
                    Task<Void> sleeper = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            try { 
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            return null;
                        }
                    };
                    sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            grid.getChildren().remove(stack);
                        }
                    });
                    try {
                        new Thread(sleeper).start();  
                    } catch (Exception e) {
                        grid.getChildren().remove(stack);
                    }
                }
            });
            
            hbWidth += perfectImgWidth;
            count += 1;
            System.out.println(count + ": " + (hbWidth ) + " vs " + (screenWidth - spacing * 4.0 - 17));
            
             // If row width is bigger than available space, add img to next row
            // ScrollBar has width 17 pixels, grid2 has inset 10 pixel each side
            // Double counted spacing for last image in row so we have to subtract spacing
            if (hbWidth >= screenWidth - spacing * 4.0 - 17 - 20) {             
                grid2.add(hb, 0, hbRow);
                hb = new HBox(spacing);
                hbRow += 1;
                hbWidth = (int) perfectImgWidth;
            }
            hb.getChildren().add(imgView);
        }
        // Add last row
        grid2.add(hb, 0, hbRow);
        
    }

    private GridPane initializeGridLayout(GridPane grid) {
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10,10,10,10));
        return grid;
    }
    
    // Sets size of application to system screen size.
    private void getScreenWidthHeight() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        screenWidth = gd.getDisplayMode().getWidth();
        screenHeight = gd.getDisplayMode().getHeight();
    }
    
    // From http://stackoverflow.com/questions/1219208/is-it-possible-to-get-java-to-ignore-the-trust-store-and-just-accept-whatever/5671038#5671038
    private void fixHttpsIssue() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {return null;}
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[] { trm }, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
    
}
