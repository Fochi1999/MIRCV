package it.unipi.mrcv.index;

import it.unipi.mrcv.data_structures.DictionaryElem;
import it.unipi.mrcv.global.Global;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static it.unipi.mrcv.index.SPIMI.decodeTerm;

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
        int step=DictionaryElem.size();
        int firstPos=0;
        int currentPos;
        int previousPos;
        int lastPos;
        ByteBuffer readBuffer=ByteBuffer.allocate(step);
        DictionaryElem readElem=new DictionaryElem();
        String res;
        try (FileChannel vocFchan = (FileChannel) Files.newByteChannel(Paths.get(path),
                StandardOpenOption.READ)) {
            lastPos= (int) (vocFchan.size()/step);
            currentPos=(firstPos+lastPos)/2;
            do {
                readBuffer.clear();
                previousPos=currentPos;
                readEntryDictionary(readBuffer, vocFchan, currentPos * step, readElem);

                if(readElem.getTerm().compareTo(term)>0){
                    lastPos=currentPos;
                }
                else{
                    firstPos=currentPos;
                }
                currentPos=(firstPos+lastPos)/2;
                if(currentPos==previousPos){
                    System.out.println("word doesn't exists in vocabulary");
                    readElem=new DictionaryElem(null);
                    break;
                }

            }while((!readElem.getTerm().equals(term)));

        }catch(Exception e){

        }
        return readElem;
    }

    public static void readEntryDictionary(ByteBuffer reader,FileChannel vocFchan,long offset,DictionaryElem ret) throws IOException {
        vocFchan.read(reader,offset);
        reader.flip();
        int termSize = 40;
        byte[] termBytes = new byte[termSize];
        reader.get(termBytes);
        ret.setTerm(decodeTerm(termBytes));
        ret.setDf(reader.getInt());
        ret.setCf(reader.getInt());
        ret.setOffsetDoc(reader.getLong());
        ret.setOffsetFreq(reader.getLong());
        ret.setLengthDocIds(reader.getInt());
        ret.setLengthFreq(reader.getInt());
        ret.setMaxTF(reader.getInt());
        ret.setOffsetSkip(reader.getLong());
        ret.setSkipLen(reader.getInt());
        ret.setIdf(reader.getDouble());
        ret.setMaxTFIDF(reader.getDouble());
        ret.setMaxBM25(reader.getDouble());

    }
}
