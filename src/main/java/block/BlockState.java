package block;

public class BlockState {

    private final String[] keys;
    private final String[] values;

    public BlockState() {
        this.keys = new String[0];
        this.values = new String[0];
    }

    public BlockState(String[] keys, String[] values) {
        this.keys = keys;
        this.values = values;
    }

    public boolean hasAttr(String key) {
        for (String k : keys) {
            if (k.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    public String getAttr(String key) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equalsIgnoreCase(key)) {
                return values[i];
            }
        }
        return null;
    }

    public String setAttr(String key, String value) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equalsIgnoreCase(key)) {
                String old = values[i];
                values[i] = value;
                return old;
            }
        }
        return null;
    }

}
