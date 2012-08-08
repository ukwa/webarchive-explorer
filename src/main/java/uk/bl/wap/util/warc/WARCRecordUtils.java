package uk.bl.wap.util.warc;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import org.archive.io.GZIPMembersInputStream;
import org.glimmerblocker.proxy.httpClient.stream.FixedLengthInputStream;

public class WARCRecordUtils {
	private final static Logger LOGGER = Logger.getLogger( WARCRecordUtils.class.getName() );

	public static String getHeaders( InputStream input, boolean isBlock ) {
		StringBuilder headers = new StringBuilder();
		String line;
		try {
			line = WARCRecordUtils.readLine( input );
			if( isBlock && line.indexOf( "HTTP" ) != 0 ) {
				return headers.toString();
			}
			while( !line.matches( "^\\s*$" ) ) {
				headers.append( line );
				line = WARCRecordUtils.readLine( input );
			}
			headers.append( line );
		} catch( IOException e ) {
			LOGGER.warning( "getHeaders(): " + e.toString() );
		}
		return headers.toString();
	}

	public static GZIPMembersInputStream getWARCRecord( RandomAccessFile random, Long offset ) {
		GZIPMembersInputStream gz = null;
		try {
			random.seek( offset );
			if( random.getFilePointer() != offset ) {
				throw new IOException( "Failed to seek to " + offset );
			}
			gz = new GZIPMembersInputStream( new FileInputStream( random.getFD() ) );
			gz.setEofEachMember( true );
		} catch( Exception e ) {
			LOGGER.warning( "getWARCRecord(): " + e.toString() );
			e.printStackTrace();
		}
		return gz;
	}

	public static InputStream getRawGzip( RandomAccessFile random, long offset, long gzipSize ) {
		FixedLengthInputStream fixed = null;
		try {
			random.seek( offset );
			if( random.getFilePointer() != offset ) {
				throw new IOException( "Failed to seek to " + offset );
			}
			fixed = new FixedLengthInputStream( new FileInputStream( random.getFD() ), gzipSize );
		} catch( Exception e ) {
			LOGGER.warning( "getRawGzip(): " + e.toString() );
			e.printStackTrace();
		}
		return fixed;
	}

	public static long getStreamLength( InputStream input ) throws IOException {
		long length = 0;
		int ch;
		byte[] buffer = new byte[ 1024 ];
		while( ( ch = input.read( buffer ) ) >= 0 ) {
			length += ch;
		}
		return length;
	}

	public static String readLine( InputStream inputstream ) throws IOException {
		int chr;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		while( ( chr = inputstream.read() ) >= 0 ) {
			buffer.write( chr );
			if( chr == '\n' ) {
				break;
			}
		}
		return new String( buffer.toByteArray() );
	}
}
