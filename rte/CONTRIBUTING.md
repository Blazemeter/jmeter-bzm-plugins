# Contributing

We are using a custom [checkstyle](http://checkstyle.sourceforge.net/index.html) configuration file which is based on google's one, is advisable to use one of the [google style configuration files](https://github.com/google/styleguide) in IDEs to reduce the friction with checkstyle and automate styling.

## Building

### Pre-requisites

- [jdk 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [maven 3.3+](https://maven.apache.org/)
- [xtn5250 emulator](https://sourceforge.net/projects/xtn5250/) installed in local maven repository:
  - If not already installed, run 
    ```
    mvn com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:wget -Ddownload.url=https://sourceforge.net/projects/xtn5250/files/xtn5250/1.19m/xtn5250_119m.jar
    mvn install:install-file -Dfile=target/xtn5250_119m.jar -DgroupId=net.sourceforge.xtn5250 -DartifactId=xtn5250 -Dversion=1.19m -Dpackaging=jar
    ```
- [dm3270 emulator](http://dmolony.github.io/) installed in local maven repository:
  - If not already installed, run 
    ```
    mvn com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:wget -Ddownload.url=https://github.com/dmolony/dm3270/releases/download/v0.5-beta-37/dm3270.jar
    mvn install:install-file -Dfile=target/dm3270.jar -DgroupId=com.bytezone.dm3270 -DartifactId=dm3270 -Dversion=0.5-beta-37 -Dpackaging=jar
    ```

### Build

To build the plugin and run all tests just run `mvn clean verify`. Since test suite include user interface tests, and such tests use UI with actual mouse interactions, avoid moving the mouse while running them.
Another option to avoid mouse interactions to affect tests is to build docker image and use it to run the build: `docker build -t jmeter-plugins-build jmeter-plugins-build && docker run --rm -v $(pwd):/src -v ~/.m2:/root/.m2 jmeter-plugins-build bash -c 'cd /src && /execute-on-vnc.sh mvn --batch-mode -Dmaven.repo.local=/root/.m2/repository clean verify'` for unix systems (some changes on the command would be required for Windows OS). 

### Installation

To use the plugin, install it (by copying the jar from `target` folder) in `lib/ext/` folder of the JMeter installation.

Run JMeter and check the new config and sampler elements available.