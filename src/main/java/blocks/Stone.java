package blocks;

import org.joml.Vector2i;

import java.util.HashMap;

public class Stone extends Block {

    public Stone(Vector2i position, HashMap<String, String> attributes) {
        super("stone", position, attributes);
    }

}
