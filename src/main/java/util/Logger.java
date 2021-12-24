package util;

import java.io.PrintStream;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Logger {

    private static PrintStream out = System.out;
    private static PrintStream err = System.err;
    public static boolean debug = false;

    public static PrintStream getOut() {
        return out;
    }

    public static PrintStream getErr() {
        return err;
    }

    public static void setOut(PrintStream printer) {
        out = printer;
    }

    public static void setErr(PrintStream printer) {
        err = printer;
    }

    private static String join(Object[] text) {
        StringBuilder joined = new StringBuilder();
        String sep = "";
        for (Object s : text) {
            joined.append(sep).append(s);
            sep = " ";
        }
        return joined.toString();
    }

    public static void log(Object... text) {
        out.println("[" + glfwGetTime() + "] " + join(text));
    }

    public static void info(Object... text) {
        out.println("[" + glfwGetTime() + "] INFO: " + join(text));
    }

    public static void debug(Object... text) {
        out.println("[" + glfwGetTime() + "] DEBUG: " + join(text));
    }

    public static void warning(Object... text) {
        err.println("[" + glfwGetTime() + "] WARNING: " + join(text));
    }

    public static void critical(Object... text) {
        String what = join(text);
        err.println("[" + glfwGetTime() + "] ERROR: " + what);
        throw new IllegalStateException(what);
    }

}
