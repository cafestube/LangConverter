package eu.cafestube.util.langconverter;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LangConverter
{
    private static final Splitter SPLITTER = Splitter.on('=').limit(2);
    private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void main(String[] args) throws IOException
    {
        final File input = new File("input");
        final File output = new File("output");

        input.mkdirs();
        output.mkdirs();

        FileUtils.copyDirectory(input, output);
        final Collection<File> files = FileUtils.listFilesAndDirs(input, FileFilterUtils.suffixFileFilter(".lang"), DirectoryFileFilter.DIRECTORY);
        files.forEach(file ->
        {
            if (!file.isDirectory())
            {
                final String fileCode = FilenameUtils.removeExtension(file.getName());
                try (final InputStream inputStream = new FileInputStream(file))
                {
                    final Map<String, String> parse = parse(inputStream);
                    final JsonObject convert = convert(parse);

                    final File newFile = new File(file.getParentFile(), fileCode + ".json");

                    if (!newFile.exists())
                        newFile.createNewFile();

                    final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(newFile.toPath()), StandardCharsets.UTF_8);
                    GSON.toJson(convert, writer);
                    writer.close();

                    System.out.printf("Converted %s to %s%n", file.getName(), newFile.getName());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                file.delete();
            }
        });
    }

    public static JsonObject convert(Map<String, String> map)
    {
        final JsonObject jsonObject = new JsonObject();
        map.forEach(jsonObject::addProperty);
        return jsonObject;
    }

    /*
    Extracted Code from Minecraft's 1.8 language system.
     */
    public static Map<String, String> parse(InputStream inputStream)
    {
        final Map<String, String> properties = new LinkedHashMap<>();

        try
        {
            for (String line : IOUtils.readLines(inputStream, StandardCharsets.UTF_8))
            {
                if (!line.isEmpty() && line.charAt(0) != '#')
                {
                    final String[] contents = Iterables.toArray(SPLITTER.split(line), String.class);

                    if (contents != null && contents.length == 2)
                    {
                        final String s1 = contents[0];
                        final String s2 = PATTERN.matcher(contents[1]).replaceAll("%$1s");
                        properties.put(s1, s2);
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return properties;
    }
}
