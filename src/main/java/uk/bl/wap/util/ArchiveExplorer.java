package uk.bl.wap.util;

import static org.archive.io.warc.WARCConstants.HEADER_KEY_TYPE;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.GZIPMembersInputStream;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.warc.WARCReaderFactory;

import uk.bl.wap.util.warc.WARCRecordUtils;

/**
 * GUI enabling browsing of compressed ARC/WARC contents.
 * 
 * @author Roger G. Coram
 * @version 0.1451
 */

public class ArchiveExplorer extends JPanel implements TreeSelectionListener {
	private static final long serialVersionUID = -1104609882336524154L;
	private static final Log LOGGER = LogFactory.getLog( ArchiveExplorer.class );
	private JTree tree;
	private ArchiveReader archiveReader;
	private JTextPane outputPane;
	private JTextPane headerPane;
	private JScrollPane outputView;
	private JScrollPane headerView;
	private File file;

	private class ArchiveEntry {
		String name;
		ArchiveRecordHeader header;

		public String toString() {
			return name;
		}
	}

	public ArchiveExplorer( String archive ) {
		super( new GridLayout( 1, 0 ) );
		file = new File( archive );
		DefaultMutableTreeNode top = new DefaultMutableTreeNode( "Archive Explorer" );
		this.createNodes( top );
		tree = new JTree( top );
		tree.getSelectionModel().setSelectionMode( TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
		tree.addTreeSelectionListener( this );
		tree.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent e ) {
				TreePath[] paths = tree.getSelectionModel().getSelectionPaths();
				if( e.getClickCount() == 2 ) {
					TreePath path = tree.getSelectionModel().getSelectionPath();
					openExternal( path );
				} else {
					if( e.isMetaDown() ) {
						if( paths.length == 1 ) {
							TreePath path = paths[ 0 ];
							exportFile( path );
						}
						if( paths.length > 1 ) {
							exportFiles( paths );
						}
					}
				}
			}
		} );
		JScrollPane treeView = new JScrollPane( tree );
		outputPane = new JTextPane();
		outputPane.setEditable( false );
		outputView = new JScrollPane( outputPane );
		JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		splitPane.setTopComponent( treeView );
		splitPane.setResizeWeight( 0.5d );

		JSplitPane splitBottom = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		headerPane = new JTextPane();
		headerPane.setEditable( false );
		headerView = new JScrollPane( headerPane );
		splitBottom.setLeftComponent( headerView );
		splitBottom.setRightComponent( outputView );
		splitBottom.setResizeWeight( 0.5d );

		splitPane.setBottomComponent( splitBottom );

		Dimension minimumSize = new Dimension( 100, 50 );
		outputView.setMinimumSize( minimumSize );
		treeView.setMinimumSize( minimumSize );
		splitPane.setDividerLocation( 100 );
		splitPane.setPreferredSize( new Dimension( 500, 300 ) );
		add( splitPane );
	}

	/**
	 * 
	 * @param args
	 *            Specifies the path to the input file.
	 * 
	 */
	public static void main( String[] args ) {
		LOGGER.info( System.getProperty( "java.version" ) );
		if( args.length < 1 ) {
			System.err.println( "No input given!" );
			System.exit( 1 );
		}
		final String archive = args[ 0 ];
		if( archive.matches( "^.+arc\\.gz$" ) ) {
			javax.swing.SwingUtilities.invokeLater( new Runnable() {
				public void run() {
					init( archive );
				}
			} );
		} else {
			LOGGER.error( "Not a valid input file: " + archive );
		}
	}

	private static void init( String archive ) {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch( Exception e ) {
			System.err.println( "Couldn't use system look and feel." );
		}
		JFrame frame = new JFrame( "Archive Explorer" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.add( new ArchiveExplorer( archive ) );
		frame.pack();
		frame.setVisible( true );
	}

	@Override
	public void valueChanged( TreeSelectionEvent select ) {
		DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) tree.getLastSelectedPathComponent();
		if( node == null )
			return;

		Object nodeInfo = node.getUserObject();
		if( node.isLeaf() ) {
			ArchiveEntry entry = ( ArchiveEntry ) nodeInfo;
			this.showArchiveRecord( entry );
		}
	}

	private void createNodes( DefaultMutableTreeNode top ) {
		try {
			ArchiveEntry dirEntry = new ArchiveEntry();
			dirEntry.header = null;
			dirEntry.name = file.getName();
			DefaultMutableTreeNode root = new DefaultMutableTreeNode( dirEntry );
			top.add( root );
			if( file.getName().matches( "^.+\\.warc.gz$" ) ) {
				archiveReader = WARCReaderFactory.get( file, 0 );
			} else {
				archiveReader = ARCReaderFactory.get( file, 0 );
			}
			Iterator<ArchiveRecord> iterator = archiveReader.iterator();
			ArchiveRecord record;
			while( iterator.hasNext() ) {
				record = iterator.next();
				ArchiveRecordHeader header = record.getHeader();
				String recordType = ( String ) header.getHeaderValue( HEADER_KEY_TYPE );
				String path = "";
				path = header.getUrl();
				if( recordType != null && !recordType.equals( "response" ) ) {
					if( recordType.equals( "warcinfo" ) ) {
						path = "WARCINFO";
					} else {
						path = path + "_" + recordType.toUpperCase();
					}
				}
				ArchiveEntry entry = new ArchiveEntry();
				entry.header = header;
				entry.name = path;
				DefaultMutableTreeNode node = new DefaultMutableTreeNode( entry );
				root.add( node );
			}
		} catch( Exception e ) {
			LOGGER.error( "createNodes(): " + e.getMessage(), e );
		}
	}

	private void showArchiveRecord( ArchiveEntry entry ) {
		InputStream input = this.getPayloadStream( entry );
		try {
			if( entry.name.toLowerCase().matches( "^.+\\.(jpg|gif|png|bmp)$" ) ) {
				StyledDocument doc = ( StyledDocument ) outputPane.getDocument();
				Style style = doc.addStyle( "StyleName", null );

				byte[] image;
				if( entry.name.matches( "^.+\\.bmp$" ) ) {
					BufferedImage bmp = ImageIO.read( input );
					ByteArrayOutputStream jpg = new ByteArrayOutputStream();
					ImageIO.write( bmp, "jpg", jpg );
					image = jpg.toByteArray();
				} else {
					image = IOUtils.toByteArray( input );
				}

				StyleConstants.setIcon( style, new ImageIcon( image ) );
				outputPane.setText( "" );
				doc.insertString( 0, "ignored text", style );
			} else {
				outputPane.read( input, null );
			}
			outputPane.setCaretPosition( 0 );
		} catch( Exception e ) {
			LOGGER.error( e.toString(), e );
		} finally {
			try {
				input.close();
			} catch( IOException e ) {
				LOGGER.error( e.toString(), e );
			}
		}
	}

	private InputStream getPayloadStream( ArchiveEntry entry ) {
		Long offset = entry.header.getOffset();
		RandomAccessFile random = null;
		GZIPMembersInputStream gz = null;
		StringBuilder headers = new StringBuilder();
		try {
			random = new RandomAccessFile( file, "r" );
			random.seek( offset );
			if( random.getFilePointer() != offset ) {
				throw new IOException( "Failed to seek to " + offset );
			}
			gz = new GZIPMembersInputStream( new FileInputStream( random.getFD() ) );
			gz.setEofEachMember( true );
			headers.append( WARCRecordUtils.getHeaders( gz, false ) );
			if( entry.header.getHeaderValue( HEADER_KEY_TYPE ) != null ) {
				headers.append( WARCRecordUtils.getHeaders( gz, true ) );
			}
			headerPane.setText( headers.toString() );
		} catch( Exception e ) {
			LOGGER.error( e.toString(), e );
		}
		return gz;
	}

	private void exportFile( TreePath path ) {
		File output;
		JFileChooser chooser = new JFileChooser();
		ArchiveEntry entry = this.pathToEntry( path );
		InputStream input = this.getPayloadStream( entry );

		String url = entry.header.getUrl();
		String filename = url.substring( url.lastIndexOf( "/" ) + 1 );
		if( filename.indexOf( "?" ) != -1 ) {
			filename = filename.substring( 0, filename.indexOf( "?" ) );
		}
		chooser.setSelectedFile( new File( filename ) );

		int response = chooser.showSaveDialog( ArchiveExplorer.this );
		if( response == JFileChooser.APPROVE_OPTION ) {
			output = chooser.getSelectedFile();
			this.writeToFile( input, output );
		}
	}

	private void exportFiles( TreePath[] paths ) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory( new java.io.File( "." ) );
		chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		chooser.setAcceptAllFileFilterUsed( false );

		String separator = System.getProperty( "file.separator" );
		String root;
		ArchiveEntry entry;
		InputStream input;
		String url;
		String filename;
		int response = chooser.showOpenDialog( ArchiveExplorer.this );
		if( response == JFileChooser.APPROVE_OPTION ) {
			root = chooser.getCurrentDirectory() + separator + chooser.getSelectedFile().getName();
			for( TreePath path : paths ) {
				entry = this.pathToEntry( path );
				input = this.getPayloadStream( entry );
				url = entry.header.getUrl();
				filename = root + separator + url.substring( url.lastIndexOf( "/" ) + 1 );
				if( filename.indexOf( "?" ) != -1 ) {
					filename = filename.substring( 0, filename.indexOf( "?" ) );
				}
				LOGGER.info( filename );
				this.writeToFile( input, new File( filename ) );
			}
		}
	}

	private void openExternal( TreePath path ) {
		ArchiveEntry entry = this.pathToEntry( path );
		InputStream input = this.getPayloadStream( entry );
		String url = entry.header.getUrl();
		String filename = url.substring( url.lastIndexOf( "/" ) + 1 );
		File tmp;
		try {
			String[] com = filename.split( "\\." );
			if( com.length < 2 ) {
				tmp = File.createTempFile( com[ 0 ], ".txt" );
			} else {
				tmp = File.createTempFile( filename.split( "\\." )[ 0 ] + "___", "." + filename.split( "\\." )[ 1 ] );
			}
			tmp.deleteOnExit();
			this.writeToFile( input, tmp );
			Desktop desktop = Desktop.getDesktop();
			desktop.open( tmp );
		} catch( IOException e ) {
			LOGGER.error( e.toString(), e );
		}
	}

	private ArchiveEntry pathToEntry( TreePath path ) {
		DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) path.getPath()[ 2 ];
		if( node == null )
			return null;

		Object nodeInfo = null;
		nodeInfo = node.getUserObject();
		ArchiveEntry entry = null;
		if( node.isLeaf() ) {
			entry = ( ArchiveEntry ) nodeInfo;
		} else {
			entry = ( ArchiveEntry ) node.getLastLeaf().getUserObject();
		}

		return entry;
	}

	private void writeToFile( InputStream input, File output ) {
		try {
			FileOutputStream stream = new FileOutputStream( output );
			IOUtils.copy( input, stream );
			stream.flush();
			stream.close();
		} catch( Exception e ) {
			LOGGER.error( e.toString(), e );
		}

	}
}