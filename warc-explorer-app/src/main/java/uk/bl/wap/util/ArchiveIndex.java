/**
 * 
 */
package uk.bl.wap.util;

import static org.archive.io.warc.WARCConstants.HEADER_KEY_TYPE;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class ArchiveIndex {
	//private static final Log LOGGER = LogFactory.getLog( ArchiveIndex.class );
	
	private ArchiveReader archiveReader;
	private File file;
	private List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();

	public ArchiveIndex(String archive) {
		file = new File(archive);
		index();
	}

	private void index() {
		try {
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
				System.out.println("Archive Entry: "+path);
				entries.add( entry );
			}
		} catch( Exception e ) {
			//LOGGER.error( "createNodes(): " + e.getMessage(), e );
		}

	}


	public String getFileName() {
		return file.getName();
	}


	public List<ArchiveEntry> getEntries() {
		return this.entries;
	}
	
	public ArchiveEntry lookup( URL path ) {
		if( path == null ) return null;
		//System.out.println("Looking up: "+path);
		for( ArchiveEntry entry : this.getEntries() ) {
			try {
				URL item = new URL(entry.name);
				//System.out.println("Comparing with: "+entry.getName());
				if( item.sameFile(path)) {
					System.out.println("Found: "+path);
					return entry;
				}
			} catch (MalformedURLException e) {
			}
		}
		System.err.println("Not Found: "+path);
		return null;
	}
	
	public Resource getResource( ArchiveEntry entry ) {
		// Get the resource:
		try {
			if( entry == null ) {
				System.err.println("ERROR: ArchiveEntry should not be NULL.");
				return null;
			} else {
				return ResourceFactory.getResource(file, entry.header.getOffset());
			}
		} catch (ResourceNotAvailableException e) {
			System.err.println("Caught ResourceNotAvailableException: "+e);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Caught IOException: "+e);
			e.printStackTrace();
		} catch (Exception e ) {
			System.err.println("Caught Exception: "+e);
			e.printStackTrace();			
		}
		return null;
	}

}
