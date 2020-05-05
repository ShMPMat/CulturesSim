package extra;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputDatabase {
    private BufferedReader bufferedReader;
    private String lastLine;

    public InputDatabase(String path) {
        try {
            this.bufferedReader = new BufferedReader(new FileReader(path));
            lastLine = bufferedReader.readLine();
            while (lastLine.trim().isEmpty() || lastLine.charAt(0) == '/') {
                lastLine = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine() {
        if (lastLine == null) {
            return null;
        }
        StringBuilder line = new StringBuilder(lastLine.substring(1));
        while (true) {
            String newLine = null;
            try {
                newLine = bufferedReader.readLine();
            } catch (IOException e) {
                System.err.println(e.toString());
                return null;
            }
            if (newLine == null) {
                lastLine = null;
                return line.toString();
            }
            if (newLine.trim().isEmpty() || newLine.charAt(0) == '/') {
                continue;
            }
            if (newLine.charAt(0) == '-') {
                lastLine = newLine;
                return line.toString();
            }
            line.append(" ").append(newLine);
        }
    }

    public List<String> readLines() {
        List<String> lines = new ArrayList<>();
        String line = readLine();
        while (line != null) {
            lines.add(line);
            line = readLine();
        }
        return lines;
    }
}
