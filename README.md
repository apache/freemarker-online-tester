FreeMarker Online Tester [![Build Status](https://travis-ci.org/apache/incubator-freemarker-online-tester.svg?branch=master)](https://travis-ci.org/apache/incubator-freemarker-online-tester) [![Coverage Status](https://coveralls.io/repos/github/apache/incubator-freemarker-online-tester/badge.svg?branch=master)](https://coveralls.io/github/apache/incubator-freemarker-online-tester?branch=master)
====================

FreeMarker Online Tester is a web page to quickly try template snippets. It's especially handy if the real application
doesn't let you iterate quickly, and you have no much FreeMarker experience.
 
For a deployed version of this tool you can visit <http://freemarker-online.kenshoo.com/> (or soon
<http://try.freemarker.org/>)

Development Instructions
------------------------
* Clone the repository to a local directory
* Run "./gradlew build" from the cloned directory (use JDK 8)
* If you want to run it using IDEA run "./gradlew cleanidea idea"; this will generate the IDEA project for you.
* For running the software from a command line, build `shadowJar` (not `jar`) and then just issue
  `java -jar build/libs/freemarker-online-<VERSION>.jar server  src/main/resources/freemarker-online.yml`, then visit
   <http://localhost:8080/>

License
-------

FreeMarker-Online is licensed under the Apache License, Version 2.0. See LICENSE.txt for details.