/**
 * 
 */
package uk.bl.wap.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class ArchiveURLStreamHandler extends URLStreamHandler {
	
	private ArchiveIndex archiveIndex;

	public ArchiveURLStreamHandler( String archive ) {
		this.archiveIndex = new ArchiveIndex(archive);
	}

	/* (non-Javadoc)
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		return new ArchiveURLConnection(archiveIndex,u);
	}

}
