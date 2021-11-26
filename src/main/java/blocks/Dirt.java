package blocks;

import org.joml.Vector2i;

import java.util.HashMap;

public class Dirt extends Block {

    public Dirt(Vector2i position, HashMap<String, String> attributes) {
        super("dirt", position, attributes);
    }

}
