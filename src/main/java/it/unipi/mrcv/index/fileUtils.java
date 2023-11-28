package it.unipi.mrcv.index;

import it.unipi.mrcv.data_structures.DictionaryElem;
import it.unipi.mrcv.global.Global;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
    public static DictionaryElem binarySearchOnFile(String path,String term){
        DictionaryElem ret=new DictionaryElem();
        long firstPos=0;
        long currentPos;
        long lastPos;
        String res;
        int entrySize=DictionaryElem.size();
        try (FileChannel vocFchan = (FileChannel) Files.newByteChannel(Paths.get(path),
                StandardOpenOption.READ)) {
                lastPos=vocFchan.size();
                currentPos=lastPos/2;
        }catch(Exception e){

        }
        return ret;
    }
}
