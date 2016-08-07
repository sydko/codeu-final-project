/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagedisplaygui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author lucyxiao
 */
public class ImageDisplayGUI extends Application {
//        private GridPane grid = new GridPane();
        private int spacing = 5;
        private int screenWidth;
        private int screenHeight;
        ObservableList<String> entries = FXCollections.observableArrayList();    
        ListView list = new ListView();
        GridPane grid = new GridPane();
        GridPane grid2 = new GridPane();
        ScrollPane sp = new ScrollPane();

        
    @Override
    public void start(Stage primaryStage) throws NoSuchAlgorithmException, KeyManagementException {
        
        grid = initializeGridLayout(grid);

        getScreenWidthHeight();
        Scene scene = new Scene(grid, screenWidth, screenHeight);
        scene.getStylesheets().add("imagedisplaygui/ImageDisplayCss/ImageDisplayGUI.css");
        
        
        fixHttpsIssue();
        
        ArrayList<String> imgArr = getStrings();
               
//        Text scenetitle =  new Text("Images");
//        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        primaryStage.setTitle("Simple Search");
        
        // Set up the ListView
        list.setMaxHeight(100);

        for (String entry : imgArr) {
            entries.add(entry);
        }
        list.setItems( entries );

//        grid2.setGridLinesVisible(true);
//        grid.setGridLinesVisible(true);
//        ScrollPane sp = new ScrollPane(grid2);
        sp.setContent(grid2);
        grid.add(sp, 0, 2);
        grid.setHgrow(sp, Priority.ALWAYS);
        
        TextField txt = new TextField();
        txt.setPromptText("Search");
        txt.setPrefSize(screenWidth*0.6,20);
        txt.getStyleClass().add("form-control");
        txt.textProperty().addListener(
            new ChangeListener() {
                public void changed(ObservableValue observable, 
                                    Object oldVal, Object newVal) {
                    handleSearchByKey2((String)oldVal, (String)newVal);
//                    addImgToGrid();
//                    sp.setContent(grid2);
    
                }

            });
        Label scenetitle = new Label("Twitter Image Search");
        scenetitle.setFont(Font.font("Roboto", FontWeight.NORMAL, 72));
        
        Button searchButton = new Button("Search");
        searchButton.setPrefSize(100, 20);
        searchButton.getStyleClass().add("button-search");
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
//                handleSearchByKey2("", txt.textProperty().toString());
                addImgToGrid();
                sp.setContent(grid2);
            }
        });
//        grid.add(scenetitle, 0, 0, 2, 1);
        HBox root = new HBox();
        root.getChildren().addAll(txt,searchButton);
        
//        root.setPadding(new Insets(10,10,10,10));
//        root.setSpacing(2);
        
        grid.add(root, 0, 1);  
        root.setAlignment(Pos.CENTER);
        StackPane p = new StackPane();
        p.setPrefSize(700,100);
        p.getChildren().add(scenetitle);
        StackPane.setAlignment(scenetitle,Pos.CENTER);
        grid.add(p, 0,0);
