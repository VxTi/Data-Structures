package project;

import org.lwjgl.opengl.GLCapabilities;

public class Window {

    public long windowId;
    public int width, height;
    public String title;

    public GLCapabilities glCapabilities;
    public WindowDrawable drawable;

    public double mouseX, mouseY, scrollX, scrollY;

    protected Window(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.mouseX = this.mouseY = this.scrollX = this.scrollY = 0.0D;
    }

    public float getAspect() {
        return (float)width / (float)height;
    }

    public static abstract class WindowDrawable {

        public final Window ctx;
        public WindowDrawable(Window ctx) {
            this.ctx = ctx;
        }
        public abstract void draw(double mouseX, double mouseY, double dt);
    }

}
