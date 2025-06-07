import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KonfeksiyonTakipSistemi {

    private KonfeksiyonGUI myGui;
    private int siparisCounter = 1;
    private int musteriCounter = 1;

    public KonfeksiyonTakipSistemi() {
        // Map'ler artık gerekli değil, database kullanacağız
    }

    // Ürün yönetimi
    public void urunEkle(long urunKodu, String urunAdi, String kategori, String renk, String beden, double fiyat, int stokMiktari) {
        String sql = "INSERT INTO konfeksiyon.urun (urun_no, urun_adi, fiyat, stok_miktari, kategori) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, urunKodu);
            pstmt.setString(2, urunAdi);
            pstmt.setDouble(3, fiyat);
            pstmt.setInt(4, stokMiktari);
            pstmt.setString(5, kategori);

            pstmt.executeUpdate();
            System.out.println("✓ Ürün eklendi: " + urunAdi);
            refreshUrunlerTable();

        } catch (SQLException e) {
            System.err.println("Ürün eklenirken hata: " + e.getMessage());
        }
    }

    public void stokGuncelle(long urunKodu, int yeniStok) {
        String selectSql = "SELECT stok_miktari FROM konfeksiyon.urun WHERE urun_no = ?";
        String updateSql = "UPDATE konfeksiyon.urun SET stok_miktari = ? WHERE urun_no = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Önce eski stok miktarını al
            int eskiStok = 0;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setLong(1, urunKodu);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    eskiStok = rs.getInt("stok_miktari");
                } else {
                    System.out.println("✗ Ürün bulunamadı: " + urunKodu);
                    return;
                }
            }

            // Sonra güncelle
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, yeniStok);
                updateStmt.setLong(2, urunKodu);
                updateStmt.executeUpdate();
                System.out.println("✓ Stok güncellendi: " + urunKodu + " (" + eskiStok + " → " + yeniStok + ")");
                refreshUrunlerTable();
            }

        } catch (SQLException e) {
            System.err.println("Stok güncellenirken hata: " + e.getMessage());
        }
    }

    public List<Urun> dusukStokUrunler(int minStok) {
        List<Urun> dusukStoklar = new ArrayList<>();
        String sql = "SELECT * FROM konfeksiyon.urun WHERE stok_miktari <= ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, minStok);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Urun urun = new Urun(
                        rs.getLong("urun_no"),
                        rs.getString("urun_adi"),
                        rs.getString("kategori"),
                        rs.getDouble("fiyat"),
                        rs.getInt("stok_miktari")
                );
                dusukStoklar.add(urun);
            }

        } catch (SQLException e) {
            System.err.println("Düşük stok ürünler getirilirken hata: " + e.getMessage());
        }

        return dusukStoklar;
    }

    // Müşteri yönetimi
    public void musteriEkle(String ad, String soyad, String telefon, String email, String adres) {
        String sql = "INSERT INTO konfeksiyon.musteri (ad, soyad, telefon, email, adres) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, ad);
            pstmt.setString(2, soyad);
            pstmt.setString(3, telefon);
            pstmt.setString(4, email);
            pstmt.setString(5, adres);

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int musteriId = generatedKeys.getInt(1);
                System.out.println("✓ Müşteri eklendi - ID: " + musteriId + ", Ad: " + ad + " " + soyad);
            }

            refreshMusterilerTable();

        } catch (SQLException e) {
            System.err.println("Müşteri eklenirken hata: " + e.getMessage());
        }
    }

    public void musteriSil(int id) {
        String sql = "DELETE FROM konfeksiyon.musteri WHERE musteri_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✓ Müşteri silindi - ID: " + id);
                refreshMusterilerTable();
            } else {
                System.out.println("✗ Silinecek müşteri bulunamadı: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Müşteri silinirken hata: " + e.getMessage());
        }
    }

    public void siparisOlustur(int siparisNo, long urunNo, String musteriAdi, int toplamAdet, LocalDate siparisTarihi, LocalDate teslimTarihi, List<SiparisDetay> detaylar, String notlar) {
        // Önce müşteri ID'sini bul
        int musteriNo = getMusteriIdByName(musteriAdi);
        if (musteriNo == -1) {
            System.out.println("✗ Müşteri bulunamadı: " + musteriAdi);
            return;
        }

        //??????????
        String siparisSQL = "INSERT INTO konfeksiyon.siparisler (siparis_no, MusteriNo, urunNo, toplam_adet, siparis_tarihi, teslim_tarihi, musteri_adi, not) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String detaySQL = "INSERT INTO konfeksiyon.siparis_detay (siparis_no, Beden, Miktar, Renk, birim_fiyatı) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaction başlat

            // Sipariş ekle
            try (PreparedStatement siparisStmt = conn.prepareStatement(siparisSQL)) {
                siparisStmt.setInt(1, siparisNo);
                siparisStmt.setInt(2, musteriNo);
                siparisStmt.setLong(3, urunNo);
                siparisStmt.setInt(4, toplamAdet);
                siparisStmt.setDate(5, Date.valueOf(siparisTarihi));
                siparisStmt.setDate(6, teslimTarihi != null ? Date.valueOf(teslimTarihi) : null);
                siparisStmt.setString(7, musteriAdi);
                siparisStmt.setString(8, notlar);
                siparisStmt.executeUpdate();
            }

            // Sipariş detayları ekle
            try (PreparedStatement detayStmt = conn.prepareStatement(detaySQL)) {
                for (SiparisDetay detay : detaylar) {
                    detayStmt.setInt(1, siparisNo);
                    detayStmt.setString(2, detay.getBeden());
                    detayStmt.setInt(3, detay.getMiktar());
                    detayStmt.setString(4, detay.getRenk());
                    detayStmt.setDouble(5, detay.getBirimFiyat());
                    detayStmt.executeUpdate();
                }
            }

            conn.commit(); // Transaction'ı onayla
            logEkle("✓ Yeni sipariş eklendi: " + siparisNo);
            refreshSiparislerTable();

        } catch (SQLException e) {
            System.err.println("Sipariş oluşturulurken hata: " + e.getMessage());
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.rollback(); // Hata durumunda geri al
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback hatası: " + rollbackEx.getMessage());
            }
        }
    }

    public void siparisDurumGuncelle(String siparisNo, boolean yeniDurum) {
        String sql = "UPDATE konfeksiyon.siparisler SET hazır = ? WHERE siparis_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, yeniDurum);
            pstmt.setLong(2, Long.parseLong(siparisNo));

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✓ Sipariş durumu güncellendi: " + siparisNo + " → " + (yeniDurum ? "Hazır" : "Beklemede"));
                refreshSiparislerTable();
            } else {
                System.out.println("✗ Sipariş bulunamadı: " + siparisNo);
            }

        } catch (SQLException e) {
            System.err.println("Sipariş durumu güncellenirken hata: " + e.getMessage());
        }
    }

    public void siparisNotEkle(String siparisNo, String not) {
        String sql = "UPDATE konfeksiyon.siparisler SET 'not' = ? WHERE siparis_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, not);
            pstmt.setLong(2, Long.parseLong(siparisNo));

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✓ Sipariş notu eklendi: " + siparisNo);
            } else {
                System.out.println("✗ Sipariş bulunamadı: " + siparisNo);
            }

        } catch (SQLException e) {
            System.err.println("Sipariş notu eklenirken hata: " + e.getMessage());
        }
    }

    // Raporlama
    public void siparisDetayGoster(String siparisNo) {
        String sql = "SELECT s.*, u.urun_adi FROM konfeksiyon.siparisler s JOIN konfeksiyon.urun u ON s.urunNo = u.urun_no WHERE s.siparis_no = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, Long.parseLong(siparisNo));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("SİPARİŞ DETAYI");
                System.out.println("=".repeat(50));
                System.out.println("Sipariş No: " + rs.getLong("siparis_no"));
                System.out.println("Müşteri: " + rs.getString("musteri_adi"));
                System.out.println("Ürün: " + rs.getString("urun_adi"));
                System.out.println("Adet: " + rs.getInt("toplam_adet"));
                System.out.println("Sipariş Tarihi: " + rs.getDate("siparis_tarihi"));
                System.out.println("Teslim Tarihi: " + rs.getDate("teslim_tarihi"));
                System.out.println("Durum: " + (rs.getBoolean("hazır") ? "Hazır" : "Beklemede"));
                System.out.println("Notlar: " + rs.getString("not"));
                System.out.println("=".repeat(50));
            } else {
                System.out.println("✗ Sipariş bulunamadı: " + siparisNo);
            }

        } catch (SQLException e) {
            System.err.println("Sipariş detayı getirilirken hata: " + e.getMessage());
        }
    }

    public void gunlukSiparisRaporu() {
        LocalDate bugun = LocalDate.now();
        String sql = "SELECT COUNT(*) as siparis_sayisi, SUM(sd.birim_fiyatı * sd.Miktar) as toplam_ciro " +
                "FROM konfeksiyon.siparisler s LEFT JOIN konfeksiyon.siparis_detay sd ON s.siparis_no = sd.siparis_no " +
                "WHERE s.siparis_tarihi = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(bugun));
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n" + "=".repeat(50));
            System.out.println("GÜNLÜK SİPARİŞ RAPORU - " + bugun);
            System.out.println("=".repeat(50));

            if (rs.next()) {
                System.out.println("Toplam Sipariş: " + rs.getInt("siparis_sayisi"));
                System.out.println("Günlük Ciro: " + String.format("%.2f TL", rs.getDouble("toplam_ciro")));
            }

            // Durum dağılımı
            String durumSQL = "SELECT hazır, COUNT(*) as sayi FROM konfeksiyon.siparisler WHERE siparis_tarihi = ? GROUP BY hazır";
            try (PreparedStatement durumStmt = conn.prepareStatement(durumSQL)) {
                durumStmt.setDate(1, Date.valueOf(bugun));
                ResultSet durumRs = durumStmt.executeQuery();

                System.out.println("\nDurum Dağılımı:");
                while (durumRs.next()) {
                    String durum = durumRs.getBoolean("hazır") ? "Hazır" : "Beklemede";
                    System.out.println("  " + durum + ": " + durumRs.getInt("sayi"));
                }
            }

            System.out.println("=".repeat(50));

        } catch (SQLException e) {
            System.err.println("Günlük rapor oluşturulurken hata: " + e.getMessage());
        }
    }

    public void stokRaporu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("STOK RAPORU");
        System.out.println("=".repeat(50));

        List<Urun> dusukStoklar = dusukStokUrunler(10);
        if (!dusukStoklar.isEmpty()) {
            System.out.println("⚠️  DÜŞÜK STOK UYARISI (≤10):");
            for (Urun urun : dusukStoklar) {
                System.out.println("  " + urun.getUrunAdi() + " - Stok: " + urun.getStokMiktari());
            }
        } else {
            System.out.println("✓ Tüm ürünlerde yeterli stok mevcut");
        }

        // Kategori bazında stok
        String sql = "SELECT kategori, SUM(stok_miktari) as toplam_stok FROM konfeksiyon.urun GROUP BY kategori";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nKategori Bazında Stok:");
            while (rs.next()) {
                System.out.println("  " + rs.getString("kategori") + ": " + rs.getInt("toplam_stok") + " adet");
            }

        } catch (SQLException e) {
            System.err.println("Kategori stok raporu oluşturulurken hata: " + e.getMessage());
        }

        System.out.println("=".repeat(50));
    }

    public void tumSiparisleriListele() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TÜM SİPARİŞLER");
        System.out.println("=".repeat(50));

        String sql = "SELECT s.siparis_no, s.musteri_adi, u.urun_adi, s.toplam_adet, s.siparis_tarihi, s.hazır, " +
                "SUM(sd.birim_fiyatı * sd.Miktar) as toplam_fiyat " +
                "FROM konfeksiyon.siparisler s " +
                "JOIN konfeksiyon.urun u ON s.urunNo = u.urun_no " +
                "LEFT JOIN konfeksiyon.siparis_detay sd ON s.siparis_no = sd.siparis_no " +
                "GROUP BY s.siparis_no";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean varMi = false;
            while (rs.next()) {
                varMi = true;
                System.out.printf("Sipariş: %d | Müşteri: %s | Ürün: %s | Durum: %s | Tutar: %.2f TL | Tarih: %s%n",
                        rs.getLong("siparis_no"),
                        rs.getString("musteri_adi"),
                        rs.getString("urun_adi"),
                        rs.getBoolean("hazır") ? "Hazır" : "Beklemede",
                        rs.getDouble("toplam_fiyat"),
                        rs.getDate("siparis_tarihi"));
            }

            if (!varMi) {
                System.out.println("Henüz sipariş bulunmuyor.");
            }

        } catch (SQLException e) {
            System.err.println("Siparişler listelenirken hata: " + e.getMessage());
        }

        System.out.println("=".repeat(50));
    }

    //Helper methods ---------------------------------------------------------------------------
    private double checkPrice(long urunNo, double birimFiyat) {
        String sql = "SELECT fiyat FROM konfeksiyon.urun WHERE urun_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, urunNo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double mainBirimFiyat = rs.getDouble("fiyat");
                if (mainBirimFiyat != birimFiyat) {
                    int answer = JOptionPane.showConfirmDialog(null,
                            "Ürünün kayıtlı birim fiyatı ile girilen fiyat aynı değil. Yeni fiyat yine de kullanılsın mı? Kayıtlı Birim Fiyatı: " + mainBirimFiyat + "| Girilen: " + birimFiyat,
                            "Fiyat Uyarısı", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (answer != JOptionPane.YES_OPTION) {
                        return mainBirimFiyat;
                    }
                }
                return birimFiyat;
            }

        } catch (SQLException e) {
            System.err.println("Fiyat kontrol edilirken hata: " + e.getMessage());
        }

        return birimFiyat;
    }

    private void checkStock(long urunNo, int adet) {
        String sql = "SELECT stok_miktari FROM konfeksiyon.urun WHERE urun_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, urunNo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int stokMiktari = rs.getInt("stok_miktari");
                if (stokMiktari < adet) {
                    JOptionPane.showMessageDialog(null, "Stok miktarı sipariş miktarını karşılamıyor. Üretilmesi gerek ürün sayısı: " + (adet - stokMiktari), "Stok Miktarı Uyarısı", JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException e) {
            System.err.println("Stok kontrol edilirken hata: " + e.getMessage());
        }
    }

    private int getMusteriIdByName(String musteriAdi) {
        String sql = "SELECT musteri_no FROM konfeksiyon.musteri WHERE CONCAT(ad, ' ', soyad) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, musteriAdi);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("musteri_no");
            }

        } catch (SQLException e) {
            System.err.println("Müşteri ID bulunurken hata: " + e.getMessage());
        }

        return -1;
    }

    private static void logEkle(String mesaj) {
        String zaman = java.time.LocalTime.now().toString().substring(0, 8);
        KonfeksiyonGUI.logArea.append("[" + zaman + "] " + mesaj + "\n");
        KonfeksiyonGUI.logArea.setCaretPosition(KonfeksiyonGUI.logArea.getDocument().getLength());
    }

    private void refreshUrunlerTable() {
        if (myGui == null) return;

        myGui.urunTableModel.setRowCount(0);

        String sql = "SELECT * FROM konfeksiyon.urun";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                Object[] row = {
                        rs.getLong("urun_no"),
                        rs.getString("urun_adi"),
                        rs.getString("kategori"),
                        String.format("%.2f TL", rs.getDouble("fiyat")),
                        rs.getInt("stok_miktari")
                };
                myGui.urunTableModel.addRow(row);
                count++;
            }

            logEkle("📦 Ürün tablosu yenilendi - " + count + " ürün");

        } catch (SQLException e) {
            System.err.println("Ürün tablosu yenilenirken hata: " + e.getMessage());
        }
    }

    private void refreshMusterilerTable() {
        if (myGui == null) return;

        myGui.musteriTableModel.setRowCount(0);

        String sql = "SELECT * FROM konfeksiyon.musteri";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("musteri_no"),
                        rs.getString("ad") + " " + rs.getString("soyad"),
                        rs.getString("telefon"),
                        rs.getString("email")
                };
                myGui.musteriTableModel.addRow(row);
                count++;
            }

            logEkle("👥 Müşteri tablosu yenilendi - " + count + " müşteri");

        } catch (SQLException e) {
            System.err.println("Müşteri tablosu yenilenirken hata: " + e.getMessage());
        }
    }

    private void refreshSiparislerTable() {
        if (myGui == null) return;

        myGui.siparisTableModel.setRowCount(0);

        String sql = "SELECT s.siparis_no, s.musteri_adi, s.urunNo, s.hazır, s.toplam_adet, " +
                "SUM(sd.birim_fiyatı * sd.Miktar) as toplam_fiyat " +
                "FROM konfeksiyon.siparisler s " +
                "LEFT JOIN konfeksiyon.siparis_detay sd ON s.siparis_no = sd.siparis_no " +
                "GROUP BY s.siparis_no";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                Object[] row = {
                        rs.getLong("siparis_no"),
                        rs.getString("musteri_adi"),
                        rs.getLong("urunNo"),
                        rs.getBoolean("hazır") ? "Hazır" : "Beklemede",
                        rs.getInt("toplam_adet"),
                        String.format("%.2f TL", rs.getDouble("toplam_fiyat"))
                };
                myGui.siparisTableModel.addRow(row);
                count++;
            }

            logEkle("📋 Sipariş tablosu yenilendi - " + count + " sipariş");

        } catch (SQLException e) {
            System.err.println("Sipariş tablosu yenilenirken hata: " + e.getMessage());
        }
    }

    // GUI bağlantısı için setter
    public void setMyGui(KonfeksiyonGUI gui) {
        this.myGui = gui;
    }


    public List<Urun> getTumUrunler() {
        List<Urun> urunler = new ArrayList<>();
        String sql = "SELECT * FROM konfeksiyon.urun";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                urunler.add(new Urun(
                        rs.getLong("urun_no"),
                        rs.getString("urun_adi"),
                        rs.getString("kategori"),
                        rs.getDouble("fiyat"),
                        rs.getInt("stok_miktari")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ürünler alınamadı: " + e.getMessage());
        }
        return urunler;
    }

    public ArrayList<Musteri> getTumMusteriler() {
        ArrayList<Musteri> musteriler = new ArrayList<>();
        String sql = "SELECT * FROM konfeksiyon.musteri";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                musteriler.add(new Musteri(
                        rs.getInt("musteri_no"),
                        rs.getString("ad"),
                        rs.getString("soyad"),
                        rs.getString("telefon"),
                        rs.getString("email"),
                        rs.getString("adres")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Müşteriler alınamadı: " + e.getMessage());
        }
        return musteriler;
    }

    public List<Siparis> getTumSiparisler() {
        List<Siparis> siparisler = new ArrayList<>();
        String sql = "SELECT * FROM konfeksiyon.siparisler";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Siparis siparis = new Siparis(
                        rs.getInt("siparis_no"),
                        rs.getLong("urunNo"),
                        rs.getString("musteri_adi"),
                        rs.getInt("toplam_adet"),
                        rs.getDate("siparis_tarihi").toLocalDate(),
                        rs.getDate("teslim_tarihi") != null ? rs.getDate("teslim_tarihi").toLocalDate() : null,
                        getSiparisDetaylari(rs.getInt("siparis_no")),
                        rs.getString("not")
                );
                siparis.setDurum(rs.getBoolean("hazır"));
                siparisler.add(siparis);
            }
        } catch (SQLException e) {
            System.err.println("Siparişler alınamadı: " + e.getMessage());
        }
        return siparisler;
    }

    private List<SiparisDetay> getSiparisDetaylari(int siparisNo) {
        List<SiparisDetay> detaylar = new ArrayList<>();
        String sql = "SELECT * FROM konfeksiyon.siparis_detay WHERE siparis_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, siparisNo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                detaylar.add(new SiparisDetay(
                        rs.getString("Beden"),
                        rs.getInt("Miktar"),
                        rs.getString("Renk"),
                        rs.getDouble("birim_fiyatı")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Sipariş detayları alınamadı: " + e.getMessage());
        }
        return detaylar;
    }
}