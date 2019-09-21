[![Build Status](https://travis-ci.org/andreas-solti/eigen-measure.svg?branch=travis-integration)](https://travis-ci.org/andreas-solti/eigen-measure)

# eigen-measure
Package for computing the largest eigenvalue based measure in the context of process mining and regular languages.
Description of the technique is provided in the paper [Behavioural Quotients for Precision and Recall in Process Mining](https://minerva-access.unimelb.edu.au/handle/11343/208876?show=full).

## Installation

### Dependencies
You'll need to install [`lpsolve55`](http://lpsolve.sourceforge.net/5.5/) and [`gradle`](https://gradle.org/).

For example in ubuntu run in terminal:<br/>
```$ sudo apt-get install lp-solve gradle```

### Installation in Linux
Within Linux-like systems, you need to:

1. first get the sources:<br/>
```git clone https://github.com/andreas-solti/eigen-measure.git```
2. Run the shell script to download the dependencies (due to licensing, I did not package them automatically)<br/>
```$ ./download_dependencies.sh```

### Installation in Windows:
Install Ubuntu for Windows and follow steps above
from within the virual linux environment.

## Running
You can verify, if all works as expected by running:<br/>
```gradle test```

## Check out the sources in your favorite IDE
Last, explore the sources (src) in your favorite editor (e.g. IntelliJ, VS-Code, or Eclipse).
There are some tests that show the functionality. Unit tests can also be run from inside those editors.

## Compute eigenvalue-based Precision
The class to use for computing precision and recall is
`org.processmining.eigenvalue.automata.PrecisionRecallComputer`

It uses an AcceptingPetriNet as input and an Event log with the classifier to compute the PrecisionAndRecall.






