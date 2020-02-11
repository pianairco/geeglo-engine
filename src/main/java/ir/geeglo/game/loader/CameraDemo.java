package ir.geeglo.game.loader;

import ir.geeglo.game.loader.ms3d.MS3DModelLoader;
import ir.geeglo.game.loader.ms3d.MS3DModelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class CameraDemo {
    GLFWErrorCallback errorCallback;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;

    long window;
    int width = 640;
    int height = 480;

    // Declare matrices for two cameras
    Matrix4f projMatrix =  new Matrix4f();
    Matrix4f viewMatrix =  new Matrix4f();
    int active = 0;
    int inactive = 1;
    // And a model matrix for a rotating cube
    Matrix4f modelMatrix = new Matrix4f();
    // Temporary vector
    Vector3f tmp = new Vector3f();
    // Rotation of the inactive camera
    float rotate = 0.0f;
    float[] rotation = {0.0f, 0.0f};

    FloatBuffer fb = BufferUtils.createFloatBuffer(16);

    void run() {
        try {
            init();
            loop();

            glfwDestroyWindow(window);
            keyCallback.free();
        } finally {
            glfwTerminate();
            errorCallback.free();
        }
    }

    MS3DModelRenderer renderer;

    void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Hello Cameras!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        System.out.println("Press 'C' to switch between the two cameras");
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key,
                    int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
                if (key == GLFW_KEY_C && action == GLFW_RELEASE)
                    switchCamera();
                if (key == GLFW_KEY_LEFT && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
                    rotate = 1.0f;
                } else if (key == GLFW_KEY_LEFT && (action == GLFW_RELEASE)) {
                    rotate = 0.0f;
                } else if (key == GLFW_KEY_RIGHT && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
                    rotate = -1.0f;
                } else if (key == GLFW_KEY_RIGHT && (action == GLFW_RELEASE)) {
                    rotate = 0.0f;
                }
            }
        });
        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        width = framebufferSize.get(0);
        height = framebufferSize.get(1);

        MS3DModelLoader ms3DModelLoader = new MS3DModelLoader();
        ms3DModelLoader.initModel("./Models/dwarf2.ms3d");
        ms3DModelLoader.loadModel();
        renderer = new MS3DModelRenderer(ms3DModelLoader);
    }

    void renderGrid() {
        glBegin(GL_LINES);
        glColor3f(0.2f, 0.2f, 0.2f);
        for (int i = -20; i <= 20; i++) {
            glVertex3f(-20.0f, 0.0f, i);
            glVertex3f(20.0f, 0.0f, i);
            glVertex3f(i, 0.0f, -20.0f);
            glVertex3f(i, 0.0f, 20.0f);
        }
        glEnd();
    }

    void switchCamera() {
        active = 1 - active;
        inactive = 1 - inactive;
    }

//    void loop() {
//        GL.createCapabilities();
//
//        // Set the clear color
//        glClearColor(0.6f, 0.7f, 0.8f, 1.0f);
//        // Enable depth testing
//        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_CULL_FACE);
//
//        // Remember the current time.
//        long firstTime = System.nanoTime();
//        long lastTime = firstTime;
//
//        // FloatBuffer for transferring matrices to OpenGL
//        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
//        // Matrix to build combined model-view
//        Matrix4f modelView = new Matrix4f();
//        // Matrix to build combined view-projection
//        Matrix4f viewProj = new Matrix4f();
//
//        while ( !glfwWindowShouldClose(window) ) {
//            long thisTime = System.nanoTime();
//            float diff = (thisTime - firstTime) / 1E9f;
//            float angle = diff;
//            float delta = (thisTime - lastTime) / 1E9f;
//            lastTime = thisTime;
//
//            // Process rotation
//            rotation[inactive] += rotate * delta;
//
//            // Setup both camera's projection matrices
//            projMatrix[0].setPerspective((float) Math.toRadians(40), (float)width/height, 1.0f, 20.0f);
//            projMatrix[1].setPerspective((float) Math.toRadians(30), (float)width/height, 2.0f, 5.0f);
//
//            // Load the active camera's projection
//            glMatrixMode(GL_PROJECTION);
//            glLoadMatrixf(projMatrix[active].get(fb));
//
//            // Setup both camera's view matrices
//            viewMatrix[0].setLookAt(0, 2, 10, 0, 0, 0, 0, 1, 0).rotateY(rotation[0]);
//            viewMatrix[1].setLookAt(3, 1, 1, 0, 0, 0, 0, 1, 0).rotateY(rotation[1]);
//
//            // Apply model transformation to active camera's view
//            modelMatrix.rotationY(angle * (float) Math.toRadians(10));
//            viewMatrix[active].mul(modelMatrix, modelView);
//
//            // And load it
//            glMatrixMode(GL_MODELVIEW);
//            glLoadMatrixf(modelView.get(fb));
//
//            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//            glViewport(0, 0, width, height);
//            // Render a cube
////            renderCube();
//            renderer.drawModel1();
//
//            // Load the active camera's view again to render the inactive camera's frustum
//            glLoadMatrixf(viewMatrix[active].get(fb));
//            // Compute and render the inactive camera's frustum
//            viewProj.set(projMatrix[inactive]).mul(viewMatrix[inactive]);
//            renderFrustum(viewProj);
//
//            glfwSwapBuffers(window);
//            glfwPollEvents();
//        }
//    }

    void loop() {
        GL.createCapabilities();

        // Set the clear color
//        glClearColor(0.8f, 0.0f, 0.0f, 1.0f);
        glClearColor(0.6f, 0.7f, 0.8f, 1.0f);
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        // Remember the current time.
        long firstTime = System.nanoTime();

        renderer.buildModel1();

        while ( !glfwWindowShouldClose(window) ) {
            // Build time difference between this and first time.
            long thisTime = System.nanoTime();
            float diff = (thisTime - firstTime) / 1E9f;
            // Compute some rotation angle.
            float angle = diff;

            // Make the viewport always fill the whole window.
            glViewport(0, 0, width, height);

            // Build the projection matrix. Watch out here for integer division
            // when computing the aspect ratio!
            projMatrix.setPerspective((float) Math.toRadians(30.0f),
                    (float)width/height, 0.01f, 100.0f);
            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(projMatrix.get(fb));

            // Build a model-view matrix which first rotates the cube
            // about the Y-axis and then lets a "camera" look at that
            // cube from a certain distance.
            viewMatrix.setLookAt(0.0f, 2.0f, 15.0f,
                    0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f)
                    // rotate 90 degrees per second
                    .rotateY(angle * (float) Math.toRadians(90));
            glMatrixMode(GL_MODELVIEW);
            glLoadMatrixf(viewMatrix.get(fb));

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Render a simple cube
            renderer.drawModel1();
//            glDrawElements(GL_TRIANGLES, 6 * 6, GL_UNSIGNED_INT, 0L);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new CameraDemo().run();
    }
}
