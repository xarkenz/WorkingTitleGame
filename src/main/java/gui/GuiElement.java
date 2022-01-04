package gui;

public abstract class GuiElement {

    protected final String name;
    protected int posX, posY, width, height;
    protected final GuiElement parent;

    protected boolean visible = false;
    protected boolean hovering = false;
    protected boolean focused = false;

    protected boolean isDirty = true;

    private int vertexIndex = -1;

    public GuiElement(String name, GuiElement parent, int x, int y, int w, int h) {
        this.name = name;
        this.parent = parent;
        this.posX = x;
        this.posY = y;
        this.width = w;
        this.height = h;
    }

    public abstract void start();
    public abstract void update(float dt);

    public void mousePress(int x, int y) {

    }

    public void mouseDrag(int x, int y) {

    }

    public void mouseRelease(int x, int y) {

    }

    public void resize(int w, int h) {
        width = w;
        height = h;
        isDirty = true;
    }

    public int loadVertexData(float[] vertices, int nextIndex) {
        return nextIndex;
    }

    public boolean wantsMouse(int x, int y) {
        return x >= posX - width / 2f && x <= posX + width / 2f && y >= posY - height / 2f && y <= posY + height / 2f;
    }

    public String getName() {
        return name;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GuiElement getParent() {
        return parent;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        isDirty = visible != this.visible;
        this.visible = visible;
    }

    public boolean isHovering() {
        return hovering;
    }

    public void setHovering(boolean hovering) {
        isDirty = hovering != this.hovering;
        this.hovering = hovering;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        isDirty = focused != this.focused;
        this.focused = focused;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public int getVertexIndex() {
        return vertexIndex;
    }

    public void setVertexIndex(int vertexIndex) {
        this.vertexIndex = vertexIndex;
    }

    public String toString() {
        return "GuiElement(" + name + ")";
    }

}
