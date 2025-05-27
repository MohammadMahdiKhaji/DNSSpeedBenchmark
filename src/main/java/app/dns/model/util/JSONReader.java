package app.dns.model.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public class JSONReader {
    private static Logger logger = LogManager.getLogger(JSONReader.class);
    private final static String DOMAIN_JSON_NAME = "/data/domain.json";
    private final static String DNS_JSON_NAME = "/data/dns.json";

    public JSONReader() {}

    public static String[] getDomainsByDomainName(String domainName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = JSONReader.class.getResourceAsStream(DOMAIN_JSON_NAME);
            JsonNode domainsJson = mapper.readTree(inputStream);
            JsonNode domains = domainsJson
                    .path("domain")
                    .path(domainName)
                    .path("domains");

            if (!domains.equals("missing node")) {
                String[] results = new String[domains.size()];
                for (int i = 0; i < domains.size(); i++) {
                    results[i] = String.valueOf(domains.get(i).textValue());
                }
                return results;
            }
        } catch (IOException e) {
            logger.error("Reading json file failed: " + e.getMessage());
        }
        return null;
    }

    public static String[] getAllDNSResolversName() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = JSONReader.class.getResourceAsStream(DNS_JSON_NAME);
            JsonNode dnsResolversJson = mapper.readTree(inputStream);
            JsonNode dnsResolvers = dnsResolversJson
                    .path("dns");

            if (!dnsResolvers.equals("missing node")) {
                Iterator<Map.Entry<String, JsonNode>> dnsResolversEntries = dnsResolvers.fields();
                String[] results = new String[dnsResolvers.size()];
                for (int i = 0; i < dnsResolvers.size(); i++) {
                    results[i] = dnsResolversEntries.next().getKey();
                }
                return results;
            }
        } catch (IOException e) {
            logger.error("Reading json file failed: " + e.getMessage());
        }
        return null;
    }

    public static String[] getAllDNSResolversAddresses() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String[] dnsResolversName = getAllDNSResolversName();
            String[] results = new String[dnsResolversName.length*2];
            int resultCounter = 0;

            if (!dnsResolversName.equals("missing node")) {
                InputStream inputStream = JSONReader.class.getResourceAsStream(DNS_JSON_NAME);
                JsonNode dnsResolversJson = mapper.readTree(inputStream);

                for (String name : dnsResolversName) {
                    JsonNode dnsResolverIPs = dnsResolversJson
                            .path("dns")
                            .path(name)
                            .path("resolver_ips");

                    if (!dnsResolverIPs.equals("missing node")) {
                        for (int i = 0; i < dnsResolverIPs.size(); i++) {
                            results[resultCounter] = String.valueOf(dnsResolverIPs.get(i).textValue());
                            resultCounter++;
                        }
                    }
                }
            }
            return results;
        } catch (IOException e) {
            logger.error("Reading json file failed: " + e.getMessage());
        }
        return null;
    }
}
