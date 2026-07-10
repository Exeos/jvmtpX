package me.exeos.jvmtpx.extractor.impl;

import me.exeos.jvmtpx.extractor.ExtractorResult;
import me.exeos.jvmtpx.extractor.IExtractor;
import me.exeos.jvmtpx.helper.ByteHelper;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class Extractor130 implements IExtractor {

    private final byte[] pepper;

    public Extractor130(byte[] pepper) {
        this.pepper = pepper;
    }

    @Override
    public ExtractorResult extract(byte[] blob) throws IOException {
        if (blob.length < 12) {
            throw new IOException("resource blob truncated");
        }

        byte[] dec = blob.clone();
        byte[] salt = Arrays.copyOfRange(dec, 0, 8);
        byte[] key = ByteHelper.deriveKey(salt, pepper);

        ByteHelper.xorKeystream(dec, 8, dec.length - 8, key); // decrypt blob, using key derived from salt bytes and pepper
        ByteArrayInputStream bin = new ByteArrayInputStream(dec, 8, dec.length - 8); // decrypted blob, excluding salt bytes
        DataInputStream din = new DataInputStream(bin);
        byte[] keyChecksum = new byte[4];
        din.readFully(keyChecksum);
        if (!Arrays.equals(keyChecksum, ByteHelper.checksum(key))) {
            throw new IOException("bad resource blob");
        }

        din.readUnsignedByte(); // unknown

        int count = din.readUnsignedShort(); // amount of platform binaries
        String[] names = new String[count];
        int[] compressedLength = new int[count];

        ByteHelper.skipPad(din); // unknown

        for (int i = 0; i < count; ++i) {
            int nameLen = din.readUnsignedShort();
            byte[] nameUTFBytes = new byte[nameLen];
            din.readFully(nameUTFBytes);
            names[i] = new String(nameUTFBytes, StandardCharsets.UTF_8);

            din.readInt(); // unknown
            compressedLength[i] = din.readInt();
            ByteHelper.skipPad(din); // unknown
        }

        int dataStart = dec.length - bin.available();
        HashMap<String, long[]> entries = new HashMap<>(count * 2);
        int offset = dataStart;
        for (int i = 0; i < count; ++i) {
            entries.put(names[i], new long[]{offset, compressedLength[i]});
            offset += compressedLength[i];
        }

        return new ExtractorResult(dec, entries);
    }
}
