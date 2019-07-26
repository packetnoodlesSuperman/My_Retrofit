package com.bob.test.sample;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class WriteFile {

    public void run() throws Exception {
        writeEnv(new File("writeFile.txt"));
    }

    public void writeEnv(File file) {
        try {
            Sink sink = Okio.sink(file);
            BufferedSink bufferedSink = Okio.buffer(sink);

            Map<String, String> map = new HashMap<>();
            map.put("1", "1");
            map.put("2", "2");
            map.put("3", "3");
            map.put("4", "4");

//            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                bufferedSink.writeUtf8(entry.getKey());
                bufferedSink.writeUtf8("=");
                bufferedSink.writeUtf8(entry.getValue());
                bufferedSink.writeUtf8("\n");
            }
            bufferedSink.flush();

        }catch (Exception e) {}

    }

    public static void main(String... args) throws Exception {
        new WriteFile().run();
    }
}
