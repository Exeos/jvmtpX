package me.exeos.jvmtpx;

import me.exeos.jvmtpx.extractor.dispatcher.ExtractorDispatcher;
import me.exeos.jvmtpx.extractor.dispatcher.ExtractorDispatcherInput;
import me.exeos.jvmtpx.packer.dispatcher.PackerDispatcher;
import me.exeos.jvmtpx.packer.dispatcher.PackerDispatcherInput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Main {

    static void main(String[] args) {
        parseArgs(args).ifPresent(cli -> {
            cli.dispatcherInput().ifPresent(dispatcherInput -> {
                Map<String, byte[]> extracted = ExtractorDispatcher.extract(dispatcherInput);

                for (Map.Entry<String, byte[]> entry : extracted.entrySet()) {
                    Path outputPath = Paths.get(
                            System.getProperty("user.dir"),
                            entry.getKey().replace("/", ".")
                    );

                    try {
                        Files.write(outputPath, entry.getValue());
                        System.out.println("Extracted: " + outputPath);
                    } catch (IOException e) {
                        System.out.println("Failed to write to file: " + outputPath);
                    }
                }
            });

            cli.packerInput().ifPresent(packerInput -> {
                PackerDispatcher.dispatch(packerInput).ifPresentOrElse(output -> {
                    Path outputPath = Paths.get(
                            System.getProperty("user.dir"),
                            "jvmtpx-packed-" + packerInput.version().stringVersion + "_" + Math.abs(Arrays.hashCode(output))
                    );

                    try {
                        Files.write(outputPath, output);
                        System.out.println("Packed: " + outputPath);
                    } catch (IOException e) {
                        System.out.println("Failed to write to file: " + outputPath);
                    }
                }, () -> {
                    System.out.println("Packer didn't return output");
                });
            });

            System.out.println("Done");
        });
    }

    private static Optional<CLIInput> parseArgs(String[] args) {
        if (args.length < 3) {
            printUsage();
            return Optional.empty();
        }

        String argVersion = args[1].trim().toLowerCase().replace("v", "");
        Version version = null;
        for (Version potentialVersion : Version.values()) {
            if (potentialVersion.stringVersion.equals(argVersion)) {
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

        switch (args[0].trim().toLowerCase()) {
            case "extract", "e" -> {
                File input = new File(args[2]);
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

                byte[] pepper = null;
                if (args.length == 4) {
                    pepper = parsePepper(args[3]);
                }

                if (version.requiresPepper && pepper == null) {
                    System.out.println("Version requires pepper, but it wasn't provided");
                    return Optional.empty();
                }

                return Optional.of(new CLIInput(
                        Optional.of(new ExtractorDispatcherInput(version, inputBytes, pepper)),
                        Optional.empty()
                ));
            }
            case "pack", "p" -> {
                Map<String, byte[]> platformBinaries = new HashMap<>();
                for (int i = 2; i < args.length; i++) {
                    File input = new File(args[i]);
                    if (!input.exists()) {
                        System.out.println("Provided platform input does not exist");
                        printUsage();
                        return Optional.empty();
                    }
                    if (!input.canRead()) {
                        System.out.println("Can't read from provided platform input");
                        return Optional.empty();
                    }

                    try {
                        platformBinaries.put(input.getName().replace(".", "/"), Files.readAllBytes(input.toPath()));
                    } catch (IOException e) {
                        System.out.println("Failed to read input platform bytes: " + input.getPath());
                        return Optional.empty();
                    }
                }

                return Optional.of(new CLIInput(
                        Optional.empty(),
                        Optional.of(new PackerDispatcherInput(version, platformBinaries))
                ));
            }
            default -> {
                return Optional.empty();
            }
        }
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
        System.out.println("Usage:");
        System.out.println("java -jar jvmtpx.jar extract <version> <path/to/jvmtp.binary> [pepper]");
        System.out.println("  pepper: optional, required for some versions. Comma-separated byte[], e.g. \"10,-7,1,0\"");
        System.out.println("or");
        System.out.println("java -jar jvmtpx.jar pack <version> [<path/to/platform.binary>]...");
        System.out.println();
        System.out.println("Supported Versions:");
        for (Version version : Version.values()) {
            System.out.println("Version: " + version.stringVersion + " (" + version.intVersion + ")" + (version.requiresPepper ? ", requires pepper" : ""));
        }
    }
}
