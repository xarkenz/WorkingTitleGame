package core;

import gui.GuiElement;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import renderer.*;
import util.Logger;
import world.Overworld;
import world.World;
import util.AssetPool;
import util.Settings;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private static final String GAME_NAME = "WorkingTitleGame";

    private int width, height;
    private String title;
    private long glfwWindow;
    private ImGuiLayer imGuiLayer;
    private Framebuffer framebuffer;
    private PickingTexture pickingTexture;

    public Vector4f clearColor;

    private static Window window;

    private long audioContext;
    private long audioDevice;

    private static World currentWorld;
    private static GuiElement focusedElement;

    private Window() {
        width = 1920;
        height = 1080;
        title = GAME_NAME + " (In-game)";
        clearColor = new Vector4f(0.6f, 0.8f, 1, 1);
    }

    public static void changeWorld(int newWorld) {
        if (newWorld == 0) {
            currentWorld = new Overworld();
        } else {
            Logger.critical("Invalid world ID:", newWorld);
        }

        currentWorld.load();
        currentWorld.init();
        currentWorld.start();
    }

    public static Window get() {
        if (Window.window == null) Window.window = new Window();
        return Window.window;
    }

    public void run() {
        init();
        loop();

        // Destroy audio context
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);

        // Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(Logger.getErr()).set();

        // Initialize GLFW
        if (!glfwInit()) Logger.critical("Failed to initialize GLFW.");

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        // Create the window
        glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);
        if (glfwWindow == NULL) Logger.critical("Failed to create the GLFW window.");

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
        glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });

        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make window visible
        glfwShowWindow(glfwWindow);

        // Initialize the audio device
        String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDevice);

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            Logger.critical("Unsupported audio library.");
        }

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        framebuffer = new Framebuffer(Settings.DISPLAY_WIDTH, Settings.DISPLAY_HEIGHT);
        pickingTexture = new PickingTexture(Settings.DISPLAY_WIDTH, Settings.DISPLAY_HEIGHT);
        glViewport(0, 0, Settings.DISPLAY_WIDTH, Settings.DISPLAY_HEIGHT);

        imGuiLayer = new ImGuiLayer(glfwWindow, pickingTexture);
        imGuiLayer.initImGui();

        Window.changeWorld(0);
    }

    public void loop() {
        float beginTime = (float) glfwGetTime();
        float endTime;
        float dt = -1;

        Shader defaultShader = AssetPool.getShader("assets/shaders/default.glsl");
        Shader pickingShader = AssetPool.getShader("assets/shaders/picking_texture.glsl");

        while (!glfwWindowShouldClose(glfwWindow)) {
            // Poll events
            glfwPollEvents();

            // Render pass 1. Render picking texture
            glDisable(GL_BLEND);
            pickingTexture.enableWriting();

            glViewport(0, 0, Settings.DISPLAY_WIDTH, Settings.DISPLAY_HEIGHT);
            glClearColor(0, 0, 0, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Renderer.bindShader(pickingShader);
            currentWorld.render();

            pickingTexture.disableWriting();
            glEnable(GL_BLEND);

            // Render pass 2. Render graphics
            if (Settings.ENABLE_DEBUG) DebugDraw.beginFrame();

            framebuffer.bind();

            glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
            glClear(GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                Renderer.bindShader(defaultShader);
                currentWorld.update(dt);
                if (Settings.ENABLE_DEBUG) DebugDraw.draw(true);
                currentWorld.render();
                if (Settings.ENABLE_DEBUG) DebugDraw.draw(false);
            }

            framebuffer.unbind();

            imGuiLayer.update(dt, currentWorld);
            glfwSwapBuffers(glfwWindow);
            MouseListener.endFrame();

            endTime = (float) glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }

        currentWorld.save();
    }

    public static World getWorld() {
        return currentWorld;
    }

    public static int getWidth() {
        return get().width;
    }

    public static int getHeight() {
        return get().height;
    }

    public static void setWidth(int newWidth) {
        get().width = newWidth;
    }

    public static void setHeight(int newHeight) {
        get().height = newHeight;
    }

    public static Framebuffer getFramebuffer() {
        return get().framebuffer;
    }

    public static float getTargetAspectRatio() {
        return 16f / 9f;
    }

    public static ImGuiLayer getImGuiLayer() {
        return get().imGuiLayer;
    }
}
