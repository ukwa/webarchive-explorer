/**
 * 
 */
package uk.bl.wap.util;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class ArchiveURLStreamHandlerFactory implements URLStreamHandlerFactory {

	private String archive;

	public ArchiveURLStreamHandlerFactory( String archive ) {
		this.archive = archive;
	}
	
	/* (non-Javadoc)
	 * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
	 */
	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if( "http".equals(protocol) ) {
			return new ArchiveURLStreamHandler(archive);
		}
		return null;
	}

}
