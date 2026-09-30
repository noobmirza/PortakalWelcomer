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
                                "Kostümlerini {/skinler} ile yönetebilirsin."),
                        List.of(
                                new Vector(496.5, 113.3, 564.5),
                                new Vector(496.0, 113.0, 547.0),
                                new Vector(506.5, 113.0, 543.0),
                                new Vector(508.5, 113.2, 556.0)),
                        List.of(
                                new Vector(488.3, 112.3, 562.5),
                                new Vector(489.5, 112.3, 546.0),
                                new Vector(513.0, 112.3, 538.5),
                                new Vector(519.5, 112.3, 553.5)),
                        200, new Vector(503.5, 111.5, 550.5)),

                TourStop.dolly(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Bu {ışınlanma çukuruna} atlayan, dünyada rastgele bir yere düşer. Aynı yolculuğa {/rtp} ile de çıkabilirsin."),
                        new Vector(503.25, 115.6, 563.5), new Vector(503.25, 115.3, 554.25), 3.0, 0.8, 100, PLAZA),

                TourStop.dolly(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Burası {RTP Savaşı} alanı. Buradakiler belirli aralıklarla {hep birlikte aynı bölgeye} ışınlanır ve savaşır.",
                                "Kendine güveniyorsan gir, ama eşyalarını kaybetmeye hazır ol."),
                        new Vector(489.5, 117.6, 572.0), new Vector(489.5, 117.0, 577.5), 3.5, 0.8, 100, PLAZA),

                TourStop.glide(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Burası {Balıkçı Rıhtımı}. {21 tür} balık var, hamsiden {Kadim Ejder Balığı}'na kadar. Oltanı rıhtımdan atman yeter.",
                                "Rıhtımın ucunda dört kişi var: {Yaşlı Balıkçı} sorularını yanıtlar, {Olta Ustası} olta satar ve balıklarını alır.",
                                "{Balık Bilgini} sunucu rekorlarını tutar, {Yemci} de yem satar."),
                        List.of(
                                new Vector(453.5, 81.62, 675.5),
                                new Vector(453.5, 81.62, 740.0),
                                new Vector(453.5, 81.62, 777.5),
                                new Vector(453.5, 82.5, 795.0)),
                        List.of(
                                new Vector(453.5, 80.5, 730.0),
                                new Vector(453.5, 80.5, 780.0),
                                new Vector(455.5, 81.5, 787.5),
                                new Vector(453.5, 81.5, 806.0)),
                        360, new Vector(453.5, 80.0, 750.0)),

                TourStop.path("Şaman", "saman", List.of(
                                "Ben {Şaman}. Görevlerimi ve eşya isteklerimi yerine getirenlere {özel eşyalar} veririm.",
                                "Mesela {Zümrüdün Şifa Kitabı}, bir zombi köylüyü anında normal köylüye çevirir. İşin düştüğünde beni burada bulursun."),
                        new Vector(465.5, 94.62, 478.5), new Vector(453.5, 92.62, 478.5), new Vector(449.0, 91.7, 478.0), 180),

                TourStop.path(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Burası {Park}. Arkadaşlarınla buluşup {mini oyunlarla} kozunu paylaşabileceğin yer.",
                                "Buraya istediğin zaman {/warp park} ile gelebilirsin."),
                        new Vector(306.5, 84.62, 490.5), new Vector(258.5, 84.62, 490.5), new Vector(240.5, 81.5, 490.5),
                        200, new Vector(282.5, 83.0, 490.5)),

                TourStop.glide(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Bunlar {bilek güreşi} masaları. Bir rakiple karşılıklı oturursun, maç kendiliğinden başlar. {3 turda 2} alan kazanır.",
                                "Ötede {XOX} masaları var. Üç işaretini ilk {yan yana} dizen kazanır."),
                        List.of(
                                new Vector(261.5, 82.12, 479.5),
                                new Vector(272.5, 82.12, 478.5),
                                new Vector(270.5, 82.62, 455.0),
                                new Vector(268.5, 82.62, 430.5),
                                new Vector(246.5, 82.62, 429.5),
                                new Vector(226.5, 82.62, 428.5)),
                        List.of(
                                new Vector(263.5, 80.9, 482.5),
                                new Vector(271.5, 80.9, 482.5),
                                new Vector(266.0, 80.5, 445.0),
                                new Vector(264.5, 80.5, 442.5),
                                new Vector(242.5, 80.5, 442.5),
                                new Vector(226.5, 80.5, 442.5)),
                        380, new Vector(268.5, 82.0, 455.0)),

                TourStop.glide(GUIDE, GUIDE_PORTRAIT, List.of(
                                "Parkın güneyi kart ve masa oyunlarına ayrılmış. Batıda {Pişti} ve {Papaz Kimde} masaları var.",
                                "Ortada {UNO} ve {101 Okey} oynanır, en doğuda ise {satranç} masaları var.",
                                "Hepsinde rakip bulunca oyun kendiliğinden başlar. Arkadaşlarınla ya da yabancılarla oynayabilirsin."),
                        List.of(
                                new Vector(198.5, 84.6, 530.5),
                                new Vector(210.5, 84.6, 530.5),
                                new Vector(240.5, 84.6, 530.5),
                                new Vector(267.5, 84.6, 530.5),
                                new Vector(279.5, 84.6, 530.5),
                                new Vector(286.5, 84.6, 527.5)),
                        List.of(
                                new Vector(201.5, 80.6, 522.5),
                                new Vector(213.5, 80.6, 538.5),
                                new Vector(252.5, 80.6, 530.5),
                                new Vector(260.5, 80.6, 526.5),
                                new Vector(275.5, 80.6, 536.5),
                                new Vector(291.5, 80.6, 527.5)),
                        420, new Vector(246.5, 82.0, 530.5)),

                TourStop.path("Dede Korkut", "dedekorkut", List.of(
                                "Gel otur evlat. Ben {Dede Korkut}. Bu diyarın yiğitlerini tanır, hünerlerini ölçerim.",
                                "Verdiğim görevleri tamamlarsan {rütbe} atlarsın. Rütben yükseldikçe yeni kapılar açılır.",
                                "Hazır olduğunda gel, seni bekliyor olacağım. Yolun açık olsun."),
                        new Vector(364.5, 85.62, 284.5), new Vector(375.5, 84.62, 287.5), new Vector(377.6, 84.1, 285.1), 200)
        );
    }
}
