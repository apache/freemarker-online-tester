FreeMarker Online Tester [![Build Status](https://travis-ci.org/apache/freemarker-online-tester.svg?branch=master)](https://travis-ci.org/apache/incubator-freemarker-online-tester) [![Coverage Status](https://coveralls.io/repos/github/apache/incubator-freemarker-online-tester/badge.svg?branch=master)](https://coveralls.io/github/apache/incubator-freemarker-online-tester?branch=master)
====================

What is FreeMarker Online Tester
--------------------------------

It's a web page to quickly try template snippets, with some simple data-model. It's especially handy for learning the
template language.
 
For a deployed version of this tool you can visit <https://try.freemarker.apache.org/>.

Development Instructions
------------------------

* Clone the repository to a local directory
* Run `./gradlew build` from the cloned directory (use JDK 8)
* If you want to run it using IDEA run `./gradlew cleanidea idea`; this will generate the IDEA project for you.
* To run the service:
  - From command line: build `shadowJar` (not `jar`) and then issue
    `java -jar build/libs/freemarker-online-<VERSION>.jar server src/main/resources/freemarker-online.yml`
  - From IDE: run class `org.apache.freemarker.onlinetester.dropwizard.FreeMarkerOnlineTester` with arguments
    `server src/main/resources/freemarker-online.yml`
* Then, to try the service locally, visit <http://localhost:8080/>

License
-------

FreeMarker-Online is licensed under the Apache License, Version 2.0. See the `LICENSE` file for details.