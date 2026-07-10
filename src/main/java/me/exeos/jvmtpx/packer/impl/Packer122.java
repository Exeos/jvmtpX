package me.exeos.jvmtpx.packer.impl;

import me.exeos.jvmtpx.packer.IPacker;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class Packer122 implements IPacker {

    private static final int MAGIC = 1246909233;

    @Override
    public byte[] pack(Map<String, byte[]> inputs) throws IOException {
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        dous.writeInt(MAGIC);
        dous.writeInt(inputs.size());

        Map<String, byte[]> deflatedInputs = deflateInput(inputs);

        // write metadata for each platform
        for (String name : inputs.keySet()) {
            dous.writeUTF(name);
            dous.writeInt(0);
            dous.writeInt(deflatedInputs.get(name).length);
        }

        // write compressed platform bytes
        for (byte[] bytes : deflatedInputs.values()) {
            dous.write(bytes);
        }

        return bous.toByteArray();
    }

    private Map<String, byte[]> deflateInput(Map<String, byte[]> input) throws IOException {
        Map<String, byte[]> deflatedInput = new LinkedHashMap<>();
        for (String key : input.keySet()) {
            ByteArrayOutputStream dBos = new ByteArrayOutputStream();
            try (var deflator = new DeflaterOutputStream(dBos, new Deflater(Deflater.DEFAULT_COMPRESSION, true))) {
                deflator.write(input.get(key));
            }

            deflatedInput.put(key, dBos.toByteArray());
        }

        return deflatedInput;
    }
}
