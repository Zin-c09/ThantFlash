package com.thantflash.card;

import static org.assertj.core.api.Assertions.assertThat;

import com.thantflash.card.TsvParser.Row;
import org.junit.jupiter.api.Test;

class TsvParserTest {

    @Test
    void parsesAnkiStyleTsv() {
        String tsv = """
                #separator:tab
                #html:true
                勉強\tべんきょう<br>study\tJLPT N2::Vocab
                Hello\tမင်္ဂလာပါ

                only-one-column
                \tempty front
                """;
        TsvParser.Result r = TsvParser.parse(tsv, "Default");

        assertThat(r.rows()).containsExactly(
                new Row("勉強", "べんきょう\nstudy", "JLPT N2::Vocab"),
                new Row("Hello", "မင်္ဂလာပါ", "Default"));
        assertThat(r.invalidLines()).isEqualTo(2);
    }

    @Test
    void handlesWindowsLineEndings() {
        assertThat(TsvParser.parse("a\tb\r\nc\td\r\n", "D").rows()).hasSize(2);
    }
}
