package gui;

public abstract class GuiElement {

    private int width, height;
    private final GuiElement parent;

    public GuiElement(GuiElement parent, int width, int height) {
        this.parent = parent;
        this.width = width;
        this.height = height;
    }

    public abstract void start();
    public abstract void update(float dt);

    public abstract void onHover(int x, int y);
    public abstract void onPress(int x, int y);
    public abstract void onDrag(int x, int y);
    public abstract void onRelease(int x, int y);

}
