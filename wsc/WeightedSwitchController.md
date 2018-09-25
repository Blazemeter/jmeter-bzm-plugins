# Weighted Switch Controller

This controller takes the information about child elements and offers managing relative weights for them. Relative weight means how frequently the child element will be executed during thread iterations. Please note that each thread has independent counter of iterations. 

Relative weights mapped to child names and it save values even if the order of the child has changed. In the case of adding a new child or rename existing child will be set the default value of the relative weight for this child. If the child element is disabled, the relative weight of this element will not be counted in the test.

* `Random Choice` - on each iteration plugin calculate weights and if this checkbox selected and was found more than 1 child with the biggest weight then plugin will execute  random of them (if false - will execute the first child with the biggest value)

![](WeightedSwitchController.png)