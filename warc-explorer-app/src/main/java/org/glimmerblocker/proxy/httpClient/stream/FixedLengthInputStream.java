/* Copyright (C) 2008 Peter Speck
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glimmerblocker.proxy.httpClient.stream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class FixedLengthInputStream extends InputStream
{
	private final InputStream src;
	private final long srcSize;
	private long remaining;

	public FixedLengthInputStream(InputStream src, long srcSize)
	{
		this.src = src;
		this.srcSize = srcSize;
		this.remaining = srcSize;
	}

	public int read() throws IOException
	{
		if (remaining <= 0)
			return -1;
		int x = src.read();
		if (x < 0)
			handleUnexpectedEndOfStream();
		else if (--remaining == 0)
			notifyReachedEndOfExpectedSize();
		return x;
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		if (remaining <= 0)
			return -1;
		if (len == 0)
			return 0;
		len = (int)Math.min(remaining, len);
		int num = src.read(b, off, len);
		if (num < 0) {
			handleUnexpectedEndOfStream();
			return -1;
		}
		remaining -= num;
		if (remaining == 0)
			notifyReachedEndOfExpectedSize();
		return num;
	}

	public long remaining()
	{
		return remaining;
	}

	protected void notifyReachedEndOfExpectedSize() throws IOException
	{
	}

	protected void handleUnexpectedEndOfStream() throws IOException
	{
		throw new EOFException("Unexpected end of input (remaining = " + remaining +
				", content-length = " + srcSize + ")");
	}
}
