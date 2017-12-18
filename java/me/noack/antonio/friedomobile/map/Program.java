package me.noack.antonio.friedomobile.map;

import static android.opengl.GLES20.*;

/**
 * Created by antonio on 25.11.2017
 */

public class Program {
    public static int compile(String vertex, String fragment){
        int i = glCreateProgram();
        int v = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(v, vertex);
        glCompileShader(v);
        glAttachShader(i, v);
        String s = glGetShaderInfoLog(v);
        if(s != null) System.out.println(s);
        int f = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(f, fragment);
        glCompileShader(f);
        glAttachShader(i, f);
        s = glGetShaderInfoLog(f);
        if(s != null) System.out.println(s);
        glLinkProgram(i);
        return i;
    }
}
