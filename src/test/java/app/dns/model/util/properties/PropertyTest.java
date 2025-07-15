package app.dns.model.util.properties;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PropertyTest {

    @Test
    @Disabled
    void testloading() throws IOException {
        Configs.getInstance().loadValues();
        System.out.println(Configs.getThreadPoolSize());
        System.out.println(Configs.getPingTimeout());
        System.out.println(Configs.getPingPacketCount());
        System.out.println(Configs.getReachabilityTimeout());
    }
}
