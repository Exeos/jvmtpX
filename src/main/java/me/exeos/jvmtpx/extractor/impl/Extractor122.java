package me.exeos.jvmtpx.extractor.impl;

import me.exeos.jvmtpx.extractor.ExtractorResult;
import me.exeos.jvmtpx.extractor.IExtractor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Extractor122 implements IExtractor {

    private static final int MAGIC = 1246909233;

    @Override
    public ExtractorResult extract(byte[] blob) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(blob);
        DataInputStream din = new DataInputStream(bin);
        if (din.readInt() != MAGIC) {
            throw new IOException("bad resource blob magic");
        }
        int count = din.readInt();
        String[] names = new String[count];
        int[] metaData = new int[count];
        for (int i = 0; i < count; ++i) {
            names[i] = din.readUTF();
            din.readInt();
            metaData[i] = din.readInt();
        }
        int dataStart = blob.length - bin.available();
        Map<String, long[]> entries = new HashMap<>(count * 2);
        int offset = dataStart;
        for (int i = 0; i < count; ++i) {
            entries.put(names[i], new long[]{offset, metaData[i]});
            offset += metaData[i];
        }

        return new ExtractorResult(blob, entries);
    }
}
