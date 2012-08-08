package webviewsample;

import java.net.URL;

import uk.bl.wap.util.ArchiveURLStreamHandlerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import netscape.javascript.JSObject;

public class Main extends Application {

    private Scene scene;

    @Override
    public void start(Stage stage) {
        // create scene
        stage.setTitle("Web View");
        scene = new Scene(new Browser(), 750, 500, Color.web("#666970"));
        stage.setScene(scene);
        scene.getStylesheets().add("webviewsample/BrowserToolbar.css");
        // show stage
        stage.show();
    }

    public static void main(String[] args) {
    	URL.setURLStreamHandlerFactory(new ArchiveURLStreamHandlerFactory(args[0]));
        launch(args);
    }
}

class Browser extends Region {

    private HBox toolBar;
    private static String[] imageFiles = new String[]{
        "product.png",
        "blog.png",
        "forum.png",
        "partners.png",
        "help.png"
    };
    private static String[] captions = new String[]{
        "Products",
        "Blogs",
        "Forums",
        "Partners",
        "Help"
    };
    private static String[] urls = new String[]{
        "http://en.wikipedia.org/wiki/Main_Page",
        "http://blogs.oracle.com/",
        "http://forums.oracle.com/forums/",
        "http://www.oracle.com/partners/index.html",
        Main.class.getResource("help.html").toExternalForm()
    };
    final ImageView selectedImage = new ImageView();
    final Hyperlink[] hpls = new Hyperlink[captions.length];
    final Image[] images = new Image[imageFiles.length];
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    final Button hideAll = new Button("Hide All");
    final Button showAll = new Button("ShowAll");
    final WebView smallView = new WebView();

    public Browser() {
        //apply the styles
        getStyleClass().add("browser");

        // load the home page        
        webEngine.load("http://en.wikipedia.org/wiki/Main_Page");
        

        for (int i = 0; i < captions.length; i++) {
            final Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
            Image image = images[i] =
                new Image(getClass().getResourceAsStream(imageFiles[i]));
            hpl.setGraphic(new ImageView(image));
            final String url = urls[i];
            
            hpl.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    webEngine.load(url);
                    webEngine.getLoadWorker().stateProperty().addListener(
                        new ChangeListener<State>() {  
                            @Override
                            public void changed(ObservableValue<? extends State>
                                ov, State oldState, State newState) {
                            	System.out.println("State: "+newState);
                                if (!hpl.getText().equals("Forums")) { 
                                    toolBar.getChildren().removeAll(showAll, 
                                            hideAll); }
                                if (newState == State.SUCCEEDED) {
                                    JSObject win = (JSObject) 
                                    webEngine.executeScript("window");
                                    win.setMember("app", new JavaApp());
                                    if (hpl.getText().equals("Forums")) {
                                        toolBar.getChildren().removeAll(showAll, 
                                                hideAll);
                                        toolBar.getChildren().addAll(showAll, 
                                                hideAll);
                                    }
                                }
                            }
                        });
                }
            });
        }
        // create the toolbar
        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().addAll(hpls);
        toolBar.getChildren().add(createSpacer());

        hideAll.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                webEngine.executeScript("hideAll()");
            }
        });

        showAll.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                webEngine.executeScript("showAll()");
            }
        });

        smallView.setPrefSize(120, 80);
        webEngine.setCreatePopupHandler(
            new Callback<PopupFeatures, WebEngine>() {
                @Override
                public WebEngine call(PopupFeatures config) {
                    smallView.setFontScale(0.8);
                    if (!toolBar.getChildren().contains(smallView)) {
                        toolBar.getChildren().add(smallView);
                    }
                    return smallView.getEngine();
                }
        });
        //add components
        getChildren().add(toolBar);
        getChildren().add(browser);
    }

    // JavaScript interface object
    private class JavaApp {
        public void exit() {
            Platform.exit();
        }
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser,0,0,w,h-tbHeight,0,HPos.CENTER,VPos.CENTER);
        layoutInArea(toolBar,0,h-tbHeight,w,tbHeight,0,HPos.CENTER,VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 600;
    }
    
}