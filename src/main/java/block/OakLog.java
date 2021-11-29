package block;

import org.joml.Vector2i;

import java.util.HashMap;

public class OakLog extends Block {

    private static final String NAME = "oak_log";
    private static final String[] NO_CONNECT = {};

    /*public OakLog(Vector2i position, HashMap<String, String> attributes) {
        super("oak_log", position, attributes);

        if (this.getAttribute("axis") == null) {
            this.setAttribute("axis", "y");
        }
    }*/

    public OakLog(int x, int y) {
        super(NAME, NO_CONNECT, x, y);
    }

}
