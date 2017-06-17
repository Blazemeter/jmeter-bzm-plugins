# XMPP Sampler
![](xmpp.png)

XMPP Samlper uses [XMPP Connection](XMPPConnection.md) to obtain connection context, then it performs one of available XMPP actions. Action is selected in the UI from the list of available actions. To use the plugin properly, you need to have at least basic knowledge of [XMPP protocol](http://xmpp.org/xmpp-protocols/) and its client-server interaction.

![](xmpp-sampler.png)

## Actions

  * Connect - every XMPP session must start with connecting to server
  * Log In - log into service with username/password/resource 
  * Roster Actions - allows getting/adding/deleting items from roster
  * Send Presence - send available/away status
  * Send Message - send messages to individual and group recipients
  * Send Raw XML - send raw XML packet to server
  * Collect Incoming Packets - very important action, collects all asynchronous incoming packets from connection input queue
  * Disconnect - finish XMPP session with this action
  * Get Bookmarked Conferences ([XEP-0048](http://xmpp.org/extensions/xep-0048.html))
  * Join Multi-User Chat ([XEP-0045](http://xmpp.org/extensions/xep-0045.html))
  * Send File ([XEP-0096](http://xmpp.org/extensions/xep-0096.html))
  * Service Discovery ([XEP-0030](http://xmpp.org/extensions/xep-0030.html))

## Resource Consumption Notice

This plugin uses [Smack API](http://www.igniterealtime.org/projects/smack/) as underlying library. This library uses approach when every client can create two more threads for its functioning (sending and receiving). Users should keep that in mind and monitor factual count of the threads for JMeter process. 

## Extensibility 
![](xzibit.jpg)

Yeah, just like that. You can implement your own action and put JAR with it under lib/ext, so XMPP sampler will detect it and display in the list of available actions in XMPP Sampler.

You'll need to inherit from class `com.blazemeter.jmeter.xmpp.actions.AbstractXMPPAction`  and implement all required methods. There are plenty of examples in existing actions' source code.