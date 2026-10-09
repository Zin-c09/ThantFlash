package com.thantzin.thantflash.card;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses Anki-style TSV: {@code Front<TAB>Back<TAB>Deck}. Same rules as the web app's parseTsv():
 * blank lines and lines starting with '#' are skipped, {@code <br>} becomes a newline,
 * and the Deck column is optional.
 */
public final class TsvParser {

    public record Row(String front, String back, String deck) {
    }

    private TsvParser() {
    }

    public static List<Row> parse(String text, String fallbackDeck) {
        List<Row> rows = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] f = line.split("\t", -1);
            String front = field(f, 0);
            String back = field(f, 1);
            String deck = field(f, 2);
            if (front.isEmpty() || back.isEmpty()) {
                continue;
            }
            rows.add(new Row(front, back, deck.isEmpty() ? fallbackDeck : deck));
        }
        return rows;
    }

    private static String field(String[] fields, int i) {
        return i < fields.length ? fields[i].replaceAll("(?i)<br\\s*/?>", "\n").trim() : "";
    }
}
