package org.noobfly.portakalwelcomer.util;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Portakal sohbet stili. Tüm Portakal pluginlerinde aynı dosya bulunur (paket adı dışında birebir aynı).
 * Renkler ve semboller tek kaynaktan gelir: plugins/portakalhub/chat-style.yml (yoksa aşağıdaki varsayılanlar).
 * Stil değişince sadece o yml değişir, pluginler yeniden başlayınca yeni renkleri kullanır.
 *
 * Kullanım:
 *   player.sendMessage(ChatStyle.error("Oyuncu bulunamadı: {}", name));
 *   player.sendMessage(ChatStyle.usage("Kullanım: <c>/takım davet</c> <v><oyuncu></v>"));
 *   Bukkit.broadcast(ChatStyle.broadcast("<v>{}</v> etkinliği kazandı!", winner));
 *   player.showTitle(Title.title(ChatStyle.title(ChatStyle.Kind.SUCCESS, "Tebrikler!"), ChatStyle.subtitle("Ödülün verildi.")));
 *
 * Şablon: gövde düz yazılır. Roller: <v>değer</v> <c>/komut</c> <m>para</m> <o>kabuk</o> <k>tıklanabilir</k> <d>ikincil</d>
 * <a>marka</a> <t>gövde</t> <b>kalın</b>. {} sıradaki argümandır: etiket içindeyse o rolde, dışındaysa değer (beyaz)
 * rolünde yazılır. Component argümanı kendi rengi varsa korunur. Satır sonu \n.
 * Türler: success (yeşil ■), error (kırmızı ■), warn (sarı ■), usage (sarı ■), info (gri ■), plain (işaretsiz),
 * broadcast ("Duyuru »" etiketi; şablon "<p=Etkinlik>..." ile başlarsa o etiket).
 */
public final class ChatStyle {

    public enum Role { BODY, VALUE, COMMAND, MONEY, KABUK, CLICK, DIM, BRAND, SUCCESS, ERROR, WARN, INFO }

    public enum Kind { SUCCESS, ERROR, WARN, USAGE, INFO, PLAIN, BROADCAST }

    private static final Map<Role, TextColor> COLORS = new EnumMap<>(Role.class);
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static String bullet = "■";
    private static String separator = "»";
    private static String prefixText = "Duyuru";

    static {
        COLORS.put(Role.BODY, TextColor.color(0xAAAAAA));
        COLORS.put(Role.VALUE, TextColor.color(0xFFFFFF));
        COLORS.put(Role.COMMAND, TextColor.color(0xFFFF55));
        COLORS.put(Role.MONEY, TextColor.color(0x55FF55));
        COLORS.put(Role.KABUK, TextColor.color(0xFFAA00));
        COLORS.put(Role.CLICK, TextColor.color(0x55FFFF));
        COLORS.put(Role.DIM, TextColor.color(0x555555));
        COLORS.put(Role.BRAND, TextColor.color(0xFF8C1A));
        COLORS.put(Role.SUCCESS, TextColor.color(0x55FF55));
        COLORS.put(Role.ERROR, TextColor.color(0xFF5555));
        COLORS.put(Role.WARN, TextColor.color(0xFFFF55));
        COLORS.put(Role.INFO, TextColor.color(0xAAAAAA));
        reload();
    }

    private ChatStyle() {
    }

    /** plugins/portakalhub/chat-style.yml dosyasını (varsa) yeniden okur. */
    public static void reload() {
        try {
            File file = new File(Bukkit.getPluginsFolder(), "portakalhub/chat-style.yml");
            if (!file.isFile()) {
                return;
            }
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            for (Role role : Role.values()) {
                String hex = yml.getString("roles." + role.name().toLowerCase() + ".hex");
                TextColor color = hex == null ? null : TextColor.fromHexString(hex.trim());
                if (color != null) {
                    COLORS.put(role, color);
                }
            }
            bullet = yml.getString("symbols.bullet", bullet);
            separator = yml.getString("symbols.separator", separator);
            prefixText = yml.getString("prefix.text", prefixText);
        } catch (Throwable ignored) {
            // yml bozuksa varsayılan renkler kullanılır
        }
    }

    public static TextColor color(Role role) {
        return COLORS.get(role);
    }

    public static Component success(String template, Object... args) {
        return render(Kind.SUCCESS, template, args);
    }

    public static Component error(String template, Object... args) {
        return render(Kind.ERROR, template, args);
    }

