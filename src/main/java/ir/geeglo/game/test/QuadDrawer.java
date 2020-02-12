package ir.geeglo.game.test;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;


import static org.lwjgl.opengl.GL15.*;

public class QuadDrawer extends OpenGLDrawer {
    float[] vertices = {
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f
    };

    float[] colors = {
            0.0f, 0.0f, 1.0f,    // Left top         ID: 0
            0.0f, 1.0f, 0.0f,   // Left bottom      ID: 1
            0.0f, 0.0f, 1.0f,    // Left top         ID: 0
            1.0f, 0.0f, 0.0f,  // Right left       ID: 3
            0.0f, 1.0f, 0.0f,    // Right bottom     ID: 2
            0.0f, 0.0f, 1.0f    // Right bottom     ID: 2
    };

    byte[] indices = new byte[36];

    // Quad variables
    private int vaoId = 0;
    private int vcoId = 0;
    private int vioId = 0;
    private int vboiId = 0;
    private int indicesCount = 0;

    // JOML matrices
    Matrix4f projMatrix = new Matrix4f();
    Matrix4f viewMatrix = new Matrix4f();

    // FloatBuffer for transferring matrices to OpenGL
    FloatBuffer fb = BufferUtils.createFloatBuffer(16);

    public QuadDrawer(int width, int height) {
        super(width, height);
    }

    @Override
    public void initScene() {
        // Sending data to OpenGL requires the usage of (flipped) byte buffers
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices);
        verticesBuffer.flip();

        // Create a new Vertex Array Object in memory and select it (bind)
        // A VAO can have up to 16 attributes (VBO's) assigned to it by default
        vaoId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vaoId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, 0, 0);

        // setup vertex color buffer
        vcoId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vcoId);
        glBufferData(GL_ARRAY_BUFFER, colors, GL_STATIC_DRAW);
        glEnableClientState(GL_COLOR_ARRAY);
        glColorPointer(3, GL_FLOAT, 0, 0);

        // Create a new VBO for the indices and select it (bind)
        for (byte i = 0; i < 4 * 6; i += 4) {
            indices[i] = i;
            indices[i + 1] = (byte)(i + 1);
            indices[i + 2] = (byte)(i + 2);
            indices[i + 3] = (byte)(i + 2);
            indices[i + 4] = (byte)(i + 3);
            indices[i + 5] = i;
        }
        indicesCount = indices.length;
        ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indices.length);
        indicesBuffer.put(indices);
        indicesBuffer.flip();

        // Create a new Vertex Buffer Object in memory and select it (bind)
        // A VBO is a collection of Vectors which in this case resemble the location of each vertex.
        vioId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vioId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
    }

    // Remember the current time.
    long firstTime = System.nanoTime();

    @Override
    public void renderScene() {
        long thisTime = System.nanoTime();
        float diff = (thisTime - firstTime) / 1E9f;
        // Compute some rotation angle.
        float angle = diff;

        // Bind to the VAO that has all the information about the vertices
        glViewport(0, 0, width, height);

// Build the projection matrix. Watch out here for integer division
        // when computing the aspect ratio!
        projMatrix.setPerspective((float) Math.toRadians(30.0f),
                (float) width / height, 0.01f, 100.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadMatrixf(projMatrix.get(fb));

        // Build a model-view matrix which first rotates the cube
        // about the Y-axis and then lets a "camera" look at that
        // cube from a certain distance.
        viewMatrix.setLookAt(0.0f, 2.0f, 15.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f)
                .rotateY(angle * (float) Math.toRadians(90));

        glMatrixMode(GL_MODELVIEW);
        glLoadMatrixf(viewMatrix.get(fb));

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Render a simple cube
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0L);
    }

    public void destroyOpenGL() {
    }

    public static void main(String[] args) {
        QuadDrawer quadDrawer = new QuadDrawer(640, 480);
        quadDrawer.run();
    }
}
