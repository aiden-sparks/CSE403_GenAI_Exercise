# CSE403_GenAI_Exercise

## Instructions for Running
1. Clone the repository.
2. Ensure you have the Java JDK installed.
3. Install GraphViz.
   - On Windows, download from here https://graphviz.org/download/, move to Program Files, then add path_to/GraphvizX.XX\bin to your User path and add path_to/GraphvizX.XX\bin\dot.exe to your System path. You may have to restart your machine.
   - On macOS, using Homebrew, run "brew install graphviz".
4. Run javac DMSGBuilder.java
5. Run java DMSGBuilder path_to/matrix.csv to run the DMSG Builder on the matrix .csv of your choice. An example one is provided.
6. The program will output dmsg.dot, which can be viewed as a png by running "dot -Tpng dmsg.dot -o dmsg.png".

## Matrix Format
The mutant-test detection matrix .csv is formatted as follows: The first column is the test number (which is some positive integer). The second column is the mutant number (which is some positive integer) that was killed by that test. The third column is the way in which the mutant was detected (killed), which can be one of three values: "FAIL", "TIME", or "EXC", corresponding to the test failing, the program timing out, or the program raising an exception.
