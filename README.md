Web Archive Explorer
====================

[![Build Status](https://travis-ci.org/ukwa/webarchive-explorer.png?branch=master)](https://travis-ci.org/ukwa/webarchive-explorer)

Wayback Player
--------------

Please note that this application requires the Java 7 JDK (not just the JRE). It should be possible to run it against a Java 6 JRE, but this is not tested at present.

Download: [warc-explorer-dist-1.0.0-bin.zip](https://search.maven.org/remotecontent?filepath=uk/bl/wa/warc-explorer/warc-explorer-dist/1.0.0/warc-explorer-dist-1.0.0-bin.zip)

Unpacked it, and then run the appropriate script, e.g.

    % ./bin/wayback-player <warc folder name>

Keep an eye on the logs until the indexer has finished processing your ARC and WARC files, and then go to [http://localhost:18080/wayback/](http://localhost:18080/wayback/) to look at the resources in your warc files.

You can change the server hostname if you wish, using the -s command. The -i command can be used to control index caching. Use the -h option for inline help.

The embedded Wayback is also configured to support access via proxy mode on port 18090.

See this page for information about the JDK dependence: http://wiki.eclipse.org/Jetty/Howto/Configure_JSP

Known Issues
------------

There are some issues with Wayback playback at the moment, noted here for future resolution.


### Bug, recent regression. ###

As noted in [this comment](https://github.com/ukwa/webarchive-explorer/issues/5#issuecomment-18516339), and appears to arise due to problems with the 
way wget-1.14 build gzip streams (see [this other issue](https://github.com/ukwa/webarchive-discovery/issues/1))

With a compressed WARC, with 1.8.0-SNAPSHOT, with a specific WARC.GZ:

    java.io.IOException: /Users/andy/Documents/workspace/warc-explorer/warcs-3/drupalib.interoperating.info.warc.gz is not a WARC file.
        at org.archive.io.warc.WARCReaderFactory.getArchiveReader(WARCReaderFactory.java:87)
        at org.archive.io.ArchiveReaderFactory.getArchiveReader(ArchiveReaderFactory.java:103)
        at org.archive.io.warc.WARCReaderFactory.get(WARCReaderFactory.java:66)
        at org.archive.wayback.resourcestore.indexer.WarcIndexer.iterator(WarcIndexer.java:71)
        at org.archive.wayback.resourcestore.indexer.IndexWorker.indexFile(IndexWorker.java:136)
        at org.archive.wayback.resourcestore.indexer.IndexWorker.doWork(IndexWorker.java:110)
        at org.archive.wayback.resourcestore.indexer.IndexWorker$WorkerThread.run(IndexWorker.java:244)

This seems to stem from [here](https://github.com/internetarchive/ia-web-commons/blob/master/src/main/java/org/archive/format/gzip/GZIPDecoder.java#L129):

    org.archive.io.warc.WARCReaderFactory.getArchiveReader(File, long)
    and this test, failing to spot that the file is compressed:
    org.archive.io.warc.WARCReaderFactory.testCompressedWARCFile(File)

which relies on:

    org.archive.util.ArchiveUtils.isGzipped(InputStream)
    org.archive.format.gzip.GZIPDecoder.parseHeader(InputStream)

Not sure why this is failing to return TRUE, but maybe this is tripping on the wget WARC variant.


### Bug, long-standing fault/limitation. ###

With an uncompressed WARC:

    org.archive.wayback.exception.ResourceNotAvailableException: /Users/andy/Documents/workspace/warc-explorer/warcs-2/drupalib.interoperating.info.warc - java.util.zip.ZipException: Not in GZIP format
    at org.archive.wayback.resourcestore.LocationDBResourceStore.retrieveResource(LocationDBResourceStore.java:96)
    at org.archive.wayback.webapp.AccessPoint.getResource(AccessPoint.java:533)
    at org.archive.wayback.webapp.AccessPoint.handleReplay(AccessPoint.java:676)
    at org.archive.wayback.webapp.AccessPoint.handleRequest(AccessPoint.java:293)
    at org.archive.wayback.util.webapp.RequestMapper.handleRequest(RequestMapper.java:185)
    at org.archive.wayback.util.webapp.RequestFilter.doFilter(RequestFilter.java:146)
    at org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1419)
    at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455)
    at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)
    at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:557)
    at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)
    at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1075)
    at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:384)
    at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)
    at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1009)
    at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)
    at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)
    at org.eclipse.jetty.server.Server.handle(Server.java:370)
    at org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:489)
    at org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:949)
    at org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:1011)
    at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:644)
    at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)
    at org.eclipse.jetty.server.AsyncHttpConnection.handle(AsyncHttpConnection.java:82)
    at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:668)
    at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:52)
    at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)
    at org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)
    at java.lang.Thread.run(Thread.java:722)

This boils down to the assumption that the WARCS are compressed in:

    org.archive.io.warc.WARCReaderFactory.getArchiveReader(String, InputStream, boolean)
    
Called by:

    org.archive.wayback.resourcestore.LocationDBResourceStore.retrieveResource(CaptureSearchResult)

Note also the clumsy calling class, which duplicated isArc/isWarc logic:

    org.archive.wayback.resourcestore.resourcefile.ResourceFactory.getResource(File, long)
