package com.thantflash.card;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Anki-style TSV the web app already accepts:
 * {@code Front<TAB>Back<TAB>Deck}, where {@code <br>} marks a line break and
 * lines starting with {@code #} are comments (Anki header directives).
 */
public final class TsvParser {

    public record Row(String front, String back, String deck) {
    }

    public record Result(List<Row> rows, int invalidLines) {
    }

    private TsvParser() {
    }

    public static Result parse(String text, String defaultDeck) {
        List<Row> rows = new ArrayList<>();
        int invalid = 0;
        for (String raw : text.split("\\r?\\n")) {
            if (raw.isBlank() || raw.startsWith("#")) continue;
            String[] cols = raw.split("\t", -1);
            if (cols.length < 2 || cols[0].isBlank() || cols[1].isBlank()) {
                invalid++;
                continue;
            }
            String deck = cols.length > 2 && !cols[2].isBlank() ? cols[2].trim() : defaultDeck;
            rows.add(new Row(unbreak(cols[0]), unbreak(cols[1]), deck));
        }
        return new Result(rows, invalid);
    }

    private static String unbreak(String s) {
        return s.replaceAll("(?i)<br\\s*/?>", "\n").trim();
    }
}
