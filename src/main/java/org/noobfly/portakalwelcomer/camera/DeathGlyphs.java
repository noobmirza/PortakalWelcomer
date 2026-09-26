package org.noobfly.portakalwelcomer.camera;

final class DeathGlyphs {
    record G(char ch, int left, int advance) {
    }

    static final String FONT = "portakalkit:olum";
    static final String[] ROW_FONTS = {"portakalkit:olum_y0", "portakalkit:olum_y1", "portakalkit:olum_y2", "portakalkit:olum_y3", "portakalkit:olum_y4", "portakalkit:olum_y5", "portakalkit:olum_y6", "portakalkit:olum_y7"};
    static final int COUNTER_ROW = 7;
    static final int DETAIL_ROWS = 7;

    static final int[] SPACE_PX = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};
    static final String SPACE_FWD = "\uf800\uf801\uf802\uf803\uf804\uf805\uf806\uf807\uf808\uf809\uf80a";
    static final String SPACE_BACK = "\uf820\uf821\uf822\uf823\uf824\uf825\uf826\uf827\uf828\uf829\uf82a";

    static final G[] VIGNETTE_IN = {new G('\ue000', -480, 964), new G('\ue001', -480, 964), new G('\ue002', -480, 964), new G('\ue003', -480, 964), new G('\ue004', -480, 964), new G('\ue005', -480, 964), new G('\ue006', -480, 964), new G('\ue007', -480, 964), new G('\ue008', -480, 964), new G('\ue009', -480, 964)};
    static final G VIGNETTE = new G('\ue00a', -480, 964);
    static final G[] VIGNETTE_OUT = {new G('\ue00b', -480, 964), new G('\ue00c', -480, 964), new G('\ue00d', -480, 964), new G('\ue00e', -480, 964), new G('\ue00f', -480, 964)};
    static final G[] TITLE = {new G('\ue010', -49, 100), new G('\ue011', -49, 100), new G('\ue012', -49, 100), new G('\ue013', -49, 100), new G('\ue014', -49, 100), new G('\ue015', -49, 100), new G('\ue016', -49, 100), new G('\ue017', -49, 100), new G('\ue018', -49, 100), new G('\ue019', -49, 100), new G('\ue01a', -49, 100)};
    static final int TITLE_SLIDE_MIN = -10;
    static final G[] DIVIDER = {null, new G('\ue01b', -70, 100), new G('\ue01c', -70, 120), new G('\ue01d', -70, 132), new G('\ue01e', -70, 140), new G('\ue01f', -70, 144), new G('\ue020', -70, 144), new G('\ue021', -70, 144)};
    static final G[] DOT = {new G('\ue022', 0, 4), new G('\ue023', 0, 4), new G('\ue024', 0, 4), new G('\ue025', 0, 4), new G('\ue026', 0, 4), new G('\ue027', 0, 4), new G('\ue028', 0, 4)};
    static final G BAR_TRACK = new G('\ue029', -50, 104);
    static final int BAR_WIDTH = 100;
    static final int[] BAR_PIECE_WIDTH = {64, 32, 16, 8, 4, 2, 1};
    static final G[] BAR_PIECE = {new G('\ue02a', 0, 68), new G('\ue02b', 0, 36), new G('\ue02c', 0, 20), new G('\ue02d', 0, 12), new G('\ue02e', 0, 8), new G('\ue02f', 0, 8), new G('\ue030', 0, 4)};

    static final String TEXT_CHARS = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u00a1\u00a2\u00a3\u00a4\u00a5\u00a6\u00a7\u00a8\u00a9\u00aa\u00ab\u00ac\u00ad\u00ae\u00af\u00b0\u00b1\u00b2\u00b3\u00b4\u00b5\u00b6\u00b7\u00b9\u00ba\u00bb\u00bc\u00bd\u00be\u00bf\u00c0\u00c1\u00c2\u00c3\u00c4\u00c5\u00c6\u00c7\u00c8\u00c9\u00ca\u00cb\u00cc\u00cd\u00ce\u00cf\u00d0\u00d1\u00d2\u00d3\u00d4\u00d5\u00d6\u00d7\u00d8\u00d9\u00da\u00db\u00dc\u00dd\u00de\u00df\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u00e6\u00e7\u00e8\u00e9\u00ea\u00eb\u00ec\u00ed\u00ee\u00ef\u00f0\u00f1\u00f2\u00f3\u00f4\u00f5\u00f6\u00f7\u00f8\u00f9\u00fa\u00fb\u00fc\u00fd\u00fe\u00ff\u011e\u011f\u0130\u0131\u015e\u015f";
    static final int[] TEXT_ADV = {2, 4, 6, 6, 6, 6, 2, 4, 4, 4, 6, 2, 6, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 4, 2, 4, 7, 2, 6, 6, 8, 6, 2, 6, 4, 8, 5, 7, 6, 4, 8, 6, 5, 6, 5, 5, 3, 6, 7, 2, 4, 5, 7, 8, 8, 8, 6, 6, 6, 6, 6, 6, 6, 10, 6, 6, 6, 6, 6, 4, 4, 4, 4, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 10, 6, 6, 6, 6, 6, 3, 3, 4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 2, 6, 6};
    static final int[] TEXT_RAW = {8, 8, 12, 12, 12, 12, 8, 8, 8, 8, 12, 8, 12, 8, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 8, 8, 8, 12, 8, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 8, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 8, 12, 8, 12, 12, 8, 12, 12, 12, 12, 12, 8, 12, 12, 8, 12, 8, 8, 12, 12, 12, 12, 12, 12, 12, 8, 12, 12, 12, 12, 12, 12, 8, 8, 8, 12, 8, 12, 12, 12, 12, 8, 12, 8, 12, 8, 12, 12, 8, 12, 12, 8, 12, 8, 8, 8, 12, 12, 8, 8, 8, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 16, 12, 12, 12, 12, 12, 8, 8, 8, 8, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 16, 12, 12, 12, 12, 12, 8, 8, 8, 8, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 8, 8, 12, 12};
    static final int BLANK_ADV = 4;

    private DeathGlyphs() {
    }
}
