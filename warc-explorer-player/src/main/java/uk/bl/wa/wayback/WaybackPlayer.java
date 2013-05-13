/**
 * 
 */
package uk.bl.wa.wayback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class WaybackPlayer {

	private static final String CLI_USAGE = "WaybackPlayer [-i <index folder>] [(W)ARCs folder]";
	private static final String CLI_HEADER = "\nWaybackPlayer launches a local Wayback server for browsing through (W)ARC files. The server should be available at http://localhost:18080/wayback/, and in proxy mode at http://localhost:18090.";
	private static final String CLI_FOOTER = "";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		CommandLineParser parser = new PosixParser();
		String warcDir = null;
		String workDir = null;
		String host = "localhost";

		Options options = new Options();
		options.addOption("h", "help", false, "Show this help message.");
		options.addOption("i", "index-folder", true, "Specify a custom directory for caching the web archiving index files. Indexes are usually re-built every time you start WaybackPlayer, which might be cumbersome for large indexes. Use this option to specify a folder in which to cache the indexes.");
		options.addOption("s", "server-name", true, "Specify a server name to use, defaults to 'localhost'")
		;
		try {
			CommandLine line = parser.parse( options, args );
		   	String cli_args[] = line.getArgs();
		   	
		   	// Check there is an argument for the warcs folder:
		   	if( !( cli_args.length > 0 ) ) {
		   		printUsage(options);
		   		return;
		   	}
		   	warcDir = cli_args[0];
		   	
		   	// Show help if required:
		   	if( line.hasOption("h") ) {
		   		printUsage(options);
		   		return;
		   	}
		   	
		   	// Allow index folder to be overridden (and thus cached):
		   	if( line.hasOption("i") ) {
		   		workDir = line.getOptionValue("i");
		   	}
		   	
		   	// Allow the host to be overridden:
		   	if( line.hasOption("s") ) {
		   		host = line.getOptionValue("s");
		   	}
		   	
		} catch (ParseException e1) {
			e1.printStackTrace();
			return;
		}
		
		// An embedded server:
		Server server = new Server();

		// Default connector for playback:
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(18080);
		connector.setHost(host);

		// Connector for Proxy mode:
		ServerConnector connector2 = new ServerConnector(server);
		connector2.setPort(18090);
		connector2.setHost(host);
		
		// Attach the two connectors:
		server.setConnectors( new Connector[] { connector, connector2 } );

		// Set a property so Wayback Spring can find the WARCs etc.
		System.setProperty("wayback.warc.dir", warcDir);
		
		// Set a propery so the host and ports are known:
		System.setProperty("wayback.host", host);
		System.setProperty("wayback.port", ""+18080);
		System.setProperty("wayback.proxy.port", ""+18090);		
		
		// TODO Option to wipe it if it's there and looks like ours?
		Path waywork = null;
		if( workDir == null ) {
			waywork = Files.createTempDirectory("wayback-play", PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rwxr-xr-x")) );
			System.setProperty("wayback.work.dir", waywork.toFile().getAbsolutePath());
			waywork.toFile().deleteOnExit();
		} else {
			System.setProperty("wayback.work.dir", workDir);
			
		}
		System.err.println("Indexes held in: "+System.getProperty("wayback.work.dir") );
		

		// Set up the Wayback web app:
		WebAppContext wac = new WebAppContext();
		wac.setContextPath("/");
		// this is path to .war OR TO expanded, existing webapp; WILL FIND web.xml and parse it
		//wac.setWar("/Users/andy/Documents/workspace/waybacks/wayback-play/target/wayback-play");
		URL resourceUrl = ClassLoader.getSystemClassLoader().getResource("lib/warc-explorer-wayback-1.0.0-SNAPSHOT.war");
		// Copy to tmp space:
		File tmpWar = File.createTempFile("wayback-player", ".war");
		tmpWar.deleteOnExit();
		IOUtils.copy(resourceUrl.openStream(), new FileOutputStream(tmpWar));
		// Fire it up:
		wac.setWar(tmpWar.getAbsolutePath());
		server.setHandler(wac);
		server.setStopAtShutdown(true);

		// Launch the server:
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//
			connector.close();
			try {
				server.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( waywork != null ) {
				if( waywork.toFile().exists() ) {
					waywork.toFile().delete();
				}
			}
		}
		
		System.err.println("Bye.");
	}

	private static void printUsage(Options options) {
	      HelpFormatter helpFormatter = new HelpFormatter( );
	      helpFormatter.defaultWidth = 80;
	      helpFormatter.printHelp( CLI_USAGE, CLI_HEADER, options, CLI_FOOTER );
	}

}
