package it.unipi.mrcv.data_structures;

public class SkipElem {
    /* Maximum docID of the block */
    private int docID;

    /* Offset where start postings */
    private long offset_docId;

    /* Block length containing docIDs */
    private int block_docId_len;

    /* Offset where start frequencies */
    private long offset_freq;

    /* Block length containing frequencies */
    private int block_freq_len;

    
}
