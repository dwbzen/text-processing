# text-processing

Generates random text, using 
* part-of-speech (POS) patterns, from POS word files and Part Of Speech Database.
* Markov chains for creating words or sentences

## Build instructions
### commonlib
* gradlew build uploadArchives

### text-processing
* gradlew build uploadArchives

## eclipse project setup
* Clone the latest [commonlib](https://github.com/dwbzen/commonlib) and [text-processing](https://github.com/dwbzen/text-processing) repos from Github
    * Recommend cloning all github projects to C:\Compile
* Download and install the version of the JDK referenced in build.gradle (Java 14)
* Download and install latest eclipse Java IDE (2020-03)
* Spin up eclipse and add the JDK for Java 14 under Installed JREs, and make it the default
* Import the commonlib gradle project
* Import the text-processing gradle project

# Text Generation Examples
* Random foods with a price:
    * TextGenerationRunner -n 10 -pattern "\`nF\`(  )W{1,3}(.)w{2}"  -format PW
* Mad-lib, also an example using a pattern lambda:
    * TextGenerationRunner -template  madlib.txt -n 2 -format "NC"
* Password generation:
    * TextGenerationRunner -pattern "\[M|F\]\[N|p|B\](-2020)" -n 50 -format PW

# Markov Chain Examples
* Drug names. The -init flag forces picking seeds from the start of the word. -order is the order of the Markov Chain, in this case 2 characters.
    * WordProducerRunner  -file  "$RESOURCE/drugBrandNames.txt"  -order 2 -list -ignoreCase -num 20 -min 5 -format "TC" -stat false -init
    
* Feminine Irish names.
     * WordProducerRunner -file "$RESOURCE/femaleIrishFirstNames.txt" -num 20  -min 4 -order 2 -list -ignoreCase -format TC -init
     
	
