package org.jwat.warc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jwat.common.Diagnosis;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

public class TestWarc {

	public static void main(String[] args) {
		File file = new File( args[0] );
		try {
			InputStream in = new FileInputStream( file );

			int records = 0;
			int errors = 0;

			WarcReader reader = WarcReaderFactory.getReader( in );
			WarcRecord record;

			while ( (record = reader.getNextRecord()) != null ) {

				++records;

				if (record.diagnostics.hasErrors()) {
					printRecord(record);
					printRecordErrors(record);
					errors += record.diagnostics.getErrors().size();
				}
			}

			System.out.println("--------------");
			System.out.println("       Records: " + records);
			System.out.println("        Errors: " + errors);
			reader.close();
			in.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printRecord(WarcRecord record) throws IOException {
		System.out.println("--------------");
		System.out.println("       Version: " + record.header.bMagicIdentified + " " + record.header.bVersionParsed + " " + record.header.major + "." + record.header.minor);
		System.out.println("       TypeIdx: " + record.header.warcTypeIdx);
		System.out.println("          Type: " + record.header.warcTypeStr);
		System.out.println("      Filename: " + record.header.warcFilename);
		System.out.println("     Record-ID: " + record.header.warcRecordIdUri);
		System.out.println("          Date: " + record.header.warcDate);
		System.out.println("Content-Length: " + record.header.contentLength);
		System.out.println("  Content-Type: " + record.header.contentType);
		System.out.println("     Truncated: " + record.header.warcTruncatedStr);
		System.out.println("   InetAddress: " + record.header.warcInetAddress);
		System.out.println("  ConcurrentTo: " + record.header.warcConcurrentToList);
		System.out.println("      RefersTo: " + record.header.warcRefersToUri);
		System.out.println("     TargetUri: " + record.header.warcTargetUriUri);
		System.out.println("   WarcInfo-Id: " + record.header.warcWarcInfoIdUri);
		System.out.println("   BlockDigest: " + record.header.warcBlockDigest);
		System.out.println(" PayloadDigest: " + record.header.warcPayloadDigest);
		System.out.println("IdentPloadType: " + record.header.warcIdentifiedPayloadType);
		System.out.println("       Profile: " + record.header.warcProfileStr);
		System.out.println("      Segment#: " + record.header.warcSegmentNumber);
		System.out.println(" SegmentOrg-Id: " + record.header.warcSegmentOriginIdUrl);
		System.out.println("SegmentTLength: " + record.header.warcSegmentTotalLength);
		System.out.println("---- Payload:");
		BufferedReader payin = new BufferedReader(new InputStreamReader(record.getPayload().getInputStream()));
		String line = null;
		while((line = payin.readLine()) != null) {
		  System.out.println(line);
		}
	}

	public static void printRecordErrors(WarcRecord record) {
		System.out.println("---- Errors:");
		if (record.diagnostics.hasErrors()) {
			List<Diagnosis> errorCol = record.diagnostics.getErrors();
			if (errorCol != null && errorCol.size() > 0) {
				Iterator<Diagnosis> iter = errorCol.iterator();
				while (iter.hasNext()) {
					Diagnosis error = iter.next();
					System.out.println( "Type:" + error.type );
					System.out.println( "Entity: "+ error.entity );
					for( String info : error.information ) {
						System.out.println( "Info: "+ info );
					}
				}
			}
		}
	}

}