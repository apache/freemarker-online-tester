```
 
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 
```

freemarker-online   [![Build Status](https://travis-ci.org/apache/incubator-freemarker-online-tester.svg?branch=master)](https://travis-ci.org/apache/incubator-freemarker-online-tester)
====================

freemarker-online is a tool for any freemarker users to evalaute their freemarker expressions with an input.
You can use it for enabling non developers to develop freemarker templates and evaluate it's values without the need for a developement enviornment.
For a deployed version of this tool you can visit http://freemarker-online.kenshoo.com/

Development Instuctions
------------------------
* Clone the repository to a local directory
* Run "./gradlew build" from the cloned directory (use JDK 8)
* If you want to run it using IDEA run "./gradlew cleanidea idea" - this will generate the IDEA project for you.
* For running the software from a command line, build `fatJar` (not `jar`) and then just hit "java -jar build/libs/freemarker-online-0.1.undef.jar server  src/main/resources/freemarker-online.yml"


License
-------

FreeMarker-Online is licensed under the Apache License, Version 2.0. See LICENSE.txt for details.