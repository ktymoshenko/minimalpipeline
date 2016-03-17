# Minimal Structural Reranking Pipeline

This is a stripped down version of the reranking pipeline (no question classification/focus identification).

If you want to know more about the pipeline used in our papers you can start with:
* [Building	Structures from Classifiers for Passage Reranking, paper](http://www.qcri.com/app/media/2060)
* [Building	Structures from Classifiers for Passage Reranking, slides](http://disi.unitn.it/~severyn/papers/cikm-2013-slides.pdf)

With this software you will be able to apply structural reranking on a question answering dataset.  

To be more specific, the structures generated by the pipeline are enriched with relational information whenever the tree of the question and the tree of a candidate passage share the same lemmas at leaf level.

## References to documentation and tutorials

* https://uima.apache.org/documentation.html  
* https://uima.apache.org/uimafit.html  
* https://dkpro-tutorials.googlecode.com/svn/de.tudarmstadt.ukp.dkpro.tutorial/tags/dkprointro20130529/dkproIntroCore.pdf  

## Prerequisites

### UIMA

Go to https://uima.apache.org/downloads.cgi and download the latest binary distribution of UIMA.

Please download the latest UIMA Java framework & SDK artifact. The zip and tar.gz archives contain the same files. Do not download UIMA DUCC, which is a version of UIMA designed to be run on multiple machines.

Decompress the archive and set the $UIMA_HOME environmental variable to the
UIMA distribution directory. If you use Linux or Mac please set the variable with:

```
export UIMA_HOME="path/to/dir/of/uima"
```

Add the line to  ~/.bashrc or ~/.bash_profile in order to automatically set the variable in the future.

If you use Windows please refer to: http://www.computerhope.com/issues/ch000549.htm

### Maven

You will need Maven for building this project. Install maven for your operating system (e.g. sudo
apt-get install maven).  

## Installation

Clone the minimal pipeline from the repository with:

```
git clone https://github.com/mnicosia/minimalpipeline.git
```


Go to the project main directory and type:

```
mvn compile
```

Then

```
mvn clean dependency:copy-dependencies package
```

## Running the pipeline

```
./run.sh
```

Look into the run.sh script and the arguments/trec-en-pipeline-arguments.txt file to understand what is happening.  

The first run will take time since Maven is downloading JARs and model files.  

## Running an experiment

### Compiling SVMLightTK

Go to the tools/SVM-Light-1.5-rer/ folder and type:

```
  make clean  
  make
```

### The experiment
The experiment consists into reranking candidate answer passages related to questions from the TREC dataset.
This dataset contains 824 questions and their answer patterns. The candidate answer passages come from the AQUAINT corpus. The latter was indexed and queried using the question terms in order to retrieve a list of related passages.

`data/trec-en/questions.txt` contains the questions

`data/trec-en/terrier.BM25b0.75_0` contains the candidate answer passages

### The pipeline

The `TrecPipelineRunner` program is used to execute the pipeline which analyzes questions and passages and produces training and test data for the reranking task.

The program arguments are:

`-argumentsFilePath arguments/trec-en-pipeline-arguments.txt`

This file contains all the actual arguments of the program.

```
-trainQuestionsPath data/trec-en/questions.txt  

-trainCandidatesPath data/trec-en/terrier.BM25b0.75_0  

-trainCasesDir CASes/trec-en/  

-trainOutputDir data/trec-en/train/  

-testQuestionsPath data/trec-en/questions.txt  

-testCandidatesPath data/trec-en/terrier.BM25b0.75_0  

-testCasesDir CASes/trec-en/  

-testOutputDir data/trec-en/test/  

-candidatesToKeepInTrain 10  

-candidatesToKeepInTest 50  

-lang en  
```

The same question/answer files are passed in both the training and testing phases since we will generate the training and test data in a cross validation fashion.

The Java virtual machine arguments are the following:

`-Djava.util.logging.config.file="resources/logging.properties"`

This argument sets the logging properties.

After the execution of the pipeline, the `data/trec-en/` folder will contain the `train` and `test` folders with the data required for the experiment.

### Settings

The candidates kept for training are 10 while the candidates kept for testing are 50.

Out of 824 questions only 416 are used to generate training examples because the others do not have valid candidate passages in the top-50 candidates list.

### Producing the folds

Run:

```
python scripts/folds.py data/trec-en/ 5
```

in order to take the training and testing examples produced by the pipeline and split them into five folds. The script assumes that the train and test data are in the specified directory.

### Learning, reranking and evaluation

The command:

```
python scripts/svm_run_cv.py --params="-t 5 -F 3 -C + -W R -V R -m 400" --ncpus 2 data/trec-en/folds/
```

will launch the learning, the reranking and the evaluation, which will be carried out on the single folds. The metrics averaged on all folds will be printed on the screen. The `--ncpus` parameter can be used to parallelize the jobs.
 
## Examples and tutorial

You can find examples about the framework in the following package:

```
src/test/java/qa/qcri/qf/tutorial
```

Please go through the provided code and comments in order to have a better idea of the framework.

## Development in Eclipse

### Maven

If you use Eclipse please install the M2E plugin. Go to Help > Install new software... and search
for m2e.  

### UIMA tooling for Eclipse

UIMA tooling simplifies the development of typesystems and annotators in Eclipse. Install Eclipse EMF
following these instructions: http://tinyurl.com/UIMA4ECLIPSE. The useful UIMA visual
tools (UIMA tooling) can be found at this software update address:
http://www.apache.org/dist/uima/eclipse-update-site/.

### Importing the project into Eclipse

Import the project into Eclipse by clicking on File -> Import -> Existing Maven Projects. Select the pipeline folder and click on Finish.
