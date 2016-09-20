package enterprises.stimes.whackyballs;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Jacob on 5/21/2016.
 * Currently takes care of the majority of the game context, ball movement & planning, as well as rendering
 * As the game gets larger, the rendering and game code will be split up
 */
public class GLRenderer implements GLSurfaceView.Renderer {

    private final float BRAKE_STOP_THRESHOLD = .00000025f;
    private final float BRAKE_REDUCTION_PROPORTION = .05f;

    private final float AI_SPEED = .001f;

    private Vec3 originalCameraPos = new Vec3(0f, 3f, 3f);
    private Vec3 cameraPos = originalCameraPos;
    private Vec3 cameraTarget = new Vec3(0f, 0f, 0f);
    private Vec3 cameraRight = new Vec3(1f, 0f, 0f);
    private Vec3 cameraUp = new Vec3(0f, 1f, 0f);

    private Shader mShader;

    private Ball mBall;
    private float ballRadius = .25f;
    private boolean brake = false;

    private int numOtherBalls = 5;
    private ArrayList<Ball> allBalls = new ArrayList<Ball>();

    private Arena mArena;
    private float arenaSize = 2.75f;
    private float arenaDepth = -ballRadius;

    private OrientationSensor orientation;

    private final float[] VPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private Context mContext;

    public GLRenderer(MainActivity activity){
        //Doing this because OS needs a context,
        // so initializing OS in mainactivity
        orientation = activity.orientationSensor;
        mContext = (Context) activity;
    }

    private void genRandomBalls(int num){
        float max = arenaSize * 2.0f;
        Random rand =  new Random();

        for(int i=0; i<num; i++){
            Ball ball = new Ball(ballRadius);
            do {
                float randX = rand.nextFloat();
                float randY = rand.nextFloat();

                ball.setPosition(new Vec3(randX * max - arenaSize, 0.0f, randY * max - arenaSize));
            }
            while(checkCollisions(ball));

            ball.SetColor(new Vec3(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));

            allBalls.add(ball);
        }

    }

    private boolean checkCollisions(Ball b){
        for(Ball other : allBalls){
            if(b != other && checkCollision(b, other)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCollision(Ball a, Ball b){
        return b.getPosition().sub(a.getPosition()).magnitude() < 2.0f * ballRadius;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mArena = new Arena(mContext, arenaSize, arenaDepth);
        mBall = new Ball(ballRadius);

        allBalls.add(mBall);
        genRandomBalls(numOtherBalls);

        mShader = new Shader();
    }

    public void tap(float x, float y){
        brake = true;
    }

    public void release(){
        brake = false;
    }

    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        update();

        draw();
    }

    private void update(){

        //Update each ball's position based on current position & velocity
        for(int i=0; i<allBalls.size(); i++) {
            Ball ball = allBalls.get(i);

            if(!isBallOnArena(ball)){
                ball.updateVelocity(new Vec3(0.0f, -.05f, 0.0f));
            }

            ball.updatePos();

            //Check if lost:
            if(ball.getPosition().y < arenaDepth - ballRadius*3.0f){
                updateBallLost(ball);
            }

        }

        //Check for and handle collisions:
        updateCollisions();

        //Update player's ball ('s velocity)  based on any user inputs:
        updatePlayerBall();

        updateOtherBalls();

        updateCamera();
    }

    private void updateBallLost(Ball ball){
        if(ball == mBall) {
            mBall.reset();
            Log.i("Game", "Player lost");
            //Toast.makeText(mContext.getApplicationContext(), "You lost", Toast.LENGTH_SHORT).show();
        }
        else {
            allBalls.remove(ball);
        }

        if(allBalls.size() == 1){
            //game over

            if(allBalls.contains(mBall)){
                //player won
                Log.i("Game", "Player won");
                //Toast.makeText(mContext, "You win", Toast.LENGTH_SHORT).show();
            }
            else {
                //player lost
            }
        }
    }

    private void updateCollisions(){
        for(int i=0; i<allBalls.size()-1; i++){
            Ball ball = allBalls.get(i);

            for(int j=i+1; j<allBalls.size(); j++){
                Ball otherBall = allBalls.get(j);
                if(checkCollision(ball, otherBall)){
                    Vec3 n = ball.getPosition().sub(otherBall.getPosition());
                    n = n.scalar(1.0f / n.magnitude());

                    float a1 = ball.velocity.dot(n);
                    float a2 = otherBall.velocity.dot(n);

                    float p = a1 - a2;

                    Vec3 v1 = ball.velocity.sub(n.scalar(p));

                    Vec3 v2 = otherBall.velocity.add(n.scalar(p));

                    ball.velocity = v1;

                    otherBall.velocity = v2;
                }
            }
        }
    }

    private void updatePlayerBall(){
        if(isBallOnArena(mBall)){

            if(brake){
                mBall.updateVelocity(mBall.velocity.scalar(-BRAKE_REDUCTION_PROPORTION));

                if(mBall.velocity.magnitude() < BRAKE_STOP_THRESHOLD){

                    mBall.velocity = new Vec3(0.0f);
                }
            }
            else {
                float sensorScaling = .001f;
                Vec3 update = new Vec3(-orientation.getLeftRightAngle() * sensorScaling, 0.0f, orientation.getForwardBackwardAngle() * sensorScaling);
                mBall.updateVelocity(update);

            }
        }
    }

    private void updateOtherBalls(){
        for(int i=1; i<allBalls.size(); i++){
            Ball cur = allBalls.get(i);
            Vec3 bestDir = mBall.getPosition().sub(cur.getPosition());

            float bestDist = Vec3.squaredDistance(cur.getPosition(), mBall.getPosition());
            for(int j=1; j<allBalls.size(); j++){
                float dist = Vec3.squaredDistance(cur.getPosition(), allBalls.get(j).getPosition());
                if(i != j && dist < bestDist){
                    bestDir = allBalls.get(j).getPosition().sub(cur.getPosition());
                    bestDist = dist;
                }
            }

            //normalize
            bestDir = bestDir.scalar(1.0f / bestDir.magnitude());

            if(bestDir.dot(cur.velocity) >=0){

            }

            cur.updateVelocity(bestDir.scalar(AI_SPEED));
        }
    }

    //Camera is to stay fixed on player's ball:
    private void updateCamera(){
        cameraTarget = mBall.getPosition();
        cameraPos = originalCameraPos.add(cameraTarget);

        // Set the camera position (View matrix)
        cameraUp = cameraRight.cross(cameraTarget.sub(cameraPos));

        Matrix.setLookAtM(mViewMatrix, 0, cameraPos.x, cameraPos.y, cameraPos.z, cameraTarget.x, cameraTarget.y, cameraTarget.z, cameraUp.x, cameraUp.y, cameraUp.z);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(VPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    private void draw(){
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mArena.draw(mShader, VPMatrix);

        for(Ball b : allBalls){
            b.draw(mShader, VPMatrix);
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public boolean isBallOnArena(Ball ball){
        Vec3 ballPos = ball.getPosition();
        if(Math.abs(ballPos.x) > arenaSize || Math.abs(ballPos.z) > arenaSize){
            return false;
        }
        return true;
    }
}
