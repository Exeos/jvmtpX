package me.exeos.jvmtpx.extractor.impl;

import me.exeos.jvmtpx.extractor.IExtractor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Extractor130 implements IExtractor {

    private final byte[] pepper;

    public Extractor130(byte[] pepper) {
        this.pepper = pepper;
    }

    @Override
    public Map<String, long[]> extract(byte[] blob) throws IOException {
        if (blob.length < 12) {
            throw new IOException("resource blob truncated");
        }
        byte[] dec = blob.clone();
        byte[] salt = Arrays.copyOfRange(dec, 0, 8);
        byte[] key = deriveKey(salt);
        xorKeystream(dec, 8, dec.length - 8, key);
        ByteArrayInputStream bin = new ByteArrayInputStream(dec, 8, dec.length - 8);
        DataInputStream din = new DataInputStream(bin);
        byte[] check = new byte[4];
        din.readFully(check);
        if (!Arrays.equals(check, checkTag(key))) {
            throw new IOException("bad resource blob");
        }
        din.readUnsignedByte();
        int count = din.readUnsignedShort();
        skipPad(din);
        String[] names = new String[count];
        int[] compressed = new int[count];
        for (int i = 0; i < count; ++i) {
            int nameLen = din.readUnsignedShort();
            byte[] nb = new byte[nameLen];
            din.readFully(nb);
            names[i] = new String(nb, StandardCharsets.UTF_8);
            din.readInt();
            compressed[i] = din.readInt();
            skipPad(din);
        }
        int dataStart = dec.length - bin.available();
        HashMap<String, long[]> entries = new HashMap<>(count * 2);
        int offset = dataStart;
        for (int i = 0; i < count; ++i) {
            entries.put(names[i], new long[]{offset, compressed[i]});
            offset += compressed[i];
        }

        return entries;
    }

    private byte[] deriveKey(byte[] salt) {
        byte[] in = new byte[pepper.length + salt.length];
        System.arraycopy(pepper, 0, in, 0, pepper.length);
        System.arraycopy(salt, 0, in, pepper.length, salt.length);
        return sha256(in);
    }

    private static void xorKeystream(byte[] data, int off, int len, byte[] key) {
        byte[] ctr = new byte[key.length + 4];
        System.arraycopy(key, 0, ctr, 0, key.length);
        int produced = 0;
        int j = 0;
        while (produced < len) {
            ctr[key.length] = (byte)j;
            ctr[key.length + 1] = (byte)(j >>> 8);
            ctr[key.length + 2] = (byte)(j >>> 16);
            ctr[key.length + 3] = (byte)(j >>> 24);
            byte[] block = sha256(ctr);
            int n = Math.min(block.length, len - produced);
            for (int i = 0; i < n; ++i) {
                int n2 = off + produced + i;
                data[n2] = (byte)(data[n2] ^ block[i]);
            }
            produced += n;
            ++j;
        }
    }

    private static byte[] sha256(byte[] in) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(in);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static byte[] checkTag(byte[] key) {
        byte[] h = sha256(key);
        return new byte[]{h[0], h[1], h[2], h[3]};
    }

    private static void skipPad(DataInputStream din) throws IOException {
        int n = din.readUnsignedByte();
        din.readFully(new byte[n]);
    }
}
