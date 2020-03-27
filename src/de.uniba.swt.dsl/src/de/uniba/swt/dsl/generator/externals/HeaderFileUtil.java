package de.uniba.swt.dsl.generator.externals;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HeaderFileUtil {

    private static Logger logger = Logger.getLogger(HeaderFileUtil.class);

    public static void updateThreadStatus(String folderPath, String filename, String oldPrefix, String oldSuffix, String newName) {
        var oldName = oldPrefix + oldSuffix;

        // find the typedef enum and remove
        try {
            var path = Paths.get(folderPath, filename);
            var lines = Files.readAllLines(path);
            var end = findEnumNameLine(oldName, lines);
            if (end >= 0) {
                var start = findTypeDefLine(lines, end);
                if (start >= 0) {
                    // remove
                    var newLines = removeAndReplaceLines(lines, start, end, oldName, newName);

                    // write to file
                    Files.write(path, newLines);
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to update thread status in header file", e);
        }
    }

    private static List<String> removeAndReplaceLines(List<String> lines, int start, int end, String oldName, String newName) {
        List<String> newLines = new ArrayList<>();
        newLines.add("#include \"tick_wrapper.h\"");
        for (int i = 0; i < lines.size(); i++) {
            if (i >= start && i<= end) {
                continue;
            }

            // replace
            newLines.add(lines.get(i).replace(oldName, newName));
        }

        return newLines;
    }

    private static int findTypeDefLine(List<String> lines, int end) {
        var regex = "^\\s*typedef\\s*enum\\s*";
        var pattern = Pattern.compile(regex);
        for (int i = end; i >= 0; i--) {
            if (pattern.matcher(lines.get(i)).find()) {
                // remove comment if needed
                if (i - 1 >= 0 && lines.get(i - 1).startsWith("//")) {
                    return i -1;
                }

                return i;
            }
        }

        return -1;
    }

    private static int findEnumNameLine(String name, List<String> lines) {
        var regex = String.format("^\\s*}\\s*%s\\s*;", name);
        var pattern = Pattern.compile(regex);
        for (int i = 0; i < lines.size(); i++) {
            if (pattern.matcher(lines.get(i)).find()) {
                return i;
            }
        }

        return -1;
    }
}
