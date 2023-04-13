package project.drawable;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import project.Main;
import project.Window;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL11.*;

public class Drawable3D extends Window.WindowDrawable {

    private Matrix4f viewProjectionMatrix = new Matrix4f();
    private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    private VBO box;

    public Drawable3D(Window window) {
        super(window);
        box = new VBO()
                .position(-0.5, -0.5, -0.5).add()
                .position(0.5, -0.5, -0.5).add()
                .position(0.5, 0.5, -0.5).add()
                .position(-0.5, 0.5, -0.5).add()

                .position(-0.5, -0.5, 0.5).add()
                .position(0.5, -0.5, 0.5).add()
                .position(0.5, 0.5, 0.5).add()
                .position(-0.5, 0.5, 0.5).add()
                .index(0, 1,  1, 2,  2, 3,  3, 0,
                        4, 5,  5, 6,  6, 7,  7, 4,
                        0, 4,  3, 7,  1, 5,  2, 6)
                .mode(GL_LINES);
    }

    @Override
    public void draw(double mouseX, double mouseY, double dt) {
        if (!box.initialized)
            box.make();
        glPushMatrix();
        glLoadIdentity();
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        viewProjectionMatrix
                .setPerspective((float) Math.toRadians(80), ctx.getAspect(), 0.01f, 100.0f)
                .lookAt(0.0f, 0.0f, -0.1f,
                        0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f);

        glLoadMatrixf(viewProjectionMatrix.get(buffer));
        glTranslated(0, 0, -1);
        box.draw();
    }
}
