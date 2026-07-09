package me.exeos.jvmtpx;

import me.exeos.jvmtpx.extractor.Version;
import me.exeos.jvmtpx.extractor.dispatchor.DispatcherInput;
import me.exeos.jvmtpx.extractor.dispatchor.ExtractorDispatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public class Main {

    static void main(String[] args) {
        parseArgs(args).ifPresent(input -> {
            Map<String, byte[]> extracted = ExtractorDispatcher.extract(input);

            for (Map.Entry<String, byte[]> entry : extracted.entrySet()) {
                Path outputPath = Paths.get(
                        System.getProperty("user.dir"),
                        entry.getKey().replace("/", ".")
                );

                try {
                    Files.write(outputPath, entry.getValue());
                } catch (IOException e) {
                    System.out.println("Failed to write to file: " + outputPath);
                }
            }
        });
    }

    private static Optional<DispatcherInput> parseArgs(String[] args) {
        if (args.length < 2 || args.length > 3) {
            printUsage();
            return Optional.empty();
        }

        File input = new File(args[0]);
        if (!input.exists()) {
            System.out.println("Provided input does not exist");
            printUsage();
            return Optional.empty();
        }
        if (!input.canRead()) {
            System.out.println("Can't read from provided input");
            return Optional.empty();
        }

        byte[] inputBytes;
        try {
            inputBytes = Files.readAllBytes(input.toPath());
        } catch (IOException e) {
            System.out.println("Failed to read input bytes");
            return Optional.empty();
        }

        String argVersion = args[1];
        Version version = null;
        for (Version potentialVersion : Version.values()) {
            if (potentialVersion.stringVersion.equals(argVersion.trim().toLowerCase().replace("v", ""))) {
                version = potentialVersion;
                break;
            }

            try {
                if (potentialVersion.intVersion == Integer.parseInt(argVersion)) {
                    version = potentialVersion;
                }
            } catch (NumberFormatException _) {
            }
        }

        if (version == null) {
            System.out.println("Failed to parse version");
            printUsage();
            return Optional.empty();
        }

        byte[] pepper = null;
        if (args.length == 3) {
            pepper = parsePepper(args[2]);
        }

        if (version.requiresPepper && pepper == null) {
            System.out.println("Version requires pepper, but it wasn't provided");
            return Optional.empty();
        }

        return Optional.of(new DispatcherInput(version, inputBytes, pepper));
    }

    private static byte[] parsePepper(String pepperArg) {
        if (pepperArg.isBlank()) {
            return null;
        }

        String[] parts = pepperArg.split(",");
        byte[] pepper = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                pepper[i] = Byte.parseByte(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid pepper byte at index " + i + ": '" + parts[i] + "'", e);
            }
        }
        return pepper;
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar jvmtpx.jar <path/to/jvmtp/binary> <version> [pepper]");
        System.out.println("  pepper: optional, required for some versions. Comma-separated byte[], e.g. \"10,-7,1,0\"");
        System.out.println();
        System.out.println("Supported Versions:");
        for (Version version : Version.values()) {
            System.out.println("Version: " + version.stringVersion + " (" + version.intVersion + ")" + (version.requiresPepper ? ", requires pepper" : ""));
        }
    }
}
