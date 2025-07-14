package app.dns.model.util.jmx;

import app.dns.model.util.jmx.auth.CustomAuthenticator;
import app.dns.model.util.jmx.mbeans.DNSBenchmarkStats;
import app.dns.model.util.properties.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

public class JMXServer {
    private static Logger logger = LogManager.getLogger(JMXServer.class);
    private static JMXServer INSTANCE;
    private static final String HOST = Configs.getJmxServerIP();
    private static final int PORT = Configs.getJmxServerPort();
    private static final String JNDINAME = Configs.getJmxServerJNDI();

    private JMXServer() {
    }

    public static JMXServer getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new JMXServer();
        }

        return INSTANCE;
    }

    public void startJMXServer() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName mbeanObjectName = new ObjectName("app.dns.model.util.jmx.mbeans:type=DNSBenchmarkStats");
            DNSBenchmarkStats mbean = new DNSBenchmarkStats();

            registerMBean(mbs, mbean, mbeanObjectName);

            JMXServiceURL url = new JMXServiceURL(
                    "service:jmx:rmi:///jndi/rmi://"+HOST+":"+PORT+"/"+JNDINAME);

            HashMap<String, Object> env = new HashMap<>();
            env.put(JMXConnectorServer.AUTHENTICATOR, new CustomAuthenticator());

            JMXConnectorServer cs =
                    JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);

            printMBeanInfo(mbs, mbeanObjectName, "DNSBenchmarkStats");

            LocateRegistry.createRegistry(PORT);
            cs.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerMBean(MBeanServer mbs, Object mbean, ObjectName mbeanObjectName) {
        try {
            mbs.registerMBean(mbean, mbeanObjectName);
        } catch (InstanceAlreadyExistsException e) {
            logger.error("Registering MBean failed: {}", e.getMessage());
        } catch (MBeanRegistrationException e) {
            logger.error("Registering MBean failed: {}", e.getMessage());
        } catch (NotCompliantMBeanException e) {
            logger.error("Registering MBean failed: {}", e.getMessage());
        }
    }

    private static void printMBeanInfo(MBeanServer mbs,
                                       ObjectName mbeanObjectName,
                                       String mbeanClassName) {
        logger.info("Retrieve the management information for the {}", mbeanClassName);
        MBeanInfo info = null;
        try {
            info = mbs.getMBeanInfo(mbeanObjectName);
        } catch (Exception e) {
            logger.error("Could not get MBeanInfo object for {}", mbeanClassName);
            return;
        }

        logger.info("CLASSNAME:\t {}\n DESCRIPTION:\t {}\n ATTRIBUTES", info.getClassName(), info.getDescription());
        MBeanAttributeInfo[] attrInfo = info.getAttributes();
        if (attrInfo.length > 0) {
            for (int i = 0; i < attrInfo.length; i++) {
                logger.info(" ** NAME:\t {}\n    DESCR:\t {}\n    TYPE:\t {}\tREAD: {}\tWRITE: {}",
                        attrInfo[i].getName(),
                        attrInfo[i].getDescription(),
                        attrInfo[i].getType(),
                        attrInfo[i].isReadable(),
                        attrInfo[i].isWritable()
                );
            }
        } else
            logger.info(" ** No attributes **");
        logger.info("\nCONSTRUCTORS");
        MBeanConstructorInfo[] constrInfo = info.getConstructors();
        for (int i = 0; i < constrInfo.length; i++) {
            logger.info(" ** NAME:\t {}\n    DESCR:\t {}\n    PARAM:\t {} parameter(s)",
                    constrInfo[i].getName(),
                    constrInfo[i].getDescription(),
                    constrInfo[i].getSignature().length
            );
        }
        logger.info("\nOPERATIONS");
        MBeanOperationInfo[] opInfo = info.getOperations();
        if (opInfo.length > 0) {
            for (int i = 0; i < opInfo.length; i++) {
                logger.info(" ** NAME:\t {}\n    DESCR:\t {}\n    PARAM:\t {} parameter(s)",
                        opInfo[i].getName(),
                        opInfo[i].getDescription(),
                        opInfo[i].getSignature().length
                );
            }
        } else
            logger.info(" ** No operations **");
        logger.info("\nNOTIFICATIONS");
        MBeanNotificationInfo[] notifInfo = info.getNotifications();
        if (notifInfo.length > 0) {
            for (int i = 0; i < notifInfo.length; i++) {
                logger.info(" ** NAME:\t {}\n    DESCR:\t {}",
                        notifInfo[i].getName(),
                        notifInfo[i].getDescription()
                );
                String notifTypes[] = notifInfo[i].getNotifTypes();
                for (int j = 0; j < notifTypes.length; j++) {
                    logger.info("    TYPE:\t {}", notifTypes[j]);
                }
            }
        } else
            logger.info(" ** No notifications **");
    }
}
