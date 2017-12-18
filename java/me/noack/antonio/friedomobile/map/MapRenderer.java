package me.noack.antonio.friedomobile.map;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
// import static javax.microedition.khronos.opengles.GL10.*;

/**
 * Created by antonio on 25.11.2017
 *
 * soll die Karte in 2D/3D rendern :D
 */

public class MapRenderer implements GLSurfaceView.Renderer {

    // automatisches Lageplanumwandeln durch Punkt- und Kantensuche? Wäre nett, sehr nett :)

    public float[] generateHouse(Gebäude gebäude){



        return null;
    }

    int[] buffers = new int[1];
    int mainProgram, buf, MVPM;
    @Override public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        glClearColor(1f, 201f/255f, 0f, 1f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDepthFunc(GL_LESS);

        mainProgram = Program.compile(
                "uniform mat4 uMVPMatrix;\n"+
                "attribute vec2 vPosition;\n"+
                "void main(){gl_Position = uMVPMatrix * vec4(vPosition, .1, 1.);\n}",

                "precision mediump float;\n" +
                "void main(){gl_FragColor = vec4(0.,0.,1.,1.);}");

        glUseProgram(mainProgram);

        float[] floats = new float[]{
                0, 0,
                1, 0,
                .9f, .5f,
                .5f, .9f,
                0, 1,
                0, 0
        };

        buf = createBuffer();
        glBindBuffer(GL_ARRAY_BUFFER, buf);
        glBufferData(GL_ARRAY_BUFFER, floats.length * 4, floatBuffer(floats), GL_STATIC_DRAW);
        // bind(buf, new int[]{2}, mainProgram);
        // glDrawArrays(GL_TRIANGLES, 0, floats.length/2);

        /*glGenBuffers(1, buffers, 0);
        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);

        Buffer buf = ByteBuffer.allocateDirect(floats.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(floats).position(0);

        glBufferData(GL_ARRAY_BUFFER, buf.capacity(), buf, GL_STATIC_DRAW);

        pos = glGetAttribLocation(mainProgram, "vPosition");
        glEnableVertexAttribArray(pos);
        glVertexAttribPointer(pos, 2, GL_FLOAT, false, 2*4, 0);

        int mainMatrix = glGetUniformLocation(mainProgram, "uMVPMatrix");


        //Matrix.setLookAtM(data, 0, 0, 3, 0, 0, 0, 0, 1, 0, 0);
        glUniformMatrix4fv(mainMatrix, 1, false, data, 0);*/
        MVPM = glGetUniformLocation(mainProgram, "uMVPMatrix");
        float[] data = new float[]{1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
        glUniformMatrix4fv(MVPM, 1, false, (FloatBuffer) floatBuffer(data));

    }

    Buffer floatBuffer(float[] floats){
        return ByteBuffer.allocateDirect(floats.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(floats).position(0);
    }

    void bind(int buffer, int[] indices, int program){
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        int ges = 0;
        for(int i:indices) ges+=i;
        ges*=4;
        int ind = 0;
        for(int i=0;i<indices.length;i++){
            glVertexAttribPointer(i, indices[i], GL_FLOAT, false, ges, 4*ind);
            glEnableVertexAttribArray(i);
            ind+=indices[i];
        }
    }

    int createBuffer(){
        int[] buffer = new int[1];
        glGenBuffers(1, buffer, 0);
        return buffer[0];
    }

    int pos;

    @Override public void onDrawFrame(GL10 gl) {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        bind(buf, new int[]{2}, mainProgram);
        glDrawArrays(GL_TRIANGLES, 0, 6);



        /*glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_BLEND);

        glUseProgram(mainProgram);
        glEnableVertexAttribArray(pos);
        glVertexAttribPointer(pos, 2, GL_FLOAT, false, 2*4, 0);
        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glDisableVertexAttribArray(pos);*/
        //gl.glMatrixMode(GL_MODELVIEW);
        //gl.glLoadIdentity();

        //GLU.gluLookAt(gl, 0, 0, -5, 0, 0, 0, 0, 1, 0);



    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {

        /*float ratio = (float) width / height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);

        */

    }
}
