package project;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.Renderable;
import project.drawable.VBO;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public class WindowManager {

    private WindowManager() {}

    private static final List<GLFWMouseButtonCallbackI> mouseInteractCallbacks = new ArrayList<>();
    private static final List<GLFWKeyCallbackI> keyInteractCallbacks = new ArrayList<>();
    private static final List<GLFWCharCallbackI> charTypedCallbacks  = new ArrayList<>();
    private static final List<GLFWScrollCallbackI> scrollCallbacks   = new ArrayList<>();

    private static ConcurrentLinkedQueue<Window> windows = new ConcurrentLinkedQueue<>();

    public static final Object thread_lock = new Object();

    private static float displayScaleX, displayScaleY;

    public static Window createWindow(String title, int width, int height) {
        Window instance = new Window(title, width, height);
        windows.add(instance);
        return instance;
    }

    public static void loadWindows() {

        if (!glfwInit())
            throw new RuntimeException("Failed to initialize GLFW.");

        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        glfwDefaultWindowHints();

        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_TRUE);

        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            FloatBuffer width = stack.mallocFloat(1);
            FloatBuffer height = stack.mallocFloat(1);

            glfwGetMonitorContentScale(glfwGetPrimaryMonitor(), width, height);
            displayScaleX = width.get(0);
            displayScaleY = height.get(0);
        }

        for (Window window : windows) {
            try {
                // Create the window
                window.windowId = glfwCreateWindow(window.width, window.height, window.title, 0, 0);
                window.width *= displayScaleX;
                window.height *= displayScaleY;

                if (window.windowId == 0) {
                    System.err.printf("Failed to create window '%s'", window.title);
                    continue;
                }

                glfwMakeContextCurrent(window.windowId);
                window.glCapabilities = GL.createCapabilities();
                GL.setCapabilities(window.glCapabilities);

                glfwSwapInterval(GLFW_TRUE);

                glfwSetCursorPosCallback(window.windowId, (wId, mouseX, mouseY) -> {
                    window.mouseX = mouseX * displayScaleX;
                    window.mouseY = mouseY * displayScaleY;
                });

                glfwSetScrollCallback(window.windowId, (wId, scrollX, scrollY) ->
                        scrollCallbacks.forEach(callback -> callback.invoke(wId, scrollX, scrollY)));

                glfwSetMouseButtonCallback(window.windowId, (wId, button, state, mods) ->
                        mouseInteractCallbacks.forEach(callback -> callback.invoke(wId, button, state, mods)));

                glfwSetKeyCallback(window.windowId, (wId, key, state, scanCode, mods) ->
                        keyInteractCallbacks.forEach(callback -> callback.invoke(wId, key, state, scanCode, mods)));

                glfwSetCharCallback(window.windowId, (wId, ch) ->
                        charTypedCallbacks.forEach(callbacks -> callbacks.invoke(wId, ch)));

                glfwSetWindowCloseCallback(window.windowId, (wId) -> {
                    glfwFreeCallbacks(window.windowId);
                    glfwDestroyWindow(window.windowId);

                    windows.remove(window);

                    if (windows.isEmpty()) {
                        glfwTerminate();
                        System.exit(1);
                    }
                });

                glfwShowWindow(window.windowId);
            } catch (Exception e) {
                System.err.println("An error occurred whilst attempting to create window:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void manage() {

        (new Thread(() -> {
            long time = System.nanoTime();
            double dt;
            while (isAlive()) {
                dt = (System.nanoTime() - time) / 1E9D;
                time = System.nanoTime();
                for (Window window : windows) {
                    if (window.drawable != null) {
                        glfwMakeContextCurrent(window.windowId);
                        GL.setCapabilities(window.glCapabilities);
                        window.drawable.draw(window.mouseX, window.mouseY, dt);
                    }
                }
            }
        })).start();

        while (isAlive()) glfwWaitEvents();
    }

    public static Window getWindow(long windowId) {
        for (Window window : windows) {
            if (window.windowId == windowId)
                return window;
        }
        return null;
    }

    public static List<Window> getWindows() {
        return windows.stream().toList();
    }

    public static boolean isAlive() {
        return !windows.isEmpty();
    }
}