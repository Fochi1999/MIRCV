package it.unipi.mrcv.index;

import java.io.File;
import java.io.FilenameFilter;

public class fileUtils {
    public static final String prefixDocFiles="doc_";
    public static final String prefixVocFiles="voc_";
    public static final String prefixFreqFiles="freq_";
    public static final String finalVoc="vocabulary";
    public static final String finalDoc="docIds";
    public static final String finalFreq="frequencies";
    public static void deleteTempFiles(){
        final File folder =new File("./");
        final File[] files = folder.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( final File dir,
                                   final String name ) {
                String regexString="^("+prefixDocFiles+"|"+prefixVocFiles+"|"+prefixFreqFiles+").*";
                return name.matches( regexString);
            }
        } );
        for ( final File file : files ) {
            if ( !file.delete() ) {
                System.err.println( "Can't remove " + file.getAbsolutePath() );
            }
        }
    }
    public static void deleteFiles(){
        final File folder =new File("./");
        final File[] files = folder.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( final File dir,
                                   final String name ) {
                String regexString="^("+finalFreq+"|"+finalDoc+"|"+finalVoc+").*";
                return name.matches( regexString);
            }
        } );
        for ( final File file : files ) {
            if ( !file.delete() ) {
                System.err.println( "Can't remove " + file.getAbsolutePath() );
            }
        }
    }
}
