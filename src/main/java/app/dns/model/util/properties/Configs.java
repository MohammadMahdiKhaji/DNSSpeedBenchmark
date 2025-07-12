package app.dns.model.util.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Configs {
    private static Logger logger = LogManager.getLogger(Configs.class);
    private final static String CONFIGS_PROPERTIES = "/config.properties";
    private static Property<Integer> threadPoolSizeProperty = new Property("thread-pool.size", Integer.class);
    private static Property<Integer> pingTimeoutProperty = new Property("ping.timeout", Integer.class);
    private static Property<Integer> pingPacketCountProperty = new Property("ping.packet-count", Integer.class);
    private static Property<Integer> reachabilityTimeoutProperty = new Property("reachability.timeout", Integer.class);
    private static List<Property> properties = new ArrayList<>();
    private static Properties configProps = new Properties();

    static {
        properties.add(threadPoolSizeProperty);
        properties.add(pingTimeoutProperty);
        properties.add(pingPacketCountProperty);
        properties.add(reachabilityTimeoutProperty);
    }

    public Configs() {}

    public void loadValues()  throws IOException {
        configProps.load(Configs.class.getResourceAsStream(CONFIGS_PROPERTIES));

        for (int i=0; i<properties.size(); i++) {
            configProps.getProperty(properties.get(i).getPropertyFullName());
            properties.get(i).setPropertyValue(
                    configProps.getProperty(properties.get(i).getPropertyFullName()));
        }

        logger.info("****** All Properties Loaded ******");
    }

    public static Integer getThreadPoolSize() {
        return threadPoolSizeProperty.getPropertyValue();
    }

    public static Integer getPingTimeout() {
        return pingTimeoutProperty.getPropertyValue();
    }

    public static Integer getPingPacketCount() {
        return pingPacketCountProperty.getPropertyValue();
    }

    public static Integer getReachabilityTimeout() {
        return reachabilityTimeoutProperty.getPropertyValue();
    }
}