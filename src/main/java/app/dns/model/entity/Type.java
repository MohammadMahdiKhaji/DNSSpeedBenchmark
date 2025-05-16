package app.dns.model.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public final class Type {
    private static Logger logger = LogManager.getLogger(Type.class);
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
        logger.error("Name couldn't be found for number: " + number);
        return null;
    }

    public int getNumberByName(String targetName) {
        for (int i=0; i < names.size(); i++) {
            if (targetName.equals(names.get(i)))
                return i;
        }
        logger.error("Number couldn't be found for type: " + targetName);
        return -1;
    }

    public List<String> getNames() {
        return names;
    }
}
