package enterprises.stimes.whackyballs;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Jacob on 5/21/2016.
 *
 * Model representing the balls to be rendered on screen
 */
public class Ball {

    private static FloatBuffer vertexBuffer;
    static final int COORDS_PER_VERTEX = 6;
    static final int POSITION_DATA_SIZE = 3;
    static final int NORMAL_DATA_SIZE = 3;
    private static final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private static int vertexCount = 0;
    private static boolean initializedBuffer = false;

    private float mRadius;

    private float[] color = new float[] {.6f, .2f, 0.0f, 1.0f};
    private Vec3 position = new Vec3(0.0f, 0.0f, 0.0f);
    public Vec3 velocity = new Vec3();

    //rotation

    public Ball(float radius){

        this.mRadius = radius;

        if(!initializedBuffer){
            ArrayList<Float> vertices = genSphere();

            float[] coords = new float[vertices.size()];
            for(int i=0; i < vertices.size(); i++){
                coords[i] = vertices.get(i);
            }

            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    coords.length * 4);
            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());

            // create a floating point buffer from the ByteBuffer
            vertexBuffer = bb.asFloatBuffer();
            // add the coordinates to the FloatBuffer
            vertexBuffer.put(coords);
            // set the buffer to read the first coordinate
            vertexBuffer.position(0);

            vertexCount = coords.length / COORDS_PER_VERTEX;

            Log.i("Ball", "finished gening points, " + vertexCount);

            initializedBuffer = true;
        }

    }

    public Vec3 getPosition(){
        return this.position;
    }

    public void setPosition(Vec3 pos){
        this.position = pos;
    }

    public void reset(){
        this.position = this.velocity = new Vec3(0.0f);
    }

    private Vec3 spherePoint(float u, float v) {

        Vec3 pt = new Vec3();
        pt.x = (float) (mRadius * Math.cos(u) * Math.cos(v));
        pt.y = (float) (mRadius * Math.cos(u) * Math.sin(v));
        pt.z = (float) (mRadius * Math.sin(u));

        return pt;
    }

    private Vec3 sphereNormal(float u, float v){
        Vec3 n = new Vec3();
        n.x = (float) (Math.cos(u) * Math.cos(v));
        n.y = (float) (Math.cos(u) * Math.sin(v));
        n.z = (float) (Math.sin(u));
        return n;
    }

    private ArrayList<Float> genSphere(){
        ArrayList<Float> mVertices = new ArrayList<Float>();

        float U, dU, V, dV;
        float S, dS, T, dT;
        int X, Y; 	/* for looping */
        float v1x, v1y, v1z, n1x, n1y, n1z;
        float v3x, v3y, v3z, n3x, n3y, n3z;

        int uSegs = 30;
        int vSegs = 30;

        float uEnd = (float) Math.PI / 2.0f;
        float uStart = -uEnd;
        float vEnd = (float) Math.PI;
        float vStart = -vEnd;

		/* Calculate delta variables */
        dU = (float)(Math.PI) / (float) uSegs;
        dV = (float)(2.0f * Math.PI) / (float) vSegs;


		/* Initialize variables for loop */
        U = uStart;

        for (Y = 0; Y < uSegs; Y++) {
			/* Initialize variables for loop */
            V = vStart;

            for (X = 0; X < vSegs; X++) {

                Vec3 v1 = spherePoint(U, V);
                Vec3 v1n = sphereNormal(U, V);

                Vec3 v2 = spherePoint(U + dU, V);
                Vec3 v2n = sphereNormal(U + dU, V);

                Vec3 v3 = spherePoint(U + dU, V + dV);
                Vec3 v3n = sphereNormal(U + dU, V + dV);

                Vec3 v4 = spherePoint(U, V + dV);
                Vec3 v4n = sphereNormal(U, V + dV);

                AddTriangle(mVertices, v1, v2, v4, v1n, v2n, v4n);
                //AddTriangle(mVertices, v1n, v2n, v4n);
                AddTriangle(mVertices, v2, v3, v4, v2n, v3n, v4n);
                //AddTriangle(mVertices, v2n, v3n, v4n);

				/* Update variables for next loop */
                V += dV;
            }

			/* Update variables for next loop */
            U += dU;
        }

        return mVertices;
    }

    public void AddTriangle(ArrayList<Float> vertices, Vec3 a, Vec3 b, Vec3 c, Vec3 aN, Vec3 bN, Vec3 cN){
        AddVec(vertices, a);
        AddVec(vertices, aN);
        AddVec(vertices, b);
        AddVec(vertices, bN);
        AddVec(vertices, c);
        AddVec(vertices, cN);
    }

    public void AddVec(ArrayList<Float> vertices, Vec3 vec){
        vertices.add(vec.x);
        vertices.add(vec.y);
        vertices.add(vec.z);
    }

    public void SetColor(Vec3 color){
        this.color[0] = color.x;
        this.color[1] = color.y;
        this.color[2] = color.z;
    }

    public void updateVelocity(Vec3 update){
        this.velocity = this.velocity.add(update);
        //Log.i("Ball", "New pos: " + position.x + ", " + position.y + ", " + position.z);
    }

    public void updatePos(){
        this.position = this.position.add(velocity.scalar(.05f));
        //Log.i("Ball", "New pos: " + position.x + ", " + position.y + ", " + position.z);
    }

    public void draw(Shader shader, float[] vpMatrix){

        float[] model = new float[16];
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, position.x, position.y, position.z);

        float[] mvp = new float[16];
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0);

        //mvp = vpMatrix;

        shader.enable();

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(shader.mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(shader.mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Enable a handle to the triangle normals
        GLES20.glEnableVertexAttribArray(shader.mNormalHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(shader.mNormalHandle, 3,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Set color for drawing the triangle
        GLES20.glUniform4fv(shader.mColorHandle, 1, color, 0);

        // Pass the mvp transformation to the shader
        GLES20.glUniformMatrix4fv(shader.mModelHandle, 1, false, model, 0);

        // Pass the mvp transformation to the shader
        //GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, mvp, 0);

        float[] lightPos = new float[]{0.0f, 3.0f, 0.0f};
        GLES20.glUniform3fv(shader.mLightPosHandle, 1, lightPos, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        shader.disable();
    }


}
