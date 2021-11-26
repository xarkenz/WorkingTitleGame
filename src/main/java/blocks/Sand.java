package blocks;

import org.joml.Vector2i;

import java.util.HashMap;

public class Sand extends Block {

    public Sand(Vector2i position, HashMap<String, String> attributes) {
        super("sand", position, attributes);
    }

}
