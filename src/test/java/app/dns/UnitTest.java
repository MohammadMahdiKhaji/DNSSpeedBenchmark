package app.dns;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class UnitTest {

    @Test
    @Disabled
    void tstLookup() throws TextParseException, UnknownHostException {
        Resolver resolver = new SimpleResolver("10.202.10.10");
        Lookup lookup = new Lookup("update.discord.com", Type.A, DClass.IN);
//        lookup.setDefaultCache(null, DClass.IN);
//        lookup.setCache(null);
        lookup.setResolver(resolver);
        lookup.run();
        if (lookup.getResult() == Lookup.SUCCESSFUL) {
            System.out.println(lookup.getAnswers()[0].rdataToString());
        } else {
            System.out.println("It failed");
        }
    }

    @Test
    @Disabled
    void testSplit() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
        Properties properties = new Properties();
        properties.load(fileInputStream);

        String dnsResolvers = properties.getProperty("DNSResovler.resolvers");
        if (dnsResolvers == null || dnsResolvers.isBlank()) {
            System.out.println("No DNSResovler resolvers found.");
            return;
        }

        String[] dnsArray = Arrays.stream(dnsResolvers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        for (String dns : dnsArray) {
            String[] strings = dns.split("\\.");
            byte[] bytes = new byte[strings.length];
            for (int i = 0; i < strings.length; i++) {
                try {
                    int val = Integer.parseInt(strings[i]);
                    if (val < 0 || val > 255) {
                        throw new NumberFormatException("Out of range");
                    }
                    bytes[i] = (byte) val; // cast safely
                    System.out.print(bytes[i] + " "); // print unsigned value
                } catch (NumberFormatException e) {
                    System.err.println("Invalid byte value in DNSResovler address: " + strings[i]);
                }
            }
            System.out.println();
        }
    }

    @Test
    @Disabled
    void testAsync() throws TextParseException, UnknownHostException, ExecutionException, InterruptedException {
        Record queryRecord = Record.newRecord(Name.fromString("ea.com."), Type.A, DClass.IN);
        Message queryMessage = Message.newQuery(queryRecord);
        Resolver r = new SimpleResolver("8.8.8.8");
//        Instant start = Instant.now();
        r.sendAsync(queryMessage)
                .whenComplete(
                        (answer, ex) -> {
                            if (ex == null) {
                                System.out.println(answer);
                            } else {
                                ex.printStackTrace();
                            }
                        })
                .toCompletableFuture()
                .get();
//        Instant end = Instant.now();
//        long durationMs = Duration.between(start, end).toMillis();


        SimpleResolver resolver = new SimpleResolver("8.8.8.8");
        Lookup lookup = new Lookup("ea.com", Type.A);
        lookup.setResolver(resolver);
        lookup.run();
        if (lookup.getResult() == Lookup.SUCCESSFUL) {
            System.out.println(lookup.getAnswers()[0].rdataToString());
        }
    }

    @Test
    @Disabled
    void networkInterface() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface nif = interfaces.nextElement();
            System.out.println("Name: " + nif.getName());
            System.out.println("Display Name: " + nif.getDisplayName());
            System.out.println("Is Up: " + nif.isUp());
            System.out.println("Is Loopback: " + nif.isLoopback());
            System.out.println("Supports Multicast: " + nif.supportsMulticast());
            System.out.println("----------------------------------");
        }
    }

    @Test
    @Disabled
    void comparePings() throws IOException {
        Resolver resolver = new SimpleResolver("8.8.8.8");
        Lookup lookup = new Lookup("news.ea.com", Type.A);
//        lookup.setDefaultResolver(resolver);
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        String temp = lookup.getAnswers()[0].rdataToString();
        ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"ping", "-n", "2", temp});
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null)
            System.out.println(line);

        String[] strings = temp.split(".");
        byte[] bytes = new byte[4];
        for (int i=0; i<strings.length; i++)
            bytes[i] = Byte.parseByte(strings[i]);
        InetAddress inetAddress = InetAddress.getByAddress(bytes);
        System.out.println("inetAddress status: " + inetAddress.isReachable(1000));
    }

    @Test
    @Disabled
    void regexTest() {

        String avg = "0.119/0.130/0.144/0.010 ms";
//        avg = avg.replaceAll("\\d.\\d/[^\\d.\\d]/\\d.\\d/\\d.\\d ms", "");
        avg = avg.replaceAll("^\\d+\\.\\d+/|(\\d+\\.\\d+)/\\d+\\.\\d+/\\d+\\.\\d+ ms$", "$1");
        System.out.println(avg);
    }

    @Test
    @Disabled
    void lambdaEventTest() {
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("hello world");
            }
        };

        ActionEvent fakeEvent = new ActionEvent();

        event.handle(fakeEvent);
    }

    @Test
    @Disabled
    void iranAccessTest() throws IOException, InterruptedException {
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/util/config.properties");
        Properties properties = new Properties();
        properties.load(fileInputStream);

        String domains = properties.getProperty("EA.target_domains");
        final String[] domainArray = Arrays.stream(domains.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        String dnsResolvers = properties.getProperty("DNSResovler.resolvers");
        if (dnsResolvers == null) return;

        String[] dnsArray = Arrays.stream(dnsResolvers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        ExecutorService subExecutor = Executors.newFixedThreadPool(domainArray.length);
        List<Callable<Void>> subtasks = new ArrayList<>();

        for (String dns : dnsArray) {
            SimpleResolver resolver = new SimpleResolver(dns);
//            ExtendedResolver extendedResolver = new ExtendedResolver();

            subtasks.add(() -> {
                for (String targetDomain : domainArray) {
                    try {
                        Lookup lookup = new Lookup(targetDomain, Type.A);
                        lookup.setResolver(resolver);
                        lookup.run();
                        String temp = lookup.getAnswers()[0].rdataToString();
                        if (lookup.getResult() == Lookup.SUCCESSFUL) {

                            String[] strings = temp.split(".");
                            byte[] bytes = new byte[4];
                            for (int i=0; i<strings.length; i++)
                                bytes[i] = Byte.parseByte(strings[i]);
                            InetAddress inetAddress = InetAddress.getByAddress(bytes);
                            InetAddress inetAddress1 = InetAddress.getByName(temp);
                            System.out.println("inetAddress using (getByAddress) status: " + inetAddress.isReachable(1000)+", DNSResovler: "+dns+", target ip: "+temp);
                            System.out.println("inetAddress using (getByName) status: " + inetAddress1.isReachable(1000)+", DNSResovler: "+dns+", target ip: "+temp);

//                            String ip = lookup.getAnswers()[0].rdataToString();
//                            String resolveArg = targetDomain + ":443:" + ip;
//
//                            ProcessBuilder processBuilder = new ProcessBuilder(
//                                    "curl", "-I", "--max-time", "10", "--resolve", resolveArg, "https://" + targetDomain
//                            );
//
//                            Process process = processBuilder.start();
//
//                            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                                    new SequenceInputStream(process.getInputStream(), process.getErrorStream())
//                            ));
//
//                            String line;
//                            while ((line = reader.readLine()) != null) {
//                                if (line.contains("403") || line.contains("451")) {
//                                    System.out.printf("Access restricted for domain: %s, DNSResovler: %s \n", targetDomain, dns);
//                                    break;
//                                }
//                            }
//
//                            // Ensure the process finishes
//                            process.waitFor(15, TimeUnit.SECONDS);
//                            process.destroy();
                        }
                    } catch (Exception e) {
                        System.err.printf("Error checking domain %s with DNSResovler %s: %s\n", targetDomain, dns, e.getMessage());
                    }
                }
                return null;
            });
        }

        subExecutor.invokeAll(subtasks);
        subExecutor.shutdown();
        subExecutor.awaitTermination(2, TimeUnit.MINUTES);
    }


    @Test
    @Disabled
    void checkDNSType() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/util/config.properties");
        Properties properties = new Properties();
        properties.load(fileInputStream);
        String domains = properties.getProperty("EA.target_domains");
//        final String[] domainArray = Arrays.stream(domains.split(","))
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .toArray(String[]::new);
        final String[] domainArray = {"news.ea.com"};
        SimpleResolver resolver1 = new SimpleResolver("8.8.8.8");
        for (String targetDomain : domainArray) {
            Lookup lookup1 = new Lookup(targetDomain, Type.A);
            lookup1.setDefaultResolver(resolver1);
            lookup1.setResolver(resolver1);
            Record[] records1 = lookup1.run();
            for (Record record : records1) {
//                System.out.println(lookup.getAnswers()[0].rdataToString());
                if (record != null) {
                    System.out.println(record);
                }
            }
        }

        SimpleResolver resolver = new SimpleResolver("10.202.10.11");
        for (String targetDomain : domainArray) {
            Lookup lookup = new Lookup(targetDomain, Type.A);
            lookup.setDefaultResolver(resolver);
            lookup.setResolver(resolver);
            Record[] records = lookup.run();
            for (Record record : records) {
//                System.out.println(lookup.getAnswers()[0].rdataToString());
                if (record != null) {
                    System.out.println(record);
                }
            }
        }
    }


    @Test
    @Disabled
    void test() throws IOException {
        String ipAddress = "159.153.204.0:8654";
        System.out.printf("requesting: %s \n", ipAddress);
        Socket socket = new Socket();
        Instant start = Instant.now();
        socket.connect(new InetSocketAddress(ipAddress.substring(0,13), Integer.parseInt(ipAddress.substring(15,ipAddress.length()-1))), 2000);
        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();
        System.out.println("IP " + ipAddress + " latency: " + durationMs + " ms");

    }


    @Test
    @Disabled
    void tmp() {
        String dns1 = "159.153.0.1";
        String dns2 = "159.153.0.2";
        String[] dnsServers = { dns1, dns2 };
        System.out.println(Arrays.toString(dnsServers));
    }

    @Test
    @Disabled
    void testJVM() throws IOException, InterruptedException {

        String dns1 = "78.157.42.100";
        String dns2 = "10.202.10.11";

        String os = System.getProperty("os.name").toLowerCase();
        String[] cmd = os.contains("win")
                ? new String[]{"ping", "-n", "2", "159.153.12.0"}
                : new String[]{"ping", "-c", "2", "159.153.12.0"};

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        process.destroy();


        String[] dnsServers = { dns1, dns2 };
        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        System.setProperty("sun.net.dns.nameservers", Arrays.toString(dnsServers));

        String os1 = System.getProperty("os.name").toLowerCase();
        String[] cmd1 = os1.contains("win")
                ? new String[]{"ping", "-n", "2", "159.153.12.0"}
                : new String[]{"ping", "-c", "2", "159.153.12.0"};

        ProcessBuilder processBuilder1 = new ProcessBuilder(cmd1);
        Process process1 = processBuilder1.start();
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));

        String line1;
        while ((line1 = reader1.readLine()) != null) {
            System.out.println(line1);
        }
        process1.destroy();
    }


    @Test
    @Disabled
    void dnsJava() throws IOException, ExecutionException, InterruptedException {

//        int count = 0;
//        int latency = 0;
        Properties properties = new Properties();


        FileInputStream fileInputStream = new FileInputStream("src/main/resources/util/config.properties");
        properties.load(fileInputStream);

        String domains = properties.getProperty("EA.target_domains");
        String dnsResolvers = properties.getProperty("DNSResovler.resolvers");

        final String[] domainArray;
        String[] dnsArray = null;
        if (domains != null) {
            domainArray = Arrays.stream(domains.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        } else {
            domainArray = null;
            System.out.println("target_ips not found in config.");
        }

        if (dnsResolvers != null) {
            dnsArray = Arrays.stream(dnsResolvers.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        } else {
            System.out.println("resolvers not found in config.");
        }

        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            Runtime.getRuntime().exec("ipconfig /flushdns");
        } else if (os.contains("mac")) {
            Runtime.getRuntime().exec("dscacheutil -flushcache");
        } else if (os.contains("nux")) {
            Runtime.getRuntime().exec("systemd-resolve --flush-caches");
        }

//        ExecutorService executor = Executors.newFixedThreadPool(dnsArray.length);
//
//        List<Callable<String>> tasks = new ArrayList<>();

        for (String dns : dnsArray) {
//            tasks.add(() -> {
                AtomicInteger count = new AtomicInteger(0);
                AtomicInteger latency = new AtomicInteger(0);

                ExecutorService subExecutor = Executors.newFixedThreadPool(domainArray.length);
                List<Callable<Void>> subtasks = new ArrayList<>();

                SimpleResolver resolver = new SimpleResolver(dns);

                for (String targetDomain : domainArray) {
                    subtasks.add(() -> {
                        Lookup lookup = new Lookup(targetDomain, Type.A);
                        lookup.setDefaultResolver(resolver);
                        lookup.setResolver(resolver);
                        lookup.run();
//                    System.out.println(targetDomain + ", " + lookup.getAnswers()[0].rdataToString());
                        if (lookup.getResult() == Lookup.SUCCESSFUL) {
                            try {

                                String[] cmd = os.contains("win")
                                        ? new String[]{"ping", "-n", "2", lookup.getAnswers()[0].rdataToString()}
                                        : new String[]{"ping", "-c", "2", lookup.getAnswers()[0].rdataToString()};

                                ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                                Process process = processBuilder.start();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (os.contains("win") && line.contains("Average =")) {
                                        int avgIndex = line.indexOf("Average =") + "Average =".length();
                                        String avg = line.substring(avgIndex).replaceAll("[^\\d]", "");
                                        latency.addAndGet(Integer.parseInt(avg));
                                    } else if (!os.contains("win") && line.contains("rtt min/avg/max")) {
                                        String stats = line.split("=")[1].trim().split(" ")[0]; // "23.232/24.321/25.111/0.222"
                                        String avg = stats.split("/")[1];
                                        latency.addAndGet((int) Math.round(Double.parseDouble(avg)));
                                    }
                                }
                                process.waitFor();
                            } catch (Exception e) {
//                            System.out.println("Ping failed: " + e.getMessage());
                            } finally {
                                count.incrementAndGet();
                            }
                        }
                        return null;
                    });
                }
                subExecutor.invokeAll(subtasks);
                subExecutor.shutdown();

                System.out.printf("DNSResovler Server: %s, success percentage: %.2f%%, avg latency: %.2f ms \n",
                        dns,
                        (double) count.get() / domainArray.length * 100,
                        count.get() == 0 ? 0 : (double) latency.get() / count.get());
//                return String.format("DNSResovler Server: %s, success percentage: %.2f%%, avg latency: %.2f ms",
//                        dns,
//                        (double) count.get() / domainArray.length * 100,
//                        count.get() == 0 ? 0 : (double) latency.get() / count.get());
//            });
        }
//        List<Future<String>> results = executor.invokeAll(tasks);
//        executor.shutdown();

//        for (Future<String> result : results) {
//            try {
//                System.out.println(result.get());
//            } catch (ExecutionException | InterruptedException e) {
//                System.err.println("Error retrieving result: " + e.getMessage());
//            }
//        }
    }
}
