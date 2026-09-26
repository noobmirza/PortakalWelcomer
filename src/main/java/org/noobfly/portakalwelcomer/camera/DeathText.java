package org.noobfly.portakalwelcomer.camera;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.Translatable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class DeathText {
    record Run(String text, boolean value) {
    }

    private static final Map<String, String> LANG = load();

    private DeathText() {
    }

    private static Map<String, String> load() {
        try (InputStream in = DeathText.class.getResourceAsStream("/olum-dil.json")) {
            if (in == null) {
                return Map.of();
            }
            Map<String, String> map = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8),
                    new TypeToken<Map<String, String>>() { }.getType());
            return map != null ? map : Map.of();
        } catch (Exception e) {
            return Map.of();
        }
    }

    static List<Run> runs(Component message) {
        List<Run> out = new ArrayList<>();
        flatten(message, false, out, 0);
        return out;
    }

    private static void flatten(Component component, boolean value, List<Run> out, int depth) {
        if (depth > 16) {
            return;
        }
        if (component instanceof TextComponent text) {
            add(out, text.content(), value);
        } else if (component instanceof TranslatableComponent translatable) {
            String pattern = LANG.get(translatable.key());
            if (pattern == null) {
                pattern = translatable.fallback() != null ? translatable.fallback() : translatable.key();
            }
            format(pattern, translatable, value, out, depth);
        } else if (component instanceof Translatable translatable) {
            add(out, LANG.getOrDefault(translatable.translationKey(), ""), value);
        }
        for (Component child : component.children()) {
            flatten(child, value, out, depth + 1);
        }
    }

    private static void format(String pattern, TranslatableComponent translatable, boolean value, List<Run> out, int depth) {
        int next = 0;
        StringBuilder literal = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c != '%' || i + 1 >= pattern.length()) {
                literal.append(c);
                continue;
            }
            if (pattern.charAt(i + 1) == '%') {
                literal.append('%');
                i++;
                continue;
            }
            int j = i + 1;
            int index = -1;
            while (j < pattern.length() && Character.isDigit(pattern.charAt(j))) {
                j++;
            }
            if (j < pattern.length() && pattern.charAt(j) == '$' && j > i + 1) {
                index = Integer.parseInt(pattern.substring(i + 1, j)) - 1;
                j++;
            }
            if (j >= pattern.length() || (pattern.charAt(j) != 's' && pattern.charAt(j) != 'd')) {
                literal.append(c);
                continue;
            }
            if (index < 0) {
                index = next++;
            }
            add(out, literal.toString(), value);
            literal.setLength(0);
            if (index < translatable.arguments().size()) {
                flatten(translatable.arguments().get(index).asComponent(), true, out, depth + 1);
            }
            i = j;
        }
        add(out, literal.toString(), value);
    }

    private static void add(List<Run> out, String text, boolean value) {
        if (text.isEmpty()) {
            return;
        }
        if (!out.isEmpty() && out.get(out.size() - 1).value() == value) {
            Run last = out.remove(out.size() - 1);
            out.add(new Run(last.text() + text, value));
        } else {
            out.add(new Run(text, value));
        }
    }
}
