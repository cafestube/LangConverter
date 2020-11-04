package eu.cafestube.util.langconverter;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import netscape.javascript.JSObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import com.google.common.base.Splitter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class LangConverter {

    private static final Splitter SPLITTER = Splitter.on('=').limit(2);
    private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void main(String[] args) {

        File input = new File("input");
        File output = new File("output");

        input.mkdirs();
        output.mkdirs();

        for (File file : Objects.requireNonNull(input.listFiles())) {
            if(file.isDirectory())
                continue;
            String fileCode = FilenameUtils.removeExtension(file.getName());

            try {
                HashMap<String, String> parse = parse(new FileInputStream(file));
                JsonObject convert = convert(parse);

                File newFile = new File(output, fileCode + ".json");

                if(!newFile.exists())
                    newFile.createNewFile();

                OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(newFile.toPath()), StandardCharsets.UTF_8);
                GSON.toJson(convert, writer);
                writer.close();

                System.out.println(String.format("Converted %s to %s", file.getName(), fileCode + ".json"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static JsonObject convert(HashMap<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        map.forEach(jsonObject::addProperty);
        return jsonObject;
    }

    /*
    Extracted Code from Minecraft's 1.8 language system.
     */
    public static HashMap<String, String> parse(InputStream inputStream) {
        HashMap<String, String> properties = new HashMap<>();

        try {
            for (String line : IOUtils.readLines(inputStream, StandardCharsets.UTF_8)) {
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    String[] contents = Iterables.toArray(SPLITTER.split(line), String.class);

                    if (contents != null && contents.length == 2) {
                        String s1 = contents[0];
                        String s2 = PATTERN.matcher(contents[1]).replaceAll("%$1s");
                        properties.put(s1, s2);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

}
