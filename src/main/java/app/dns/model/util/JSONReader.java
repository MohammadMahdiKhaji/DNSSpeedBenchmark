package app.dns.model.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONReader {
    private static Logger logger = LogManager.getLogger(JSONReader.class);
    private static final String DOMAIN_JSON_FILE_ADDRESS = "/data/domain.json";
    private static final String DNS_JSON_FILE_ADDRESS = "/data/dns.json";

    public JSONReader() {}

    public static String[] getDomainsByDomainName(String domainName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = JSONReader.class.getResourceAsStream(DOMAIN_JSON_FILE_ADDRESS);
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

    public static String[] getDomainNames() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = JSONReader.class.getResourceAsStream(DOMAIN_JSON_FILE_ADDRESS);
            JsonNode domainsJson = mapper.readTree(inputStream);
            Iterator<Map.Entry<String, JsonNode>> entryIterator =
                    domainsJson.path("domain").fields();

            int size = domainsJson.path("domain").size();
            String[] results = new String[size];
            for (int i = 0; i < size; i++) {
                if (entryIterator.hasNext()) {
                        results[i] = String.valueOf(entryIterator.next().getKey());
                }
            }
            return results;
        } catch (IOException e) {
            logger.error("Reading json file failed: " + e.getMessage());
        }
        return null;
    }

    public static String[] getAllDNSResolversName() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = JSONReader.class.getResourceAsStream(DNS_JSON_FILE_ADDRESS);
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
            List<String> results = new ArrayList<>();
            int resultCounter = 0;

            if (!dnsResolversName.equals("missing node")) {
                InputStream inputStream = JSONReader.class.getResourceAsStream(DNS_JSON_FILE_ADDRESS);
                JsonNode dnsResolversJson = mapper.readTree(inputStream);

                for (String name : dnsResolversName) {
                    JsonNode dnsResolverIPs = dnsResolversJson
                            .path("dns")
                            .path(name)
                            .path("resolver_ips");

                    if (!dnsResolverIPs.equals("missing node")) {
                        for (int i = 0; i < dnsResolverIPs.size(); i++) {
                            if (dnsResolverIPs.get(i) != null && !dnsResolverIPs.get(i).textValue().equals("")) {
                                results.add(dnsResolverIPs.get(i).textValue());
                                resultCounter++;
                            }
                        }
                    }
                }
            }
            return results.toArray(new String[0]);
        } catch (IOException e) {
            logger.error("Reading json file failed: " + e.getMessage());
        }
        return null;
    }
}
