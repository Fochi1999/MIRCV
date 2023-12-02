package it.unipi.mrcv.data_structures;

import it.unipi.mrcv.data_structures.termBlock;

// comparator for the termBlock class used in the priority queue for the merger
public class ComparatorTerm  implements java.util.Comparator<termBlock> {
    @Override
    public int compare(termBlock o1, termBlock o2) {
        if (o1.getTerm().compareTo(o2.getTerm()) == 0) {
            if(o1.getNumBlock()<o2.getNumBlock())
                return -1;
            else
                return 1;
        } else {
            return o1.getTerm().compareTo(o2.getTerm());
        }
    }
}
