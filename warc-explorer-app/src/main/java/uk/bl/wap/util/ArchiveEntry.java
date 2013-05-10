package uk.bl.wap.util;

import org.archive.io.ArchiveRecordHeader;

public class ArchiveEntry {
	String name;
	ArchiveRecordHeader header;
	
	public ArchiveEntry() {}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}
}