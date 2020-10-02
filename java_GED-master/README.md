# Character Recognition in Ancient Greek Papyrus #
Gwenael Gendre


## Single run ##
- Run src/main/java/algorithms/GraphMatchingSegFree.java as a Java Application with parameter 
"test/papyrus/properties/gwenael0.prop"
- In the properties file, adapt:
    - the graphs path: the sourcePath folder must contain the .gxl files listed in the sourceFile .cxl file, same with 
    targetPath and targetFile 
    - the images path
    - the groundtruth file path
    - the bounding boxes groundtruth path
    - the destination folder for  the hotmap visualisation
- All the files are in the files/ folder  

    
## Multiple runs ## 
- Run src/main/java/gwenael/Test.java as a Java Application. 
- In the file, change the properties folder: the GraphMatchingSegFree.java will be run once with each file in the folder
    as a property file

## Multiple properties creation ## 
- To create multiple .prop files, run the src/main/java/properties/GMPropertyCSVSimple.java file as a Java Application
- The files test/papyrus/settings/Parameters.csv and test/papyrus/settings/Graphs.csv contain the desired settings
- The images path folder and the hotmap visualisation folder paths should also be updated in the file

## trec_eval for different OS ##

- To use trec_eval on Windows, please follow the instructions on the [trec_eval github page](https://github.com/usnistgov/trec_eval): 
- Here are the steps I followed:  
    - install Cygwin with the correct packages
    - delete the trec_eval file
    - use the make utility inside Cygwin
    - copy cygwin1.dll to /treceval folder
    - change the treceval property in files to trec_eval.exe
    - change the executeCommand function in /src/main/java/util/treceval/TrecEval to a Powershell-version
- (To use on another system, this should be enough: 
    - change the treceval property in files to trec_eval
    - change the executeCommand function in /src/main/java/util/treceval/TrecEval to the /bin/sh version )
    

---------------------------------------------------------------------------
Michael Stauffer
> #Graph Edit Distance
> ##General
> - The project forked from ["http://www.fhnw.ch/wirtschaft/iwi/gmt"](http://www.fhnw.ch/wirtschaft/iwi/gmt)
> - Run src/algorithms/GraphMatching.java as a Java Application with parameter "properties/letters-hed.prop"
> - that is, under "Run Configurations..." enter "properties/letters-hed.prop" into the field "Program arguments:"
>
>
> ## Toy example: ##
> 1. Import the GraphEditDistance/ folder as a Java project in Eclipse
> **Java 1.6 is needed
>
> 2. Run src/algorithms/GraphMatching.java as a Java Application with parameter "properties/letters-hed.prop"
> that is, under "Run Configurations..." enter "properties/letters-hed.prop" into the field "Program arguments:"
>
> 3. Compare the results "results/letters-hed.ged" and "results/letters-hed.info" with the files in "results-andreas/"
> they should be the same except the timestamp and the runtime in the info file
>
> 4. Modify "properties/letters-hed.prop" by setting "oneMatch=true" and run the application again
> only one match between the graphs with index 0 and 1 will be performed
>
> 5. Compare the results "results/letters-hed_0_1.edt" with the file in "results-andreas/"
> they should be the same
>
> 6. Study "properties/fingerprint.prop" and the website "http://www.fhnw.ch/wirtschaft/iwi/gmt"
> they provide detailed information about the graph format and the parameters in the properties file
>
>