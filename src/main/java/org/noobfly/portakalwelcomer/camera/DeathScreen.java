package org.noobfly.portakalwelcomer.camera;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import org.noobfly.portakalwelcomer.util.ChatStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class DeathScreen {
    record Data(List<List<DeathText.Run>> message, List<String[]> details) {
    }

    private static final int MESSAGE_MAX_WIDTH = 300;
    private static final int MESSAGE_MAX_LINES = 2;
    private static final Map<Character, Integer> CHAR_INDEX = new HashMap<>();

    static {
        for (int i = 0; i < DeathGlyphs.TEXT_CHARS.length(); i++) {
            CHAR_INDEX.putIfAbsent(DeathGlyphs.TEXT_CHARS.charAt(i), i);
        }
    }

    private DeathScreen() {
    }

    static Data data(Component message, List<String[]> details) {
        List<List<DeathText.Run>> lines = message == null ? List.of() : wrap(DeathText.runs(message));
        return new Data(lines, details);
    }

    static Component render(Data data, int tick, int duration) {
        Pen pen = new Pen();
        pen.glyph(vignette(tick, duration));
        if (tick < 2 || tick >= duration - 4) {
            return pen.build();
        }
        TextColor body = ChatStyle.color(ChatStyle.Role.BODY);
        TextColor value = ChatStyle.color(ChatStyle.Role.VALUE);

        int slide = (int) Math.round(-10.0 * (1.0 - easeOut((tick - 2) / 7.0)));
        pen.glyph(DeathGlyphs.TITLE[slide - DeathGlyphs.TITLE_SLIDE_MIN]);
        if (tick >= 6) {
            DeathGlyphs.G divider = DeathGlyphs.DIVIDER[Math.min(tick - 6, DeathGlyphs.DIVIDER.length - 1)];
            if (divider != null) {
                pen.glyph(divider);
            }
        }

        int row = 0;
        if (tick >= 9) {
            for (List<DeathText.Run> line : data.message()) {
                int x = -(runsWidth(line) / 2);
                for (DeathText.Run run : line) {
                    x = pen.text(row, x, run.text(), run.value() ? value : body) + 1;
                }
                row++;
            }
        } else {
            row = data.message().size();
        }
        row = row == 0 ? 1 : row + 1;

        List<String[]> details = data.details();
        int labelWidth = 0;
        int valueWidth = 0;
        for (String[] line : details) {
            labelWidth = Math.max(labelWidth, width(line[0]));
            valueWidth = Math.max(valueWidth, width(line[1]));
        }
        int mid = (labelWidth - valueWidth) / 2;
        for (int i = 0; i < details.size() && row + i < DeathGlyphs.DETAIL_ROWS; i++) {
            if (tick < 15 + 3 * i) {
                break;
            }
            String[] line = details.get(i);
            pen.text(row + i, mid - 5 - width(line[0]), line[0], body);
            pen.glyphAt(DeathGlyphs.DOT[row + i], mid);
            pen.text(row + i, mid + 5, line[1], value);
        }

        if (tick >= 10) {
            int left = duration - tick;
            int seconds = Math.max(1, (left + 19) / 20);
            String before = "Yeniden doğmana ";
            String number = String.valueOf(seconds);
            String after = " saniye";
            int total = width(before) + 1 + width(number) + 1 + width(after);
            int x = -(total / 2);
            x = pen.text(DeathGlyphs.COUNTER_ROW, x, before, body) + 1;
            x = pen.text(DeathGlyphs.COUNTER_ROW, x, number, value) + 1;
            pen.text(DeathGlyphs.COUNTER_ROW, x, after, body);

            pen.glyph(DeathGlyphs.BAR_TRACK);
            int fill = (int) Math.round((double) DeathGlyphs.BAR_WIDTH * left / duration);
            int x0 = DeathGlyphs.BAR_TRACK.left();
            for (int i = 0; i < DeathGlyphs.BAR_PIECE.length; i++) {
                if (fill >= DeathGlyphs.BAR_PIECE_WIDTH[i]) {
                    pen.glyphAt(DeathGlyphs.BAR_PIECE[i], x0);
                    x0 += DeathGlyphs.BAR_PIECE_WIDTH[i];
                    fill -= DeathGlyphs.BAR_PIECE_WIDTH[i];
                }
            }
        }
        return pen.build();
    }

    private static DeathGlyphs.G vignette(int tick, int duration) {
        if (tick < DeathGlyphs.VIGNETTE_IN.length) {
            return DeathGlyphs.VIGNETTE_IN[tick];
        }
        int out = tick - (duration - DeathGlyphs.VIGNETTE_OUT.length - 1);
        if (out <= 0) {
            return DeathGlyphs.VIGNETTE;
        }
        return DeathGlyphs.VIGNETTE_OUT[Math.min(out, DeathGlyphs.VIGNETTE_OUT.length) - 1];
    }

    private static double easeOut(double x) {
        x = Math.max(0.0, Math.min(1.0, x));
        return 1.0 - Math.pow(1.0 - x, 3);
    }

    private static int advance(char c) {
        if (c == ' ') {
            return DeathGlyphs.BLANK_ADV;
        }
        Integer i = CHAR_INDEX.get(c);
        return DeathGlyphs.TEXT_ADV[i != null ? i : CHAR_INDEX.get('?')];
    }

    static int width(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            w += advance(s.charAt(i));
        }
        return s.isEmpty() ? 0 : w - 1;
    }

    private static int runsWidth(List<DeathText.Run> line) {
        int w = -1;
        for (DeathText.Run run : line) {
            w += width(run.text()) + 1;
        }
        return Math.max(0, w);
    }

    private static List<List<DeathText.Run>> wrap(List<DeathText.Run> runs) {
        List<DeathText.Run> words = new ArrayList<>();
        for (DeathText.Run run : runs) {
            String text = run.text().replace('\n', ' ');
            int start = 0;
            for (int i = 0; i <= text.length(); i++) {
                if (i == text.length() || text.charAt(i) == ' ') {
                    if (i > start) {
                        words.add(new DeathText.Run(text.substring(start, i), run.value()));
                    }
                    if (i < text.length()) {
                        words.add(new DeathText.Run(" ", run.value()));
                    }
                    start = i + 1;
                }
            }
        }
        List<List<DeathText.Run>> lines = new ArrayList<>();
        List<DeathText.Run> line = new ArrayList<>();
        int lineWidth = 0;
        for (DeathText.Run word : words) {
            boolean blank = word.text().equals(" ");
            int w = width(word.text()) + 1;
            if (!blank && lineWidth + w - 1 > MESSAGE_MAX_WIDTH && !line.isEmpty()) {
                trimEnd(line);
                lines.add(line);
                line = new ArrayList<>();
                lineWidth = 0;
            }
            if (blank && line.isEmpty()) {
                continue;
            }
            line.add(word);
            lineWidth += w;
        }
        trimEnd(line);
        if (!line.isEmpty()) {
            lines.add(line);
        }
        if (lines.size() > MESSAGE_MAX_LINES) {
            lines = new ArrayList<>(lines.subList(0, MESSAGE_MAX_LINES));
            List<DeathText.Run> last = lines.get(MESSAGE_MAX_LINES - 1);
            while (!last.isEmpty() && runsWidth(last) + width("...") + 1 > MESSAGE_MAX_WIDTH) {
                last.remove(last.size() - 1);
            }
            trimEnd(last);
            last.add(new DeathText.Run("...", false));
        }
        List<List<DeathText.Run>> merged = new ArrayList<>();
        for (List<DeathText.Run> l : lines) {
            List<DeathText.Run> m = new ArrayList<>();
            for (DeathText.Run r : l) {
                if (!m.isEmpty() && m.get(m.size() - 1).value() == r.value()) {
                    DeathText.Run prev = m.remove(m.size() - 1);
                    m.add(new DeathText.Run(prev.text() + r.text(), r.value()));
                } else {
                    m.add(r);
                }
            }
            merged.add(m);
        }
        return merged;
    }

    private static void trimEnd(List<DeathText.Run> line) {
        while (!line.isEmpty() && line.get(line.size() - 1).text().equals(" ")) {
            line.remove(line.size() - 1);
        }
    }

    private static final class Pen {
        private final TextComponent.Builder root = Component.text().shadowColor(ShadowColor.none());
        private final StringBuilder segment = new StringBuilder();
        private String font = DeathGlyphs.FONT;
        private TextColor color = NamedTextColor.WHITE;
        private int x;

        void glyph(DeathGlyphs.G g) {
            this.glyphAt(g, g.left());
        }

        void glyphAt(DeathGlyphs.G g, int left) {
            this.moveTo(left);
            this.put(DeathGlyphs.FONT, NamedTextColor.WHITE, String.valueOf(g.ch()));
            this.x += g.advance();
        }

        int text(int row, int left, String s, TextColor color) {
            String rowFont = DeathGlyphs.ROW_FONTS[row];
            this.moveTo(left);
            this.use(rowFont, color);
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == ' ') {
                    this.segment.append(' ');
                    this.x += DeathGlyphs.BLANK_ADV;
                    continue;
                }
                Integer index = CHAR_INDEX.get(c);
                if (index == null) {
                    c = '?';
                    index = CHAR_INDEX.get('?');
                }
                this.segment.append(c).append(spaces(DeathGlyphs.TEXT_ADV[index] - DeathGlyphs.TEXT_RAW[index]));
                this.x += DeathGlyphs.TEXT_ADV[index];
            }
            return left + width(s);
        }

        Component build() {
            this.moveTo(0);
            this.flush();
            return this.root.build();
        }

        private void moveTo(int target) {
            this.segment.append(spaces(target - this.x));
            this.x = target;
        }

        private void put(String font, TextColor color, String s) {
            this.use(font, color);
            this.segment.append(s);
        }

        private void use(String font, TextColor color) {
            if (!font.equals(this.font) || !Objects.equals(color, this.color)) {
                this.flush();
                this.font = font;
                this.color = color;
            }
        }

        private void flush() {
            if (!this.segment.isEmpty()) {
                this.root.append(Component.text(this.segment.toString(), this.color).font(Key.key(this.font)));
                this.segment.setLength(0);
            }
        }

        private static String spaces(int px) {
            if (px == 0) {
                return "";
            }
            String table = px > 0 ? DeathGlyphs.SPACE_FWD : DeathGlyphs.SPACE_BACK;
            int n = Math.abs(px);
            int max = DeathGlyphs.SPACE_PX[DeathGlyphs.SPACE_PX.length - 1];
            StringBuilder sb = new StringBuilder();
            while (n >= max * 2) {
                sb.append(table.charAt(DeathGlyphs.SPACE_PX.length - 1));
                n -= max;
            }
            for (int i = DeathGlyphs.SPACE_PX.length - 1; i >= 0; i--) {
                if ((n & DeathGlyphs.SPACE_PX[i]) != 0) {
                    sb.append(table.charAt(i));
                }
            }
            return sb.toString();
        }
    }
}
