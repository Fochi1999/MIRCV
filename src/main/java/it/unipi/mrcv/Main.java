package it.unipi.mrcv;

import it.unipi.mrcv.compression.Unary;
import it.unipi.mrcv.compression.VariableByte;
import it.unipi.mrcv.index.Merger;
import it.unipi.mrcv.index.SPIMI;

import java.io.IOException;
import java.util.ArrayList;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        SPIMI.exeSPIMI("reduced collection.tsv");


        Merger.Merge();
        //SPIMI.readCompressedDic("vocabulary","docids");
        //SPIMI.readIndex("frequencies");

/*
        int nProva=3;
        ArrayList<Integer> ListProva=new ArrayList<>();
        ArrayList<Integer> result;
        byte[] buffer = new byte[1024];
        for(int i=0;i<200;i++){
            ListProva.add(i);
        }
        buffer=Unary.ArrayIntToUnary(ListProva);
        result=Unary.unaryToArrayInt(buffer);

        for(int x:result){
            System.out.println(x);
        }
*/
/*
        int nProva=3;
        ArrayList<Integer> ListProva=new ArrayList<>();
        ArrayList<Integer> result=new ArrayList<>();
        byte[] buffer = new byte[1024];
        for(int i=0;i<200;i++){
            ListProva.add(i);
            buffer=Unary.intToUnary(i);
            result.add(Unary.unaryToInt(buffer));
        }


        for(int x:result){
            System.out.println(x);
        }*/
    /*    long nProva=76;
        ArrayList<Long> listLongP=new ArrayList<>();
        ArrayList<Long> res=new ArrayList<>();
        byte[] buffer;
        for(long i=0;i<4000000;i++){
            listLongP.add((long)i);
            buffer= VariableByte.fromLongToVarByte(i);
            System.out.println(i+": ");
            for(byte b:buffer){
                System.out.print(b+" ");
            }
            System.out.println("");
            res.add(VariableByte.fromVarByteToLong(buffer));
            System.out.println(VariableByte.fromVarByteToLong(buffer));
            if(VariableByte.fromVarByteToLong(buffer)!=i){
                System.out.println("FALSE");
            }
        }
*/
    }
}

