package com.thantzin.thantflash.card;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.thantzin.thantflash.card.TsvParser.Row;

class TsvParserTest {

    @Test
    void parsesRowsCommentsAndLineBreaks() {
        String tsv = """
                # JLPT N2 kanji
                締切\tしめきり<br>deadline\tN2 Kanji
                検討\tけんとう

                \tno front
                """;
        List<Row> rows = TsvParser.parse(tsv, "Default");
        assertThat(rows).containsExactly(
                new Row("締切", "しめきり\ndeadline", "N2 Kanji"),
                new Row("検討", "けんとう", "Default"));
    }

    @Test
    void handlesWindowsLineEndings() {
        assertThat(TsvParser.parse("a\tb\r\nc\td\r\n", "X")).hasSize(2);
    }
}
