# MIRCV
Multimedia information retrivial and computer vision project
## Overview
This project revolves around the field of information retrieval, aiming to build a search engine that enables users to express their information requirements through queries. The system then generates a list of documents deemed most pertinent, using a variety of metrics to calculate relevance scores. The project consists of three primary components:
* Index construction
* Query processing
* Evaluation
## Index Construction
The first step in the process is to construct an index of the documents in the collection. This is done by first parsing the documents, then creating an inverted index of the terms in the collection. The inverted index is a mapping from terms to the documents that contain them. The index is then written to disk for later use.
In Order to do so, the user must first download and add the following files to the project:
* A dataset of documents to be indexed named "collection.tar.gz"
* optional: A list of stopwords named "stopwords-en.txt"

The user can then run the following command to build the index:
``` Main.java -i```

The following flags are optional and can be used to change the default behaviour:
* -c: compression (default: true, false is used usually only for debugging, suggested to keep true)
* -s: stemming (default: true)
* -sw: stopwords (default: true, a list of stopwords must be provided)

After the index is built, the following files will be created:
* vocabularyCompressed
* skipping
* frequenciesCompressed
* docIdsCompressed
* collectionInfo.txt

All of these files are required for the query processing and evaluation steps.
## Query Processing
The second step is to process the queries.
The user can run the following command to process the queries:
``` Main.java ```

All the flags used are the same as the ones used for index construction and are retrieved from the collectionInfo file.

The user has to choose several options:
* Score function:
  * TF-IDF
  * BM25
* Query mode:
    * DAAT
    * MaxScore
    * Conjunctive
* number of results to return: (default: 10)
* query: (default: "")