//        double centerTitle  = (scenetitle.widthProperty().doubleValue()/2.0);
//        System.out.println(scenetitle.widthProperty().doubleValue());
//        root.setMargin(scenetitle, new Insets(10,centerTitle,10,centerTitle));
        
        
        primaryStage.setTitle("San Fransokyo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public void addImgToGrid() {
        grid2 = new GridPane();
        grid2.setVgap(spacing);
        int hbRow = 1;
        int hbWidth = 0;
        HBox hb = new HBox(spacing);

        double perfectImgWidth;
        int count =0 ;
        perfectImgWidth = (screenWidth - spacing * 4.0 - 17 - 20)/5.0;
            for (Object imgUrlObj : list.getItems()) {
               String imgUrl = (String) imgUrlObj;
               Image img = new Image(imgUrl, perfectImgWidth, 0, true, false, true);
               ImageView imgView = new ImageView();
               imgView.setCache(true);
               imgView.setCacheHint(CacheHint.SPEED);
               imgView.setImage(img);

                hbWidth += perfectImgWidth;
                count += 1;
                System.out.println(count + ": " + (hbWidth ) + " vs " + (screenWidth - spacing * 4.0 - 17));
               if (hbWidth >= screenWidth - spacing * 4.0 - 17) {             
                    grid2.add(hb, 0, hbRow);
                    hb = new HBox(spacing);
                    hbRow += 1;
                    hbWidth = (int) perfectImgWidth;
               }
               hb.getChildren().add(imgView);
            }
        grid2.add(hb, 0, hbRow);
        
//        return grid2;
    }
 
    public void handleSearchByKey2(String oldVal, String newVal) {
        // If the number of characters in the text box is less than last time
        // it must be because the user pressed delete
        if ( oldVal != null && (newVal.length() < oldVal.length()) ) {
            // Restore the lists original set of entries 
            // and start from the beginning
            list.setItems( entries );
        }
         
        // Break out all of the parts of the search text 
        // by splitting on white space
        String[] parts = newVal.toUpperCase().split(" ");
 
        // Filter out the entries that don't contain the entered text
        ObservableList<String> subentries = FXCollections.observableArrayList();
        for ( Object entry: list.getItems() ) {
            boolean match = true;
            String entryText = (String)entry;
            for ( String part: parts ) {
                // The entry needs to contain all portions of the
                // search string *but* in any order
                if ( ! entryText.toUpperCase().contains(part) ) {
                    match = false;
                    break;
                }
            }
 
            if ( match ) {
                subentries.add(entryText);
            }
        }
        list.setItems(subentries);
    }
    
    /**
    * @param urlClass the class containing the instance variable for array 
    * containing image urls
    * @author lucyxiao
    */
    public ArrayList<String> getStrings() {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("https://pixabay.com/static/uploads/photo/2014/05/23/12/06/cat-351926_960_720.jpg");
        arr.add("http://static.boredpanda.com/blog/wp-content/uploads/2016/04/beautiful-fluffy-cat-british-longhair-thumb.jpg");
        arr.add("http://mikecann.co.uk/wp-content/uploads/2009/12/javafx_logo_color_1.jpg");
        arr.add("https://pixabay.com/static/uploads/photo/2014/05/23/12/06/cat-351926_960_720.jpg");
        arr.add("https://pixabay.com/static/uploads/photo/2014/05/23/12/06/cat-351926_960_720.jpg");
        arr.add("http://static.boredpanda.com/blog/wp-content/uploads/2016/04/beautiful-fluffy-cat-british-longhair-thumb.jpg");
        arr.add("http://mikecann.co.uk/wp-content/uploads/2009/12/javafx_logo_color_1.jpg");
        arr.add("https://pixabay.com/static/uploads/photo/2014/05/23/12/06/cat-351926_960_720.jpg");
        arr.add("https://pixabay.com/static/uploads/photo/2014/05/23/12/06/cat-351926_960_720.jpg");
        arr.add("http://static.boredpanda.com/blog/wp-content/uploads/2016/04/beautiful-fluffy-cat-british-longhair-thumb.jpg");
        arr.add("http://mikecann.co.uk/wp-content/uploads/2009/12/javafx_logo_color_1.jpg");
        arr.add("https://pixabay.com/static/uploads/photo/2014/05/23/12/06/cat-351926_960_720.jpg");
        arr.add("https://pixabay.com/static/uploads/photo/2014/05/23/12/06/cat-351926_960_720.jpg");
        arr.add("http://static.boredpanda.com/blog/wp-content/uploads/2016/04/beautiful-fluffy-cat-british-longhair-thumb.jpg");
        arr.add("http://mikecann.co.uk/wp-content/uploads/2009/12/javafx_logo_color_1.jpg");
        arr.add("https://pixabay.com/static/uploads/photo/2014/05/23/12/06/cat-351926_960_720.jpg");
        
        return arr;
    }
    private GridPane initializeGridLayout(GridPane grid) {
//        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10,10,10,10));
        return grid;
    }
    
    private void getScreenWidthHeight() {
       // Sets size of application to system screen size.
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
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     * @throws javax.swing.UnsupportedLookAndFeelException
     */
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            System.out.println("Got in!");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "San Fransokyo TITLE");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        Application.launch(args);
        System.out.println(System.getProperty("os.name"));
    }
    
}