    public static Component warn(String template, Object... args) {
        return render(Kind.WARN, template, args);
    }

    public static Component usage(String template, Object... args) {
        return render(Kind.USAGE, template, args);
    }

    public static Component info(String template, Object... args) {
        return render(Kind.INFO, template, args);
    }

    public static Component plain(String template, Object... args) {
        return render(Kind.PLAIN, template, args);
    }

    public static Component broadcast(String template, Object... args) {
        return render(Kind.BROADCAST, template, args);
    }

    /** Title ana metni: türün renginde ve kalın (bilgi türü marka renginde). */
    public static Component title(Kind kind, String template, Object... args) {
        Role role = switch (kind) {
            case SUCCESS -> Role.SUCCESS;
            case ERROR -> Role.ERROR;
            case WARN, USAGE -> Role.WARN;
            default -> Role.BRAND;
        };
        return line(template, args, new int[] {0}, role).decoration(TextDecoration.BOLD, true);
    }

    /** Title alt metni: gri gövde, değerler beyaz. */
    public static Component subtitle(String template, Object... args) {
        return line(template, args, new int[] {0}, Role.BODY);
    }

    /**
     * Action bar ve benzeri sohbet dışı yüzeyler: ■ işareti yok, metin türün renginde (bilgi gri),
     * değerler beyaz. Tek satır.
     */
    public static Component bar(Kind kind, String template, Object... args) {
        Role role = switch (kind) {
            case SUCCESS -> Role.SUCCESS;
            case ERROR -> Role.ERROR;
            case WARN, USAGE -> Role.WARN;
            default -> Role.BODY;
        };
        return line(template.replace("\n", " "), args, new int[] {0}, role, true);
    }

    /**
     * Para (TL): TCMB kuralıyla ₺ rakamın solunda ve bitişik ("₺1.000", "₺12,5"), binlik ayraç nokta, para renginde (açık yeşil).
     * İkon burada eklenir; çağıran taraf ₺ ya da TL yazmaz. Kabuk gibi başka para birimleri için kullanılmaz.
     */
    public static Component money(Number amount) {
        return Component.text(moneyText(amount), COLORS.get(Role.MONEY));
    }

    /** money() ile aynı biçim, renksiz düz metin ("₺1.000"). */
    public static String moneyText(Number amount) {
        java.text.DecimalFormat f = new java.text.DecimalFormat("#,##0.##",
                java.text.DecimalFormatSymbols.getInstance(java.util.Locale.forLanguageTag("tr-TR")));
        f.setRoundingMode(java.math.RoundingMode.HALF_UP);
        return "₺" + f.format(amount == null ? 0 : amount.doubleValue());
    }

    /** Kabuk (ikinci para birimi): "1.000 Kabuk", binlik ayraç nokta, turuncu. ₺ kullanılmaz. */
    public static Component kabuk(Number amount) {
        java.text.DecimalFormat f = new java.text.DecimalFormat("#,##0.##",
                java.text.DecimalFormatSymbols.getInstance(java.util.Locale.forLanguageTag("tr-TR")));
        return Component.text(f.format(amount == null ? 0 : amount.doubleValue()) + " Kabuk", COLORS.get(Role.KABUK));
    }

    /** Tıklanabilir metin (açık mavi). clickEvent/hoverEvent çağıran taraf ekler. */
    public static Component clickable(String text) {
        return Component.text(text, COLORS.get(Role.CLICK));
    }

    /** String bekleyen eski API'ler için (§ kodlu metin). */
    public static String legacy(Component component) {
        return SECTION.serialize(component);
    }

