import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KonfeksiyonTakipSistemi {
    Map<String, Urun> urunler;
    private Map<String, Musteri> musteriler;
    private Map<String, Siparis> siparisler;
    private int siparisCounter = 1;
    private int musteriCounter = 1;

    public KonfeksiyonTakipSistemi() {
        this.urunler = new HashMap<>();
        this.musteriler = new HashMap<>();
        this.siparisler = new HashMap<>();
    }

    // Ürün yönetimi
    public void urunEkle(String urunKodu, String urunAdi, String kategori, String renk, String beden, double fiyat, int stokMiktari) {
        Urun urun = new Urun(urunKodu, urunAdi, kategori, renk, beden, fiyat, stokMiktari);
        urunler.put(urunKodu, urun);
        System.out.println("✓ Ürün eklendi: " + urun);
    }

    public void stokGuncelle(String urunKodu, int yeniStok) {
        Urun urun = urunler.get(urunKodu);
        if (urun != null) {
            int eskiStok = urun.getStokMiktari();
            urun.setStokMiktari(yeniStok);
            System.out.println("✓ Stok güncellendi: " + urunKodu + " (" + eskiStok + " → " + yeniStok + ")");
        } else {
            System.out.println("✗ Ürün bulunamadı: " + urunKodu);
        }
    }

    public List<Urun> dusukStokUrunler(int minStok) {
        return urunler.values().stream()
                .filter(urun -> urun.getStokMiktari() <= minStok)
                .collect(Collectors.toList());
    }

    // Müşteri yönetimi
    public String musteriEkle(String ad, String soyad, String telefon, String email, String adres) {
        String musteriId = "M" + String.format("%04d", musteriCounter++);
        Musteri musteri = new Musteri(musteriId, ad, soyad, telefon, email, adres);
        musteriler.put(musteriId, musteri);
        System.out.println("✓ Müşteri eklendi: " + musteri);
        return musteriId;
    }

    // Sipariş yönetimi
    public String siparisOlustur(String musteriId) {
        if (!musteriler.containsKey(musteriId)) {
            System.out.println("✗ Müşteri bulunamadı: " + musteriId);
            return null;
        }

        String siparisNo = "S" + String.format("%06d", siparisCounter++);
        Siparis siparis = new Siparis(siparisNo, musteriId);
        siparisler.put(siparisNo, siparis);
        System.out.println("✓ Yeni sipariş oluşturuldu: " + siparisNo);
        return siparisNo;
    }

    public void siparisUrunEkle(String siparisNo, String urunKodu, int miktar) {
        Siparis siparis = siparisler.get(siparisNo);
        Urun urun = urunler.get(urunKodu);

        if (siparis == null) {
            System.out.println("✗ Sipariş bulunamadı: " + siparisNo);
            return;
        }

        if (urun == null) {
            System.out.println("✗ Ürün bulunamadı: " + urunKodu);
            return;
        }

        if (urun.getStokMiktari() < miktar) {
            System.out.println("✗ Yetersiz stok! Mevcut: " + urun.getStokMiktari() + ", İstenen: " + miktar);
            return;
        }

        siparis.urunEkle(urunKodu, miktar, urun.getFiyat());
        urun.setStokMiktari(urun.getStokMiktari() - miktar);
        System.out.println("✓ Ürün siparişe eklendi: " + urunKodu + " x" + miktar);
    }

    public void siparisDurumGuncelle(String siparisNo, SiparisDurumu yeniDurum) {
        Siparis siparis = siparisler.get(siparisNo);
        if (siparis != null) {
            SiparisDurumu eskiDurum = siparis.getDurum();
            siparis.setDurum(yeniDurum);
            System.out.println("✓ Sipariş durumu güncellendi: " + siparisNo + " (" + eskiDurum + " → " + yeniDurum + ")");
        } else {
            System.out.println("✗ Sipariş bulunamadı: " + siparisNo);
        }
    }

    public void siparisNotEkle(String siparisNo, String not) {
        Siparis siparis = siparisler.get(siparisNo);
        if (siparis != null) {
            siparis.setNotlar(not);
            System.out.println("✓ Sipariş notu eklendi: " + siparisNo);
        } else {
            System.out.println("✗ Sipariş bulunamadı: " + siparisNo);
        }
    }

    // Raporlama
    public void siparisDetayGoster(String siparisNo) {
        Siparis siparis = siparisler.get(siparisNo);
        if (siparis != null) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("SİPARİŞ DETAYI");
            System.out.println("=".repeat(50));
            System.out.println(siparis);

            Musteri musteri = musteriler.get(siparis.getMusteriId());
            if (musteri != null) {
                System.out.println("Müşteri Bilgileri: " + musteri);
            }
            System.out.println("=".repeat(50));
        } else {
            System.out.println("✗ Sipariş bulunamadı: " + siparisNo);
        }
    }

    public void gunlukSiparisRaporu() {
        LocalDate bugun = LocalDate.now();
        List<Siparis> gunlukSiparisler = siparisler.values().stream()
                .filter(s -> s.getSiparisTarihi().toLocalDate().equals(bugun))
                .collect(Collectors.toList());

        System.out.println("\n" + "=".repeat(50));
        System.out.println("GÜNLÜK SİPARİŞ RAPORU - " + bugun);
        System.out.println("=".repeat(50));
        System.out.println("Toplam Sipariş: " + gunlukSiparisler.size());

        double toplamCiro = gunlukSiparisler.stream()
                .mapToDouble(Siparis::getToplamTutar)
                .sum();
        System.out.println("Günlük Ciro: " + String.format("%.2f TL", toplamCiro));

        Map<SiparisDurumu, Long> durumSayilari = gunlukSiparisler.stream()
                .collect(Collectors.groupingBy(Siparis::getDurum, Collectors.counting()));

        System.out.println("\nDurum Dağılımı:");
        for (Map.Entry<SiparisDurumu, Long> entry : durumSayilari.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("=".repeat(50));
    }

    public void stokRaporu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("STOK RAPORU");
        System.out.println("=".repeat(50));

        List<Urun> dusukStoklar = dusukStokUrunler(10);
        if (!dusukStoklar.isEmpty()) {
            System.out.println("⚠️  DÜŞÜK STOK UYARISI (≤10):");
            for (Urun urun : dusukStoklar) {
                System.out.println("  " + urun);
            }
        } else {
            System.out.println("✓ Tüm ürünlerde yeterli stok mevcut");
        }

        System.out.println("\nKategori Bazında Stok:");
        Map<String, Integer> kategoriStok = urunler.values().stream()
                .collect(Collectors.groupingBy(
                        Urun::getKategori,
                        Collectors.summingInt(Urun::getStokMiktari)
                ));

        for (Map.Entry<String, Integer> entry : kategoriStok.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " adet");
        }
        System.out.println("=".repeat(50));
    }

    public void tumSiparisleriListele() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TÜM SİPARİŞLER");
        System.out.println("=".repeat(50));

        if (siparisler.isEmpty()) {
            System.out.println("Henüz sipariş bulunmuyor.");
        } else {
            for (Siparis siparis : siparisler.values()) {
                System.out.println(String.format("Sipariş: %s | Müşteri: %s | Durum: %s | Tutar: %.2f TL | Tarih: %s",
                        siparis.getSiparisNo(),
                        siparis.getMusteriId(),
                        siparis.getDurum(),
                        siparis.getToplamTutar(),
                        siparis.getSiparisTarihi().toLocalDate()));
            }
        }
        System.out.println("=".repeat(50));
    }
}
