# Random CSV Data Set Config 

Random CSV Data Set Config is used to read CSV rows from file, split and put them into JMeter variables in random order.

This plugin has following options that affect the behavior:
  * `Filename` - path to CSV file;
  * `File encoding` - encoding of this CSV file;
  * `Delimiter` - delimiter that be used to split records in the file;
  * `Variable Names` - list (comma-separated) of variable names;
  * `Random order`  - reading in random order;
  * `Rewind on end of list` - if the flag is selected and an iteration loop has reached the end, the new loop will be started;
  * `First line is CSV header` - select this flag to skip header(used only if `Variable Names` is not empty);
  * `Independent list per thread` - determines that CSV data set will be shared for all threads or each thread will be having own local copy.

_In preview area shows only 20 records from CSV file._

![](randomCSVDataSetConfig.png)
