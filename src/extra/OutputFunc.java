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
     * @param guarantiedLength if left fragment is already has equal length of lines.
     * @return left and right text merged in one.
     */
    public static StringBuilder addToRight(String left, String right, boolean guarantiedLength) {
        StringBuilder stringBuilder = new StringBuilder();
        int m = left.lines().map(String::length).max(Integer::compareTo).get() + 4;
        List<String> list1 = left.lines().collect(Collectors.toList()), list2 = right.lines()
                .collect(Collectors.toList());
        for (int i = 0; i < Math.max(left.lines().count(), right.lines().count()); i++) {
            String ss1 = (i < list1.size() ? list1.get(i) : "");
            String ss2 = (i < list2.size() ? list2.get(i) : "");
            stringBuilder.append(ss1).append(new String(new char[guarantiedLength ? 4 : m - ss1.length()])
                    .replace('\0', ' ')).append(ss2).append("\n");
        }
        return stringBuilder;
    }

    /**
     * Edits text via carrying lines which are longer than size.
     * @param text text wich will be edited.
     * @param size maximal acceptable length of a line.
     * @return edited text, each line in it is not longer than size. Words can be cut in the middle.
     */
    public static StringBuilder chompToSize(String text, int size) {//TODO find spaces
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : text.lines().collect(Collectors.toList())) {
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

    /**
     * Edits text via adding lines after size + 1 recursively to the right.
     * @param text text which will be modified.
     * @param size maximal acceptable number of lines.
     * @return edited text, has no more than size lines, greater lines added to the right.
     */
    public static StringBuilder chompToLines(String text, int size) {
        int index = 0, counter = 0;
        while (index + 1 < text.length()) {
            index = text.indexOf("\n", index + 1);
            counter++;
            if (counter == size) {
                return addToRight(text.substring(0, index),
                        chompToLines(text.substring(index + 1), size).toString(), false);
            }
        }
        return new StringBuilder(text);
    }
}
