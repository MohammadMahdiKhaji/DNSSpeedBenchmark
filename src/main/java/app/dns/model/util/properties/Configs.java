package app.dns.model.util.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Configs {
    private static Logger logger = LogManager.getLogger(Configs.class);
    private static Configs INSTANCE;
    private static final String CONFIGS_PROPERTIES = "/config.properties";
    private static Property<Integer> threadPoolSizeProperty = new Property("thread-pool.size", Integer.class);
    private static Property<String> jmxServerIP = new Property("jmxremote.server_ip", String.class);
    private static Property<Integer> jmxServerPort = new Property("jmxremote.server_port", Integer.class);
    private static Property<String> jmxServerJNDI = new Property("jmxremote.server_jndi", String.class);
    private static Property<Integer> pingTimeoutProperty = new Property("ping.timeout", Integer.class);
    private static Property<Integer> pingPacketCountProperty = new Property("ping.packet-count", Integer.class);
    private static Property<Integer> reachabilityTimeoutProperty = new Property("reachability.timeout", Integer.class);
    private static List<Property> properties = new ArrayList<>();
    private static Properties configProps = new Properties();

    static {
        properties.add(threadPoolSizeProperty);
        properties.add(jmxServerIP);
        properties.add(jmxServerPort);
        properties.add(jmxServerJNDI);
        properties.add(pingTimeoutProperty);
        properties.add(pingPacketCountProperty);
        properties.add(reachabilityTimeoutProperty);
    }

    private Configs() {
    }

    public static Configs getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Configs();
        }

        return INSTANCE;
    }

    public void loadValues()  throws IOException {
        configProps.load(Configs.class.getResourceAsStream(CONFIGS_PROPERTIES));

        for (int i=0; i<properties.size(); i++) {
            String propertyFullName = properties.get(i).getPropertyFullName();
            configProps.getProperty(propertyFullName);
            properties.get(i).setPropertyValue(
                    configProps.getProperty(propertyFullName));
        }

        logger.info("****** All Properties Loaded ******");
    }

    public static Integer getThreadPoolSize() {
        return threadPoolSizeProperty.getPropertyValue();
    }

    public static String getJmxServerIP() {
        return jmxServerIP.getPropertyValue();
    }

    public static Integer getJmxServerPort() {
        return jmxServerPort.getPropertyValue();
    }

    public static String getJmxServerJNDI() {
        return jmxServerJNDI.getPropertyValue();
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