    public static Component render(Kind kind, String template, Object... args) {
        TextComponent.Builder out = Component.text();
        // duyuru etiketi: "<p=Etkinlik>metin" -> "Etkinlik » metin" (yoksa chat-style.yml prefix.text)
        String label = prefixText;
        if (kind == Kind.BROADCAST && template.startsWith("<p=")) {
            int end = template.indexOf('>');
            if (end > 3) {
                label = template.substring(3, end);
                template = template.substring(end + 1);
            }
        }
        String[] lines = template.split("\n", -1);
        // işaret/etiket ilk dolu satıra konur: "\nParkur etkinliği..." gibi boş satırla başlayan blok mesajlarda
        // ■ tek başına boş satırda kalmasın. Satırın girintisi işaretten önce korunur ("   ■ ARENA: ...").
        int headLine = 0;
        while (headLine < lines.length - 1 && lines[headLine].isBlank()) {
            headLine++;
        }
        int[] argIndex = {0};
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                out.append(Component.newline());
            }
            String text = lines[i];
            if (i == headLine) {
                int indent = 0;
                while (indent < text.length() && text.charAt(indent) == ' ') {
                    indent++;
                }
                if (indent > 0) {
                    out.append(Component.text(text.substring(0, indent)));
                    text = text.substring(indent);
                }
                out.append(head(kind, label));
            }
            out.append(line(text, args, argIndex, Role.BODY));
        }
        return out.build();
    }

    private static Component head(Kind kind, String label) {
        return switch (kind) {
            case PLAIN -> Component.empty();
            case BROADCAST -> Component.text(label, COLORS.get(Role.BRAND))
                    .append(Component.text(" " + separator + " ", COLORS.get(Role.DIM)));
            case SUCCESS -> Component.text(bullet + " ", COLORS.get(Role.SUCCESS));
            case ERROR -> Component.text(bullet + " ", COLORS.get(Role.ERROR));
            case WARN, USAGE -> Component.text(bullet + " ", COLORS.get(Role.WARN));
            case INFO -> Component.text(bullet + " ", COLORS.get(Role.INFO));
        };
    }

    private static Role tagRole(char tag) {
        return switch (tag) {
            case 'v' -> Role.VALUE;
            case 'c' -> Role.COMMAND;
            case 'm' -> Role.MONEY;
            case 'o' -> Role.KABUK;
            case 'k' -> Role.CLICK;
            case 'd' -> Role.DIM;
            case 'a' -> Role.BRAND;
            case 't' -> Role.BODY;
            default -> null;
        };
    }

    private static Component line(String text, Object[] args, int[] argIndex, Role base) {
        return line(text, args, argIndex, base, base == Role.BODY);
    }

    /** valueArgs: etiket dışındaki {} argümanları değer (beyaz) rolünde mi. */
    private static Component line(String text, Object[] args, int[] argIndex, Role base, boolean valueArgs) {
        TextComponent.Builder out = Component.text();
        Deque<Role> stack = new ArrayDeque<>();
        Role current = base;
        boolean bold = false;
        StringBuilder buf = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char ch = text.charAt(i);
            if (ch == '<' && i + 2 < text.length()) {
                boolean close = text.charAt(i + 1) == '/';
                int t = close ? i + 2 : i + 1;
                if (t + 1 < text.length() && text.charAt(t + 1) == '>') {
                    char tag = text.charAt(t);
                    Role role = tagRole(tag);
                    if (role != null || tag == 'b') {
                        flush(out, buf, current, bold);
                        if (tag == 'b') {
                            bold = !close;
                        } else if (close) {
                            current = stack.isEmpty() ? base : stack.pop();
                        } else {
                            stack.push(current);
                            current = role;
                        }
                        i = t + 2;
                        continue;
                    }
                }
            }
            if (ch == '{' && i + 1 < text.length() && text.charAt(i + 1) == '}') {
                flush(out, buf, current, bold);
                Object arg = argIndex[0] < args.length ? args[argIndex[0]] : "";
                argIndex[0]++;
                // etiket dışındaki {} değer rolünde; <t>{}</t> gibi açık etiket içindeyse o etiketin rolünde
                Role role = stack.isEmpty() && valueArgs ? Role.VALUE : current;
                Component c = argument(arg, role);
                out.append(bold ? c.decoration(TextDecoration.BOLD, true) : c);
                i += 2;
                continue;
            }
            buf.append(ch);
            i++;
        }
        flush(out, buf, current, bold);
        return out.build();
    }

    private static void flush(TextComponent.Builder out, StringBuilder buf, Role role, boolean bold) {
        if (buf.length() == 0) {
            return;
        }
        Component c = Component.text(buf.toString(), COLORS.get(role));
        out.append(bold ? c.decoration(TextDecoration.BOLD, true) : c);
        buf.setLength(0);
    }

    private static Component argument(Object arg, Role role) {
        TextColor color = COLORS.get(role);
        if (arg instanceof Component component) {
            return component.colorIfAbsent(color);
        }
        String s = String.valueOf(arg);
        if (s.indexOf('§') >= 0) {
            return SECTION.deserialize(s).colorIfAbsent(color);
        }
        if (s.matches(".*&[0-9a-fk-orA-FK-OR].*")) {
            return AMPERSAND.deserialize(s).colorIfAbsent(color);
        }
        return Component.text(s, color);
    }
}
