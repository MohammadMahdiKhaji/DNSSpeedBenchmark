package org.dns;

import java.util.HashMap;
import java.util.Map;

public final class Type {
    public static final int EA_SERVERS = 0;
    public static final int MICROSOFT_SERVERS = 1;
    public static final int ROCKSTAR_SERVERS = 2;
    private static final Map<Integer, String> intToName = new HashMap<>();

    private Type() {
    }


    //
    static {
        intToName.put(0, "EA_SERVERS");
        intToName.put(1, "MICROSOFT_SERVERS");
        intToName.put(2, "ROCKSTAR_SERVERS");
    }

    //
    public String getName(int number) {
        return intToName.get(number);
    }
}
