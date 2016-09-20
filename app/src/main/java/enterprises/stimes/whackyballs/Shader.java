package enterprises.stimes.whackyballs;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by Jacob on 5/21/2016.
 *
 * Abstracts most of the shader junk
 */
public class Shader {
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec3 vPosition;" +
                    "attribute vec3 vNormal;" +

                    "uniform mat4 model;" +

                    "varying vec3 normal;" +
                    "varying vec3 fragPos;" +

                    "void main() {" +

                    //"  normal = vNormal;" +
                    "  normal = mat3(model) * vNormal;" +
                    "  fragPos = vec3(model * vec4(vPosition, 1.0f));" +
                    "  gl_Position = uMVPMatrix * vec4(vPosition, 1.0f);" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform vec3 lightPos;" +

                    "varying vec3 fragPos;" +
                    "varying vec3 normal;" +

                    "void main() {" +
                    "  vec3 lightColor = vec3(1.0, 1.0, 1.0);" +

                    "  float ambientStrength = 0.7f;"+
                    "  vec3 ambient = ambientStrength * lightColor;"+

                    //"  // Diffuse "+
                    "  vec3 norm = normalize(normal);"+
                    "  vec3 lightDir = normalize(lightPos - fragPos);"+
                    "  float diff = max(dot(norm, lightDir), 0.0);"+
                    "  vec3 diffuse = diff * lightColor;"+
                    "  gl_FragColor = vec4((ambient + diffuse), 1.0f) * vColor;"+
                    "}";

    public int mPositionHandle;

    public int mNormalHandle;

    public int mColorHandle;

    // Use to access and set the view transformation
    public int mMVPMatrixHandle;

    public int mModelHandle;

    public int mLightPosHandle;

    public final int mProgram;

    public Shader(){
        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        int vertexShader = GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);

        checkGLError("vert shader load");

        int fragmentShader = GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        checkGLError("frag shader load");

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        checkGLError("vert shader attach");

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        checkGLError("frag shader attach");

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        checkGLError("link program");

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        mModelHandle = GLES20.glGetUniformLocation(mProgram, "model");

        mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "lightPos");

        GLES20.glUniform3fv(mLightPosHandle, 1, new float[]{0.0f, 3.0f, 0.0f}, 0);
    }

    public void enable(){
        GLES20.glUseProgram(mProgram);
    }

    public void disable(){
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("Shader", op + ": glError " + error);
        }
    }
}
