package me.exeos.jvmtpx;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class Main {

    private static final int MAGIC = 1246909233;

    static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("You need to provide the path to the compressed binary");
            return;
        }

        Path inputPath = Path.of(args[0]).toAbsolutePath();
        Path inputDir = inputPath.getParent();
        try {
            byte[] inFile = Files.readAllBytes(inputPath);
            Map<String, long[]> platformOffsets = new HashMap<>();
            byte[] blob = loadCompressed(inFile, platformOffsets);

            for (Map.Entry<String, long[]> entry : platformOffsets.entrySet()) {
                String name = entry.getKey();
                long[] loc = entry.getValue();

                int offset = (int)loc[0];
                int compressedLen = (int)loc[1];
                ByteArrayInputStream slice = new ByteArrayInputStream(blob, offset, compressedLen);
                var iis = new InflaterInputStream(slice, new Inflater(true));

                Path outPath = inputDir.resolve(name.replace("/", "."));
                System.out.println(outPath.toString());
                if (outPath.getParent() != null) {
                    Files.createDirectories(outPath.getParent());
                }

                try (OutputStream out = Files.newOutputStream(outPath)) {
                    iis.transferTo(out);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] loadCompressed(byte[] blob, Map<String, long[]> platformOffsets) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(blob);
        DataInputStream din = new DataInputStream(bin);
        if (din.readInt() != MAGIC) {
            throw new IOException("bad resource blob magic");
        }
        int count = din.readInt();
        String[] names = new String[count];
        int[] compressed = new int[count];
        for (int i = 0; i < count; ++i) {
            names[i] = din.readUTF();
            din.readInt();
            compressed[i] = din.readInt();
        }
        int dataStart = blob.length - bin.available();
        Map<String, long[]> entries = new HashMap<String, long[]>(count * 2);
        int offset = dataStart;
        for (int i = 0; i < count; ++i) {
            entries.put(names[i], new long[]{offset, compressed[i]});
            offset += compressed[i];
        }

        platformOffsets.putAll(entries);
        return blob;
    }
}
