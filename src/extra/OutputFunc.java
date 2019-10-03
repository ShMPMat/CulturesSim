package extra;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Functions which help with output.
 */
public class OutputFunc {
    /**
     * Adds right text to the left text. right text will be lined up with
     * consideration to longest line from the left text.
     *
     * @param left  left text. Must have at least one line in it.
     * @param right right text.
     * @return left and right text merged in one.
     */
    public static StringBuilder addToRight(String left, String right) {
        StringBuilder stringBuilder = new StringBuilder();
        int m = left.lines().map(String::length).max(Integer::compareTo).get() + 4;
        List<String> list1 = left.lines().collect(Collectors.toList()), list2 = right.lines()
                .collect(Collectors.toList());
        for (int i = 0; i < Math.max(left.lines().count(), right.lines().count()); i++) {
            String ss1 = (i < list1.size() ? list1.get(i) : "");
            String ss2 = (i < list2.size() ? list2.get(i) : "");
            stringBuilder.append(ss1).append(new String(new char[m - ss1.length()]).replace('\0', ' '))
                    .append(ss2).append("\n");
        }
        return stringBuilder;
    }

    public static StringBuilder chompToSize(String string, int size) {//TODO find spaces
        StringBuilder stringBuilder = new StringBuilder();
        for (String line: string.lines().collect(Collectors.toList())) {
            while (!line.isEmpty()) {
                if (size >= line.length()) {
                    stringBuilder.append(line).append("\n");
                    break;
                } else {
                    stringBuilder.append(line, 0, size).append("\n");
                    line = line.substring(size);
                }
            }
        }
        return stringBuilder;
    }

    public static StringBuilder chompToLines(String string, int size) {
        //TODO
        return null;
    }
}
