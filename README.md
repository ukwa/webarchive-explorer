WARC Explorer
=============


Wayback Player
--------------

Download: [warc-explorer-player-1.0.0-20130513.145008-2-war-in-jar.jar](https://oss.sonatype.org/content/groups/public/uk/bl/wa/warc-explorer/warc-explorer-player/1.0.0-SNAPSHOT/warc-explorer-player-1.0.0-20130513.145008-2-war-in-jar.jar)

Run as:

    % java -jar warc-explorer-player-1.0.0-20130513.145008-2-war-in-jar.jar <warc folder name>

Keep an eye on the logs until the indexer has finished processing your ARC and WARC files, and then go to [http://localhost:18080/wayback/](http://localhost:18080/wayback/) to look at the resources in your warc files.

The embedded Wayback is also configured to support access via proxy mode on port 18090.


