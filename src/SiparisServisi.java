import java.util.List;

public class SiparisServisi {

    public boolean stoktanUrunCikar(List<Urun> urunler, String urunKodu, int miktar) {
        for (Urun urun : urunler) {
            if (urun.getUrunKodu().equals(urunKodu)) {
                if (urun.getStokMiktari() >= miktar) {
                    urun.setStokMiktari(urun.getStokMiktari() - miktar);
                    return true;
                } else {
                    System.out.println("Yetersiz stok: " + urun.getUrunAdi());
                    return false;
                }
            }
        }
        System.out.println("Ürün bulunamadı: " + urunKodu);
        return false;
    }

    public boolean sipariseUrunEkle(Siparis siparis, List<Urun> urunler, String urunKodu, int miktar) {
        for (Urun urun : urunler) {
            if (urun.getUrunKodu().equals(urunKodu)) {
                if (urun.getStokMiktari() >= miktar) {
                    siparis.urunEkle(urunKodu, miktar, urun.getFiyat());
                    urun.setStokMiktari(urun.getStokMiktari() - miktar);
                    return true;
                } else {
                    System.out.println("Stok yetersiz: " + urun.getUrunAdi());
                    return false;
                }
            }
        }
        System.out.println("Ürün kodu geçersiz: " + urunKodu);
        return false;
    }

    public Siparis yeniSiparisOlustur(String siparisNo, Musteri musteri) {
        return new Siparis(siparisNo, musteri.getMusteriId());
    }

    public void siparisRaporuGoster(Siparis siparis, List<Urun> urunler) {
        System.out.println("----- Sipariş Özeti -----");
        System.out.println(siparis);

        for (SiparisDetay detay : siparis.getDetaylar()) {
            for (Urun urun : urunler) {
                if (urun.getUrunKodu().equals(detay.getUrunKodu())) {
                    System.out.println("  Ürün Adı: " + urun.getUrunAdi() + " (" + urun.getRenk() + ", " + urun.getBeden() + ")");
                }
            }
        }
    }
}
