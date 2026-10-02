# PortakalWelcomer

portakalnetwork.com'un tanıtım plugini. Yeni giren oyuncuyu kamerayla sunucuda gezdiriyor, rehber de yol boyunca bir şeyler anlatıyor. Ölünce de kamera cesedin etrafında dönüyor, ölüm ekranına bakıp küsmesin diye.

Kamera aslında armor stand değil, sadece oyuncunun kendi ekranında görünen bir display entity. Oyuncu o sırada görünmez, kıpırdayamaz ve ölmez (kendi kendine tıklayıp kick yemesin diye ayrıca birkaç önlem var).

Komut: `/pwc tanitim [oyuncu]`, `kilit`, `sinematik`, `olum`, `birak [oyuncu]`. Yetki `portakalwelcomer.admin`.

Paper 26.2, Java 25, PacketEvents ve PortakalHub lazım, yoksa çalışmaz.
