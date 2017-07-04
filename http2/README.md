# HTTP2Plugin
HTTP 2 for JMeter 

Step to use HTTP2plugin

1- 	download alpn-boot acording to your jvm version
		https://www.eclipse.org/jetty/documentation/9.4.x/alpn-chapter.html
	
2- On Windows at the start of jmeter.bat add the next line:
		set JVM_ARGS= -Xbootclasspath/p:<path.to.jar>;

	On Linux and Mac at the start of jmeter.sh add the next line:
		JVM_ARGS="-Xbootclasspath/p:<path.to.jar>"	

3- Execute Jmeter with jmeter.bat or jmeter.sh
