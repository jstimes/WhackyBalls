package enterprises.stimes.whackyballs;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Created by Jacob on 5/21/2016.
 *
 * Just here as an example of how to do the basic rendering stuff in OpenGL ES
 */
public class Triangle {

    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 6;

    static final int POSITION_DATA_SIZE = 3;
    static final int NORMAL_DATA_SIZE = 3;

    float triangleCoords[];

    // Set color with red, green, blue and alpha (opacity) values
    private float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    private final int vertexCount = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //private final int mProgram;

    public Triangle(Vec3 a, Vec3 b, Vec3 c, Vec3 aN, Vec3 bN, Vec3 cN){
        float[] coords = new float[18];
        coords[0] = a.x;
        coords[1] = a.y;
        coords[2] = a.z;
        coords[3] = aN.x;
        coords[4] = aN.y;
        coords[5] = aN.z;


        coords[6] = b.x;
        coords[7] = b.y;
        coords[8] = b.z;
        coords[9] = bN.x;
        coords[10] = bN.y;
        coords[11] = bN.z;

        coords[12] = c.x;
        coords[13] = c.y;
        coords[14] = c.z;
        coords[15] = cN.x;
        coords[16] = cN.y;
        coords[17] = cN.z;

        // create empty OpenGL ES Program
        //mProgram = GLES20.glCreateProgram();

        init(coords);
    }

    public Triangle(float coords[]) {
        // create empty OpenGL ES Program
       // mProgram = GLES20.glCreateProgram();

        init(coords);
    }

    public void SetColor(Vec3 color){
        this.color[0] = color.x;
        this.color[1] = color.y;
        this.color[2] = color.z;
    }

    private void init(float coords[]){
        triangleCoords  = Arrays.copyOf(coords, coords.length);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
    }

    public void draw(Shader shader, float[] mvpMatrix) {
        shader.enable();

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(shader.mPositionHandle);

        // Prepare the triangle coordinate data
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(shader.mPositionHandle, POSITION_DATA_SIZE,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(shader.mNormalHandle);

        // Prepare the triangle coordinate data
        vertexBuffer.position(POSITION_DATA_SIZE);
        GLES20.glVertexAttribPointer(shader.mNormalHandle, NORMAL_DATA_SIZE,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Set color for drawing the triangle
        GLES20.glUniform4fv(shader.mColorHandle, 1, color, 0);

        // Pass the mvp transformation to the shader
        GLES20.glUniformMatrix4fv(shader.mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        float[] I4 = new float[16];
        Matrix.setIdentityM(I4, 0);

        // Pass the mvp transformation to the shader
        GLES20.glUniformMatrix4fv(shader.mModelHandle, 1, false, I4, 0);

        float[] lightPos = new float[]{0.0f, 3.0f, 0.0f};
        GLES20.glUniform3fv(shader.mLightPosHandle, 1, lightPos, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        shader.disable();
    }
}