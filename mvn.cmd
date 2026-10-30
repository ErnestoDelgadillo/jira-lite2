@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin;%PATH%"
"C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" %*
