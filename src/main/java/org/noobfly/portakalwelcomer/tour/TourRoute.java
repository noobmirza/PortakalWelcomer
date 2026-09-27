package org.noobfly.portakalwelcomer.tour;

import org.bukkit.util.Vector;

import java.util.List;

final class TourRoute {
    static final String WORLD = "Spawn";

    private static final String GUIDE = "Rehber";
    private static final String GUIDE_PORTRAIT = "rehber";

    private static final Vector PLAZA = new Vector(503.5, 111.5, 568.5);

    private TourRoute() {
    }

    static List<TourStop> stops() {
        return List.of(
                TourStop.dolly(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Portakal'a hoş geldin! Seni kısaca sunucuda gezdireceğim.",
                                "Bu tabelada en çok işine yarayacak komutlar var: {/kurallar} ile kuralları öğren, {/bilgi} ile bilgi al, {/wiki} ile wikiye ulaş.",
                                "Kuralları mutlaka oku, bilmemek mazeret sayılmaz."),
                        new Vector(503.5, 117.62, 586.5), new Vector(503.5, 119.0, 582.5), 4.0, 1.0, 120, null)
                        .waitForShift(),

                TourStop.glide(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Burası {kasalar} alanı. Kasalar eşya vermez, ekipmanlarına {kostüm görünümü} kazandırır.",
                                "Kasaların arkasındaki mankenler, o kasadan çıkabilecek kostümleri giyiyor. Kostümlerini {/skinler} ile yönetebilirsin."),
                        List.of(
                                new Vector(496.5, 113.3, 564.5),
                                new Vector(495.5, 113.0, 556.0),
                                new Vector(496.0, 113.0, 547.0),
                                new Vector(500.0, 113.0, 543.5),
                                new Vector(506.5, 113.0, 543.0),
                                new Vector(508.0, 113.0, 547.0),
                                new Vector(508.5, 113.2, 556.0)),
                        List.of(
                                new Vector(488.3, 112.3, 562.5),
                                new Vector(487.5, 112.3, 555.5),
                                new Vector(489.5, 112.3, 546.0),
                                new Vector(494.0, 112.3, 538.5),
                                new Vector(513.0, 112.3, 538.5),
                                new Vector(517.5, 112.3, 546.5),
                                new Vector(519.5, 112.3, 553.5)),
                        380, new Vector(503.5, 111.5, 550.5)),

                TourStop.dolly(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Bu {ışınlanma çukuruna} atlayan, dünyada rastgele bir yere düşer. Aynı yolculuğa {/rtp} ile de çıkabilirsin."),
                        new Vector(503.25, 115.6, 563.5), new Vector(503.25, 115.3, 554.25), 3.0, 0.8, 100, PLAZA),

                TourStop.dolly(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Burası {RTP Savaşı} alanı. Buradakiler belirli aralıklarla {hep birlikte aynı bölgeye} ışınlanır ve savaşır.",
                                "Kendine güveniyorsan gir, ama eşyalarını kaybetmeye hazır ol."),
                        new Vector(489.5, 117.6, 572.0), new Vector(489.5, 117.0, 577.5), 3.5, 0.8, 100, PLAZA),

                TourStop.path(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Burası {Balıkçı Rıhtımı}. Bu koyda hamsiden iki tonluk {Kadim Ejder Balığı}'na kadar {21 tür} balık yaşar.",
                                "Bu balıklar sadece sen {rıhtımın içindeyken} oltana gelir. Rıhtımın dışında deniz sıradan, eski usul balık verir.",
                                "Denize girmene gerek yok, oltanı rıhtımdan at yeter. Gerisini buranın balıkçıları sana anlatır."),
                        new Vector(453.5, 81.62, 675.5), new Vector(453.5, 81.62, 777.5), new Vector(453.5, 80.5, 809.5),
                        260, new Vector(453.5, 80.0, 726.5)),

                TourStop.path("Yaşlı Balıkçı", "bal_yasli", List.of(
                                "Hoş geldin yolcu. Ben {Yaşlı Balıkçı}, kırk yıldır bu koyda balık tutarım.",
                                "Balık nasıl tutulur, oltanın ve balığın üstündeki yazılar ne demek, merak ettiğin her şeyi bana sorabilirsin."),
                        new Vector(452.5, 81.62, 784.5), new Vector(455.5, 80.62, 787.5), new Vector(457.6, 80.7, 789.6), 160),

                TourStop.path("Olta Ustası", "bal_usta", List.of(
                                "Ben {Olta Ustası}. Oltalar benden çıkar, ilk {Basit Olta} da benden hediye.",
                                "Balık tuttukça ve para biriktirdikçe oltan {21 seviyeye} kadar yükselir. Tuttuğun balıkları da market değil, {ben alırım}."),
                        new Vector(425.5, 80.62, 799.5), new Vector(425.5, 80.62, 806.0), new Vector(425.5, 80.7, 809.5), 160),

                TourStop.path("Balık Bilgini", "bal_bilgin", List.of(
                                "Ben {Balık Bilgini}. Bu sularda tuttuğun her türü deftere yazarım.",
                                "Bir türün en ağırını tutarsan adın {sunucu rekoru} olarak kalır, herkes duyar."),
                        new Vector(453.5, 80.62, 800.5), new Vector(453.5, 80.62, 806.0), new Vector(453.5, 80.7, 809.5), 160),

                TourStop.path("Yemci", "bal_yemci", List.of(
                                "Taze yem, taze! Ben {Yemci}. {Canlı} da var {cansız} da.",
                                "Yem şart değil, ama iyi yem balığı çabuk getirir, kaçmasını da zorlaştırır."),
                        new Vector(481.5, 80.62, 800.5), new Vector(481.5, 80.62, 806.0), new Vector(481.5, 80.7, 809.4), 160),

                TourStop.path("Şaman", "saman", List.of(
                                "Ben {Şaman}. Görevlerimi ve eşya isteklerimi yerine getirenlere {özel eşyalar} veririm.",
                                "Mesela {Zümrüdün Şifa Kitabı}, bir zombi köylüyü anında normal köylüye çevirir. İşin düştüğünde beni burada bulursun."),
                        new Vector(465.5, 94.62, 478.5), new Vector(453.5, 92.62, 478.5), new Vector(449.0, 91.7, 478.0), 180),

                TourStop.path("Dede Korkut", "dedekorkut", List.of(
                                "Gel otur evlat. Ben {Dede Korkut}. Bu diyarın yiğitlerini tanır, hünerlerini ölçerim.",
                                "Verdiğim görevleri tamamlarsan {rütbe} atlarsın. Rütben yükseldikçe yeni kapılar açılır.",
                                "Hazır olduğunda gel, seni bekliyor olacağım. Yolun açık olsun."),
                        new Vector(364.5, 85.62, 284.5), new Vector(375.5, 84.62, 287.5), new Vector(377.6, 84.1, 285.1), 200)
        );
    }
}
