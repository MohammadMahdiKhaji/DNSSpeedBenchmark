package app.dns.model.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JSONTest {

    @Test
    @Disabled
    void testReader() {
        JSONReader.getAllDNSResolversAddresses();
    }

    @Test
    void testReaderDomainName() {
        JSONReader.getDomainNames();
    }
}
