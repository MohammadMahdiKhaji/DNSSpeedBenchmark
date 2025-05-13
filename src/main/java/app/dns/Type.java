package app.dns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Type {
    public static final int EA_SERVERS = 0;
    public static final int MICROSOFT_SERVERS = 1;
    public static final int ROCKSTAR_SERVERS = 2;
    private static final List<String> names = new ArrayList<>();

    public Type() {
    }

    static {
        names.add(0, "EA_SERVERS");
        names.add(1, "MICROSOFT_SERVERS");
        names.add(2, "ROCKSTAR_SERVERS");
    }

    public String getNameByNumber(int number) {
        if (number < names.size()) {
            return names.get(number);
        }
        throw new RuntimeException("number not found"); // TODO: 5/12/2025 not best practice
    }

    public int getNumberByName(String targetName) {
        for (int i=0; i < names.size(); i++) {
            if (targetName.equals(names.get(i)))
                return i;
        }
        throw new RuntimeException("name not found"); // TODO: 5/12/2025 not best practice
    }

    public List<String> getNames() {
        return names;
    }
}
