Toy example:

1) Import the GraphEditDistance/ folder as a Java project in Eclipse
- Java 1.6 is needed

2) Run src/algorithms/GraphMatching.java as a Java Application with parameter "properties/letters-hed.prop"
- that is, under "Run Configurations..." enter "properties/letters-hed.prop" into the field "Program arguments:"

3) Compare the results "results/letters-hed.ged" and "results/letters-hed.info" with the files in "results-andreas/"
- they should be the same except the timestamp and the runtime in the info file

4) Modify "properties/letters-hed.prop" by setting "oneMatch=true" and run the application again
- only one match between the graphs with index 0 and 1 will be performed

5) Compare the results "results/letters-hed_0_1.edt" with the file in "results-andreas/"
- they should be the same

6) Study "properties/fingerprint.prop" and the website "http://www.fhnw.ch/wirtschaft/iwi/gmt"
- they provide detailed information about the graph format and the parameters in the properties file
