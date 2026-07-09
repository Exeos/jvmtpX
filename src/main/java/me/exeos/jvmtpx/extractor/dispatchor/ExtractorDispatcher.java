package me.exeos.jvmtpx.extractor.dispatchor;

import me.exeos.jvmtpx.extractor.impl.Extractor122;
import me.exeos.jvmtpx.extractor.impl.Extractor130;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class ExtractorDispatcher {

    public static Map<String, byte[]> extract(DispatcherInput input) {
        Map<String, byte[]> output = new HashMap<>();
        Map<String, long[]> platformOffsets;
        try {
            platformOffsets = switch (input.version()) {
                case V1_2_2 -> new Extractor122().extract(input.inputBytes());
                case V1_3_0 -> new Extractor130(input.pepper()).extract(input.inputBytes());
            };
        } catch (IOException e) {
            System.out.println("Failed to extract platform offsets");
            return output;
        }

        if (platformOffsets == null) {
            return output;
        }

        for (Map.Entry<String, long[]> entry : platformOffsets.entrySet()) {
            String name = entry.getKey();
            long[] entryMetaData = entry.getValue();
            int offset = (int) entryMetaData[0];
            int compressedLen = (int) entryMetaData[1];

            ByteArrayInputStream slice = new ByteArrayInputStream(input.inputBytes(), offset, compressedLen);
            try (var inflater = new InflaterInputStream(slice, new Inflater(true))) {
                output.put(name, inflater.readAllBytes());
            } catch (IOException e) {
                System.out.println("Failed to write bytes for platform: " + name);
            }
        }

        return output;
    }
}
