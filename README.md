# text-processing

Generates random text, using 
* part-of-speech (POS) patterns, from POS word files and Part Of Speech Database.
* Markov chains for creating words or sentences

## Build instructions
### commonlib
* gradlew build uploadArchives
* gradlew sonarqube (optional, specify build.gradle.sonarqube as the build file)

### text-processing
* gradlew build uploadArchives
* gradlew sonarqube (optional, specify build.gradle.sonarqube as the build file)

## eclipse project setup
* Clone the latest [commonlib](https://github.com/dwbzen/commonlib) and [text-processing](https://github.com/dwbzen/text-processing) repos from Github
    * Recommend cloning in C:\Compile along with commonlib and music-framework projects
* Download and install the version of the JDK referenced in build.gradle (Java 13)
* Download and install latest eclipse Java IDE (2019-09)
* Spin up eclipse and add the JDK for Java 13 under Installed JREs, and make it the default
* Import the text-processing gradle project

