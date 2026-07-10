package me.exeos.jvmtpx.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ByteHelper {

    public static void xorKeystream(byte[] data, int off, int len, byte[] key) {
        byte[] ctr = new byte[key.length + 4];
        System.arraycopy(key, 0, ctr, 0, key.length);
        int produced = 0;
        int j = 0;
        while (produced < len) {
            ctr[key.length] = (byte) j;
            ctr[key.length + 1] = (byte) (j >>> 8);
            ctr[key.length + 2] = (byte) (j >>> 16);
            ctr[key.length + 3] = (byte) (j >>> 24);
            byte[] block = sha256(ctr);
            int n = Math.min(block.length, len - produced);
            for (int i = 0; i < n; ++i) {
                int n2 = off + produced + i;
                data[n2] = (byte) (data[n2] ^ block[i]);
            }
            produced += n;
            ++j;
        }
    }

    public static byte[] checksum(byte[] key) {
        byte[] h = sha256(key);
        return new byte[]{h[0], h[1], h[2], h[3]};
    }

    public static byte[] sha256(byte[] in) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(in);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static byte[] deriveKey(byte[] salt, byte[] pepper) {
        byte[] in = new byte[pepper.length + salt.length];
        System.arraycopy(pepper, 0, in, 0, pepper.length);
        System.arraycopy(salt, 0, in, pepper.length, salt.length);
        return sha256(in);
    }

    public static void skipPad(DataInputStream din) throws IOException {
        int n = din.readUnsignedByte();
        din.readFully(new byte[n]);
    }

    public static void writePad(DataOutputStream dos) throws IOException {
        dos.writeByte(1);
        dos.write(new byte[1]);
    }
}
