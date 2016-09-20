package enterprises.stimes.whackyballs;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

///The one and only activity at this time. Basically just sets up the
/// surface view with a renderer. In the future this would be a game menu,
/// and users can choose to play the AI or other people online, and view scores
/// and maybe even purchase in-game content :)
public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLView;
    OrientationSensor orientationSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        orientationSensor = new OrientationSensor(this);

        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyGLSurfaceView extends GLSurfaceView {

        private final GLRenderer mRenderer;

        public MyGLSurfaceView(MainActivity context){
            super(context);

            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);

            mRenderer = new GLRenderer(context);

            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(mRenderer);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            // MotionEvent reports input details from the touch screen
            // and other input controls. In this case, you are only
            // interested in events where the touch position changed.

            float x = e.getX();
            float y = e.getY();

            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    mRenderer.tap(x, y);

                    break;

                case MotionEvent.ACTION_UP:

                    mRenderer.release();

                    break;

                case MotionEvent.BUTTON_BACK:
                    MainActivity.this.finish();
            }

            return true;
        }
    }
}

