package it.unipi.mrcv.index;

import it.unipi.mrcv.global.Global;

import java.io.File;
import java.io.FilenameFilter;

public class fileUtils {

    public static void deleteTempFiles(){
        final File folder =new File("./");
        final File[] files = folder.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( final File dir,
                                   final String name ) {
                String regexString="^("+ Global.prefixDocFiles+"|"+Global.prefixVocFiles+"|"+Global.prefixFreqFiles+").*";
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
                String regexString="^("+Global.finalFreq+"|"+Global.finalDoc+"|"+Global.finalVoc+").*";
                return name.matches( regexString);
            }
        } );
        for ( final File file : files ) {
            if ( !file.delete() ) {
                System.err.println( "Can't remove " + file.getAbsolutePath() );
            }
        }
    }
    public static void deleteFilesCompressed(){
        final File folder =new File("./");
        final File[] files = folder.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( final File dir,
                                   final String name ) {
                String regexString="^("+Global.finalFreqCompressed+"|"+Global.finalDocCompressed+"|"+Global.finalVocCompressed+").*";
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
