package me.noack.antonio.friedomobile.struct;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by antonio on 23.11.2017
 */

public class LiveList {

    private final InputStreamReader in;
    private final ArrayList<char[]> content = new ArrayList<>(16);
    private int len = 0;
    private boolean finished = false;
    private final int chunkSize, cslg2, csm1;

    public LiveList(InputStream in, final int log2ChunkSize){
        this.in = new InputStreamReader(in);
        this.chunkSize = 1 << (cslg2 = log2ChunkSize);
        this.csm1 = chunkSize-1;
    }

    public int get(int index) throws IOException {
        if(index < len){
            return content.get(index >> cslg2)[index & csm1];
        } else if(finished){
            return -1;
        } else {
            // lese noch einen Chunk und probiere es nochmal :)
            thug:while(index >= len){
                char[] c = new char[chunkSize];
                content.add(c);
                for(int i=0;i<chunkSize;i++){
                    int r = in.read();// muss einzeln gelesen werden, da die Lesefunktion mit dem Array nicht zuverlässig ist
                    if(r < 0){
                        finished = true;
                        break thug;
                    } else {
                        c[i] = (char) r;
                        len++;
                    }
                }
            }

            if(index < len){
                return content.get(index >> cslg2)[index & csm1];
            } else {
                return -1;
            }
        }
    }

    public String get(int start, int len) throws IOException {
        char[] data = new char[len];
        for(int i=0;i<len;i++){
            data[i] = (char) get(start+i);
        } return new String(data);
    }
}
