package app.dns.model.util.properties;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PropertyTest {

    @Test
    void testloading() throws IOException {
        Configs c = new Configs();
        c.loadValues();
        System.out.println(c.getThreadPoolSize());
        System.out.println(c.getPingTimeout());
        System.out.println(c.getPingPacketCount());
        System.out.println(c.getReachabilityTimeout());
    }
}
