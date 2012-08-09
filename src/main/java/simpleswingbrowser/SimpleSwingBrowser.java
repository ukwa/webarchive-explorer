package simpleswingbrowser;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import uk.bl.wap.util.ArchiveURLStreamHandlerFactory;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import static javafx.concurrent.Worker.State.FAILED;

public class SimpleSwingBrowser implements Runnable {
    private JFXPanel jfxPanel;
    private WebEngine engine;

    private JFrame frame = new JFrame();
    private JPanel panel = new JPanel(new BorderLayout());
    private JLabel lblStatus = new JLabel();

    private JButton btnGo = new JButton("Go");
    private JTextField txtURL = new JTextField();
    private JProgressBar progressBar = new JProgressBar();

    private void initComponents() {
        jfxPanel = new JFXPanel();

        createScene();

        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                loadURL(txtURL.getText());
            }
        };

        btnGo.addActionListener(al);
        txtURL.addActionListener(al);

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);


        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
    }

    private void createScene() {

        Platform.runLater(new Runnable() {
            @Override public void run() {

                WebView view = new WebView();
                engine = view.getEngine();

                engine.titleProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override public void run() {
                                frame.setTitle(newValue);
                            }
                        });
                    }
                });

                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override public void handle(final WebEvent<String> event) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override public void run() {
                                lblStatus.setText(event.getData());
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override public void run() {
                                txtURL.setText(newValue);
                            }
                        });
                    }
                });

                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override public void run() {
                                progressBar.setValue(newValue.intValue());
                            }
                        });
                    }
                });
                
                engine.getLoadWorker().stateProperty().addListener(
                        new ChangeListener<Object>() {
                            @Override
                            public void changed(ObservableValue<?> observable,
                                    Object oldValue, Object newValue) {
                                State oldState = (State)oldValue;
                                State newState = (State)newValue;
                                if (State.SUCCEEDED == newValue) {
                                    captureView();
                                	engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
                                }
                            }
                        });


                engine.getLoadWorker()
                        .exceptionProperty()
                        .addListener(new ChangeListener<Throwable>() {

                            public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                                if (engine.getLoadWorker().getState() == FAILED) {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override public void run() {
                                            JOptionPane.showMessageDialog(
                                                    panel,
                                                    (value != null) ?
                                                    engine.getLocation() + "\n" + value.getMessage() :
                                                    engine.getLocation() + "\nUnexpected error.",
                                                    "Loading error...",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                                }
                            }
                        });
                
             // hide webview scrollbars whenever they appear.
                /*
                view.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
                  @Override public void onChanged(Change<? extends Node> change) {
                    Set<Node> deadSeaScrolls = webView.lookupAll(".scroll-bar");
                    for (Node scroll : deadSeaScrolls) {
                      scroll.setVisible(false);
                    }
                  }
                });
                */


                jfxPanel.setScene(new Scene(view));
            }
        });
    }

    public void loadURL(final String url) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                String tmp = toURL(url);

                if (tmp == null) {
                    tmp = toURL("http://" + url);
                }

                engine.load(tmp);
            }
        });
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
                return null;
        }
    }

    @Override public void run() {

        frame.setPreferredSize(new Dimension(1024, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();

        loadURL("http://en.wikipedia.org/wiki/Main_Page");

        frame.pack();
        frame.setVisible(true);
    }
    
    private void captureView() {
    	BufferedImage bi = new BufferedImage(jfxPanel.getWidth(), jfxPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
    	Graphics graphics = bi.createGraphics();
    	jfxPanel.paint(graphics);
    	try {
    	    ImageIO.write(bi, "PNG", new File("demo.png"));
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	graphics.dispose();
    	bi.flush();
    	//
    	try {
	    	Document doc = this.engine.getDocument();
	    	//set up a transformer
	    	TransformerFactory transfac = TransformerFactory.newInstance();
	    	Transformer trans = transfac.newTransformer();
	    	trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    	trans.setOutputProperty(OutputKeys.INDENT, "yes");
	
	    	//create string from xml tree
	    	OutputStreamWriter sw = new OutputStreamWriter(new FileOutputStream("test.xhtml"), "UTF-8");
	    	StreamResult result = new StreamResult(sw);
	    	DOMSource source = new DOMSource(doc);
	    	trans.transform(source, result);
	    	sw.flush();
	    	sw.close();
    	} catch ( Exception e ) {
    		e.printStackTrace();
    	}
    	//
    	// Get a DOMImplementation.
        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Ask the test to render into the SVG Graphics2D implementation.
        jfxPanel.paint(svgGenerator);

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes
        Writer out;
		try {
			out = new OutputStreamWriter(new FileOutputStream("test.svg"), "UTF-8");
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        try {
			svgGenerator.stream(out, useCSS);
		} catch (SVGGraphics2DIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
        	out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


    public static void main(String[] args) {
    	URL.setURLStreamHandlerFactory(new ArchiveURLStreamHandlerFactory(args[0]));
        SwingUtilities.invokeLater(new SimpleSwingBrowser());
    }
}
