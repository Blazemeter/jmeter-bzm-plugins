# Rotating JTL Listener

Rotating JTL Listener is a Listener that allows writing sequence of JTL files, limited by number of samples per file.

This enables recorded file to be handled by some background cleanup process, 
to be archived/deleted for saving disk space during soak tests and long duration/hight throughput tests.

File names form sequence like `result.jtl => result.1.jtl => result.2.jtl`.

![](rotatingListener.png)
