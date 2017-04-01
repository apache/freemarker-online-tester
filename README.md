freemarker-online
====================

freemarker-online is a tool for any freemarker users to evalaute their freemarker expressions with an input.
You can use it for enabling non developers to develop freemarker templates and evaluate it's values without the need for a developement enviornment.
For a deployed version of this tool you can visit http://freemarker-online.kenshoo.com/

Development Instuctions
------------------------
* Clone the repository to a local directory
* Currently the project has an old dependency, "com.berico:fallwizard:1.1.1", which can't be found in any public repositories I know of. Thus, you have to Maven-install it locally:
  1. Clone https://github.com/Berico-Technologies/Fallwizard.git
  2. Check out this old version: 7ed7803496
  3. `mvn install` it
* Run "./gradlew build" from the cloned directory (use JDK 7, not 8!)
* If you want to run it using IDEA run "./gradlew cleanidea idea" - this will generate the IDEA project for you.
* For running the software from a command line, build `fatJar` (not `jar`) and then just hit "java -jar build/libs/freemarker-online-0.1.undef.jar server  src/main/resources/freemarker-online.yml"


License
-------

FreeMarker-Online is licensed under the Apache License, Version 2.0. See LICENSE.txt for details.