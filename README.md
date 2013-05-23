WARC Explorer
=============


Wayback Player
--------------

Please note that this application requires the Java 7 JDK (not just the JRE). 

Download: [warc-explorer-dist-1.0.0-20130523.202703-3-bin.zip](https://oss.sonatype.org/content/groups/public/uk/bl/wa/warc-explorer/warc-explorer-dist/1.0.0-SNAPSHOT/warc-explorer-dist-1.0.0-20130523.202703-3-bin.zip)

Unpacked it, and then run the appropriate script, e.g.

    % ./bin/wayback-player <warc folder name>

Keep an eye on the logs until the indexer has finished processing your ARC and WARC files, and then go to [http://localhost:18080/wayback/](http://localhost:18080/wayback/) to look at the resources in your warc files.

You can change the server hostname if you wish, using the -s command. The -i command can be used to control index caching. Use the -h option for inline help.

The embedded Wayback is also configured to support access via proxy mode on port 18090.

See this page for information about the JDK dependence: http://wiki.eclipse.org/Jetty/Howto/Configure_JSP
