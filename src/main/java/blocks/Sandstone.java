package blocks;

import org.joml.Vector2i;

import java.util.HashMap;

public class Sandstone extends Block {

    public Sandstone(Vector2i position, HashMap<String, String> attributes) {
        super("sandstone", position, attributes);
    }

}
