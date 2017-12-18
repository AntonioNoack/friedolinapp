package me.noack.antonio.friedomobile.map;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by antonio on 25.11.2017
 */

public class MapView extends GLSurfaceView {

    public Renderer renderer;

    public MapView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public void init(){
        setEGLContextClientVersion(2);// OpenGL ES 2.0

        setRenderer(renderer = new MapRenderer());

        // This setting prevents the GLSurfaceView frame from being redrawn until you call requestRender(), which is more efficient for this sample app.
        // setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
