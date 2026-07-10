package me.exeos.jvmtpx.packer.impl;

import me.exeos.jvmtpx.helper.ByteHelper;
import me.exeos.jvmtpx.packer.IPacker;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Packer130 implements IPacker {

    private final byte[] pepper;

    public Packer130(byte[] pepper) {
        this.pepper = pepper;
    }

    @Override
    public byte[] pack(Map<String, byte[]> platformBinaries) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        // header containing salt, for decrypting the blob
        byte[] salt = new byte[8];
        byte[] key = ByteHelper.deriveKey(salt, pepper);
        dos.write(salt);

        // write actual blob, every thing from this point on is lated encrypted with @key
        dos.write(ByteHelper.checksum(key)); // key checksum
        dos.writeByte(0); // ignored
        dos.writeShort(platformBinaries.size());
        ByteHelper.writePad(dos);

        Map<String, byte[]> deflatedInputs = Packer122.deflateInput(platformBinaries);
        for (String name : deflatedInputs.keySet()) {
            // platform binary name
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            dos.writeShort(nameBytes.length);
            dos.write(nameBytes);

            // ignored
            dos.writeInt(0);

            // compressed data length
            dos.writeInt(deflatedInputs.get(name).length);

            ByteHelper.writePad(dos);
        }

        // write compressed platform bytes
        for (byte[] bytes : deflatedInputs.values()) {
            dos.write(bytes);
        }

        // encrypt blob with key
        byte[] encrypted = bos.toByteArray();
        ByteHelper.xorKeystream(encrypted, salt.length, encrypted.length - salt.length, key);
        return encrypted;
    }
}
