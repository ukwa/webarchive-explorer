/**
 * 
 */
package uk.bl.wap.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class ArchiveURLConnection extends URLConnection {

	private ArchiveIndex archiveIndex;
	private ArchiveEntry entry;

	protected ArchiveURLConnection(URL url) {
		super(url);
	}

	public ArchiveURLConnection(ArchiveIndex archiveIndex, URL u) {
		super(u);
		this.archiveIndex = archiveIndex;
		this.entry = archiveIndex.lookup(getURL());
	}

	@Override
	public void connect() throws IOException {
		if( entry == null  || entry.name == null ) {
			throw new IOException("Resource not in archive.");
		}
	}
	
	/* (non-Javadoc)
	 * @see java.net.URLConnection#getHeaderField(java.lang.String)
	 */
	@Override
	public String getHeaderField(String name) {
		if( entry.header == null ) return "";
		return archiveIndex.getResource(entry).getHttpHeaders().get(name);
	}

	/* (non-Javadoc)
	 * @see java.net.URLConnection#getHeaderFields()
	 */
	@Override
	public Map<String, List<String>> getHeaderFields() {
		Map<String, List<String>> hfm = new HashMap<String, List<String>>();
		Map<String, String> headers = archiveIndex.getResource(entry).getHttpHeaders();
		if( entry.header == null ) return hfm;
			for( String hf : headers.keySet() ) {
			String v = headers.get(hf);
			ArrayList<String> vlist = new ArrayList<String>();
			vlist.add( (String)v);
			hfm.put(hf,vlist);
		}
		return hfm;
	}

	/* (non-Javadoc)
	 * @see java.net.URLConnection#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		connect();
		return archiveIndex.getResource(entry);
	}

}
