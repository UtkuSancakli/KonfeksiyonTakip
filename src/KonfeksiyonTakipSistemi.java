import javax.swing.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KonfeksiyonTakipSistemi {

    private KonfeksiyonGUI myGui;
    Map<Long, Urun> urunler;
    protected Map<Integer, Musteri> musteriler;
    protected Map<Integer, Siparis> siparisler;

    private int siparisCounter = 1;
    private int musteriCounter = 1;

    public KonfeksiyonTakipSistemi() {
        this.urunler = new HashMap<>();
        this.musteriler = new HashMap<>();
        this.siparisler = new HashMap<>();
    }

    // Ürün yönetimi
    public void urunEkle(long urunKodu, String urunAdi, String kategori, String renk, String beden, double fiyat, int stokMiktari) {
        Urun urun = new Urun(urunKodu, urunAdi, kategori, renk, beden, fiyat, stokMiktari);
        urunler.put(urunKodu, urun);
        System.out.println("✓ Ürün eklendi: " + urun);
    }

    public void stokGuncelle(long urunKodu, int yeniStok) {
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
    public void musteriEkle(String ad, String soyad, String telefon, String email, String adres) {
        int musteriId = musteriCounter++;
        Musteri musteri = new Musteri(musteriId, ad, soyad, telefon, email, adres);
        musteriler.put(musteriId, musteri);
        System.out.println("✓ Müşteri eklendi: " + musteri);
    }

    public void musteriSil(int id){
        musteriler.remove(id);
        musteriCounter--;
    }

    public void siparisOlustur(int siparisNo, long urunNo, String musteriAdi, int toplamAdet, LocalDate siparisTarihi, LocalDate teslimTarihi,  List<SiparisDetay> detaylar, String notlar) {

        //checkStock(urunNo, toplamAdet);

        Siparis tempSiparis = new Siparis(siparisNo, urunNo, musteriAdi, toplamAdet, siparisTarihi, teslimTarihi, detaylar, notlar);
        siparisler.put(siparisNo, tempSiparis);


        logEkle("✓ Yeni sipariş eklendi: " + siparisNo);
    }

    public void siparisDurumGuncelle(String siparisNo, boolean yeniDurum) {
        Siparis siparis = siparisler.get(siparisNo);
        if (siparis != null) {
            boolean eskiDurum = siparis.getDurum();
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

            Musteri musteri = musteriler.get(siparis.getMusteriAdi());
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
                .filter(s -> s.getSiparisTarihi().equals(bugun))
                .toList();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("GÜNLÜK SİPARİŞ RAPORU - " + bugun);
        System.out.println("=".repeat(50));
        System.out.println("Toplam Sipariş: " + gunlukSiparisler.size());

        double toplamCiro = gunlukSiparisler.stream()
                .mapToDouble(Siparis::getToplamFiyat)
                .sum();
        System.out.println("Günlük Ciro: " + String.format("%.2f TL", toplamCiro));

        Map<Boolean, Long> durumSayilari = gunlukSiparisler.stream()
                .collect(Collectors.groupingBy(Siparis::getDurum, Collectors.counting()));

        System.out.println("\nDurum Dağılımı:");
        for (Map.Entry<Boolean, Long> entry : durumSayilari.entrySet()) {
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
        }
        else {
            for (Siparis siparis : siparisler.values()) {
                System.out.println(String.format("Sipariş: %s | Müşteri: %s | Durum: %s | Tutar: %.2f TL | Tarih: %s",
                        siparis.getSiparisNo(),
                        siparis.getMusteriAdi(),
                        siparis.getDurum(),
                        siparis.getToplamFiyat(),
                        siparis.getSiparisTarihi()));
            }
        }
        System.out.println("=".repeat(50));
    }


    //Helper methods ---------------------------------------------------------------------------
    private double checkPrice(long urunNo, double birimFiyat) {

        double mainBirimFiyat = urunler.get(urunNo).getFiyat();
        if(mainBirimFiyat != birimFiyat){
            int answer = JOptionPane.showConfirmDialog(null,
                    "Ürünün kayıtlı birim fiyatı ile girilen fiyat aynı değil. Yeni fiyat yine de kullanılsın mı? Kayıtlı Birim Fiyatı: " + mainBirimFiyat + "| Girilen: " + birimFiyat,
                    "Fiyat Uyarısı", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return mainBirimFiyat;
            }
        }
        return birimFiyat;
    }

    private void checkStock(long urunNo, int adet) {
        int stokMiktari = urunler.get(urunNo).getStokMiktari();
        if(stokMiktari < adet){
            JOptionPane.showMessageDialog(null, "Stok miktarı sipariş miktarını karşılamıyor. Üretilmesi gerek ürün sayısı: " + (adet-stokMiktari), "Stok Miktarı Uyarısı", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void logEkle(String mesaj) {
        String zaman = java.time.LocalTime.now().toString().substring(0, 8);
        KonfeksiyonGUI.logArea.append("[" + zaman + "] " + mesaj + "\n");
        KonfeksiyonGUI.logArea.setCaretPosition(KonfeksiyonGUI.logArea.getDocument().getLength());
    }

    private void refreshUrunlerTable() {
        // Clear existing data
        myGui.urunTableModel.setRowCount(0);

        // Add all products from the Map
        for (Urun urun : urunler.values()) {
            Object[] row = {
                    urun.getUrunKodu(),
                    urun.getUrunAdi(),
                    urun.getKategori(),
                    urun.getRenk(),
                    urun.getBeden(),
                    String.format("%.2f TL", urun.getFiyat()),
                    urun.getStokMiktari()
            };
            myGui.urunTableModel.addRow(row);
        }

        logEkle("📦 Ürün tablosu yenilendi - " + urunler.size() + " ürün");
    }

    private void refreshMusterilerTable() {
        // Assuming you have a musteriTableModel variable for the customer table
        // Clear existing data
        myGui.musteriTableModel.setRowCount(0);

        // Add all customers from the Map
        for (Musteri musteri : musteriler.values()) {
            Object[] row = {
                    musteri.getId(),
                    musteri.getAd() + " " + musteri.getSoyad(),
                    musteri.getTelefon(),
                    musteri.getEmail()
            };
            myGui.musteriTableModel.addRow(row);
        }

        logEkle("👥 Müşteri tablosu yenilendi - " + musteriler.size() + " müşteri");
    }

    private void refreshSiparislerTable() {
        // Assuming you have a siparisTableModel variable for the orders table
        // Clear existing data
        myGui.siparisTableModel.setRowCount(0);

        // Add all orders from the Map
        for (Siparis siparis : siparisler.values()) {
            Object[] row = {
                    siparis.getSiparisNo(),
                    siparis.getMusteriAdi(),
                    siparis.getUrunNo(),
                    siparis.getDurum() ? "Hazır" : "Beklemede",
                    siparis.getDetaylar().stream().mapToInt(SiparisDetay::getMiktar).sum(), // Total quantity
                    String.format("%.2f TL", siparis.getToplamFiyat())
            };
            myGui.siparisTableModel.addRow(row);
        }

        logEkle("📋 Sipariş tablosu yenilendi - " + siparisler.size() + " sipariş");
    }


}
