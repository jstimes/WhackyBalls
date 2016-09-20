package enterprises.stimes.whackyballs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Jacob on 5/21/2016.
 *
 * Scene object representing the floor the balls roll on, and the background scene
 */
public class Arena {

    private Triangle t1, t2;
    private Floor floor;

    private float height;
    private float width;

    private Vec3 color = new Vec3(1.0f, 1.0f, 1.0f);

    public Arena(Context cxt, float arenaSize, float floorDepth){

        width = arenaSize;
        height = floorDepth;

        Vec3 norm =  new Vec3(0.0f, 1.0f, 0.0f);
        Vec3 backLeft = new Vec3(-width, height, width);
        Vec3 backRight = new Vec3(width, height, width);
        Vec3 frontLeft = new Vec3(-width, height, -width);
        Vec3 frontRight = new Vec3(width, height, -width);

        t1 = new Triangle(backLeft, frontLeft, frontRight, norm, norm, norm);
        t2 = new Triangle(backLeft, frontRight, backRight, norm, norm, norm);

        t1.SetColor(color);
        t2.SetColor(color);

        if(cxt == null){
            Log.i("context", "Arena cxt null");
        }
        //floor = new Floor(cxt);

    }

    public void draw(Shader shader, float[] mvpMatrix){
        t1.draw(shader, mvpMatrix);
        t2.draw(shader, mvpMatrix);
        //floor.draw(mvpMatrix);
    }

    class Floor {

        private Context mContext;
        public float uvs[];
        public FloatBuffer vertexBuffer;
        public ShortBuffer drawListBuffer;
        public FloatBuffer uvBuffer;

        // We have to create the vertices of our triangle.
        float[] vertices = new float[]
        {  10.0f, 200f, 0.0f,
                10.0f, 100f, 0.0f,
                100f, 100f, 0.0f,
                100f, 200f, 0.0f,
        };

        short[] indices = new short[] {0, 1, 2, 0, 2, 3}; // The order of vertexrendering.

        public Floor(Context cxt){
            mContext = cxt;
            SetupTriangle();
            SetupImage();
        }

        public void SetupTriangle()
        {
            // We have to create the vertices of our triangle.
            vertices = new float[]
                    {-100.0f, -100.0f, -100.0f,
                            -100.0f, -100.0f, 100.0f,
                            100.0f, -100.0f, 100.0f,
                            100.0f, -100.0f, -100.0f,
                    };

            indices = new short[] {0, 1, 2, 0, 2, 3}; // The order of vertexrendering.

            // The vertex buffer.
            ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);

            // initialize byte buffer for the draw list
            ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(indices);
            drawListBuffer.position(0);


        }

        public void SetupImage(){
            // Create our UV coordinates.
            uvs = new float[] {
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f
            };

            // The texture buffer
            ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
            bb.order(ByteOrder.nativeOrder());
            uvBuffer = bb.asFloatBuffer();
            uvBuffer.put(uvs);
            uvBuffer.position(0);

            // Generate Textures, if more needed, alter these numbers.
            int[] texturenames = new int[1];
            GLES20.glGenTextures(1, texturenames, 0);

            // Retrieve our image from resources.
            int id = mContext.getResources().getIdentifier("drawable/lavaImage.jpg", null,
                    mContext.getPackageName());

            // Temporary create a bitmap
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);

            // Bind texture to texturename
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            // Set wrapping mode
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

            // We are done using the bitmap so we should recycle it.
            bmp.recycle();
        }

        public void draw(float[] m){
            // get handle to vertex shader's vPosition member
            int mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");

            // Enable generic vertex attribute array
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, vertexBuffer);

            // Get handle to texture coordinates location
            int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord" );

            // Enable generic vertex attribute array
            GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

            // Prepare the texturecoordinates
            GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                    false,
                    0, uvBuffer);

            // Get handle to shape's transformation matrix
            int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

            // Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

            // Get handle to textures locations
            int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" );

            // Set the sampler texture unit to 0, where we have saved the texture.
            GLES20.glUniform1i ( mSamplerLoc, 0);

            // Draw the triangle
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                    GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(mTexCoordLoc);
        }
    }
}
