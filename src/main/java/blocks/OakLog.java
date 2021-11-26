package blocks;

import org.joml.Vector2i;

import java.util.HashMap;

public class OakLog extends Block {

    public OakLog(Vector2i position, HashMap<String, String> attributes) {
        super("oak_log", position, attributes);

        if (this.getAttribute("axis") == null) {
            this.setAttribute("axis", "y");
        }
    }

}
