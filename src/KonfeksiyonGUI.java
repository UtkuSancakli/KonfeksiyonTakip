import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;

public class KonfeksiyonGUI extends JFrame {
    private KonfeksiyonTakipSistemi sistem;
    private JTabbedPane tabbedPane;

    UrunTableModel urunTableModel;
    private JTable urunTable;
    MusteriTableModel musteriTableModel;
    private JTable musteriTable;
    SiparisTableModel siparisTableModel;
    private JTable siparisTable;
    SiparisDetayTableModel detayTableModel;
    private JTable detayTable;

    static JTextArea logArea;

    public KonfeksiyonGUI() {
        sistem = new KonfeksiyonTakipSistemi();
        initializeGUI();
        ornekVerileriYukle();
    }

    private void initializeGUI() {
        setTitle("🏭 Konfeksiyon Takip Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Ana menü oluştur
        createMenuBar();

        // Tabbed pane oluştur
        tabbedPane = new JTabbedPane();

        // Sekmeleri ekle
        tabbedPane.addTab("📦 Ürün Yönetimi", createUrunPanel());
        tabbedPane.addTab("👥 Müşteri Yönetimi", createMusteriPanel());
        tabbedPane.addTab("📋 Sipariş Yönetimi", createSiparisPanel());
        tabbedPane.addTab("📊 Raporlar", createRaporPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Log alanı ekle
        logArea = new JTextArea(8, 0);
        logArea.setEditable(false);
        logArea.setBackground(new Color(240, 240, 240));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("İşlem Geçmişi"));
        add(logScroll, BorderLayout.SOUTH);

        // Pencere ayarları
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Look and Feel ayarla
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Dosya menüsü
        JMenu dosyaMenu = new JMenu("Dosya");
        JMenuItem cikisItem = new JMenuItem("Çıkış");
        cikisItem.addActionListener(e -> System.exit(0));
        dosyaMenu.add(cikisItem);

        // Yardım menüsü
        JMenu yardimMenu = new JMenu("Yardım");
        JMenuItem hakkindaItem = new JMenuItem("Hakkında");
        hakkindaItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Konfeksiyon Takip Sistemi v1.0\n\nJava Swing ile geliştirilmiştir.",
                        "Hakkında",
                        JOptionPane.INFORMATION_MESSAGE));
        yardimMenu.add(hakkindaItem);

        menuBar.add(dosyaMenu);
        menuBar.add(yardimMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createUrunPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Üst buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton ekleBtn = new JButton("➕ Ürün Ekle");
        JButton guncelleBtn = new JButton("🔄 Stok Güncelle");
        JButton yenileBtn = new JButton("↻ Yenile");

        ekleBtn.addActionListener(e -> urunEkleDialog());
        guncelleBtn.addActionListener(e -> stokGuncelleDialog());
        yenileBtn.addActionListener(e -> urunTablosunuYenile());

        buttonPanel.add(ekleBtn);
        buttonPanel.add(guncelleBtn);
        buttonPanel.add(yenileBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);

        // Ürün tablosu
        urunTableModel = new UrunTableModel(new ArrayList<>(sistem.urunler.values()));
        urunTable = new JTable(urunTableModel);
        urunTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        urunTable.getTableHeader().setReorderingAllowed(false);

        // Stok sütunu için özel renderer (düşük stok kırmızı)
        urunTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                int stok = (Integer) value;
                if (stok <= 10) {
                    setBackground(new Color(255, 200, 200));
                    setForeground(Color.RED);
                } else {
                    setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(urunTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMusteriPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton ekleBtn = new JButton("➕ Müşteri Ekle");
        JButton duzenleBtn = new JButton("✏️ Düzenle");
        JButton silBtn = new JButton("🗑️ Sil");
        JButton yenileBtn = new JButton("↻ Yenile");

        buttonPanel.add(ekleBtn);
        buttonPanel.add(duzenleBtn);
        buttonPanel.add(silBtn);
        buttonPanel.add(yenileBtn);
        panel.add(buttonPanel, BorderLayout.NORTH);

        // Müşteri tablosu
        List<Musteri> musteriler = new ArrayList<>(sistem.musteriler.values());
        musteriTableModel = new MusteriTableModel(musteriler);
        musteriTable = new JTable(musteriTableModel);
        musteriTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        musteriTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(musteriTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Ekle butonu
        ekleBtn.addActionListener(e -> musteriEkleDialog());

        // Düzenle butonu
        duzenleBtn.addActionListener(e -> musteriDuzenleDialog());

        // Sil butonu
        silBtn.addActionListener(e -> musteriSil());

        // Yenile butonu
        yenileBtn.addActionListener(e -> musteriTablosunuYenile());

        return panel;
    }

    private JPanel createSiparisPanel() {
        JPanel anaPanel = new JPanel(new BorderLayout());


        // === Üst: Buton Paneli ===
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton ekleBtn = new JButton("➕ Sipariş Ekle");
        JButton yenileBtn = new JButton("↻ Yenile");
        buttonPanel.add(ekleBtn);
        buttonPanel.add(yenileBtn);
        anaPanel.add(buttonPanel, BorderLayout.NORTH);

        // === Orta: JSplitPane ile 2 tablo ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // --- Üst: Sipariş Tablosu ---
        List<Siparis> siparisler = new ArrayList<>(sistem.siparisler.values());
        siparisTableModel = new SiparisTableModel(siparisler);
        siparisTable = new JTable(siparisTableModel);
        siparisTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane siparisScroll = new JScrollPane(siparisTable);
        siparisScroll.setBorder(BorderFactory.createTitledBorder("Siparişler"));

        // --- Alt: Sipariş Detay Tablosu ---
        detayTableModel = new SiparisDetayTableModel(new ArrayList<>());
        detayTable = new JTable(detayTableModel);
        JScrollPane detayScroll = new JScrollPane(detayTable);
        detayScroll.setBorder(BorderFactory.createTitledBorder("Sipariş Detayları"));

        // === Satır seçimi ===
        siparisTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = siparisTable.getSelectedRow();
            if (selectedRow >= 0) {
                Siparis secili = siparisTableModel.getSiparisAt(selectedRow);
                if (secili != null) {
                    detayTableModel.setDetaylar(secili.getDetaylar());
                }
            }
        });

        splitPane.setTopComponent(siparisScroll);
        splitPane.setBottomComponent(detayScroll);
        anaPanel.add(splitPane, BorderLayout.CENTER);

        // === Buton aksiyonları ===
        ekleBtn.addActionListener(e -> siparisEkleDialog());
        yenileBtn.addActionListener(e -> siparisTablosunuYenile());

        return anaPanel;
    }

    private JPanel createRaporPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton stokRaporuBtn = new JButton("📊 Stok Raporu");
        JButton siparisRaporuBtn = new JButton("📋 Günlük Sipariş Raporu");
        JButton dusukStokBtn = new JButton("⚠️ Düşük Stok Uyarısı");
        JButton genelRaporBtn = new JButton("📈 Genel Rapor");

        stokRaporuBtn.addActionListener(e -> stokRaporuGoster());
        siparisRaporuBtn.addActionListener(e -> logEkle("Günlük sipariş raporu oluşturuldu"));
        dusukStokBtn.addActionListener(e -> dusukStokUyarisi());
        genelRaporBtn.addActionListener(e -> logEkle("Genel rapor oluşturuldu"));

        panel.add(stokRaporuBtn);
        panel.add(siparisRaporuBtn);
        panel.add(dusukStokBtn);
        panel.add(genelRaporBtn);

        return panel;
    }

    private void urunEkleDialog() {
        JDialog dialog = new JDialog(this, "Ürün Ekle", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Form alanları
        JTextField kodField = new JTextField(15);
        JTextField adField = new JTextField(15);
        JTextField kategoriField = new JTextField(15);
        JTextField renkField = new JTextField(15);
        JTextField bedenField = new JTextField(15);
        JTextField fiyatField = new JTextField(15);
        JTextField stokField = new JTextField(15);

        // Label ve field'ları ekle
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(new JLabel("Ürün Kodu:"), gbc);
        gbc.gridx = 1; dialog.add(kodField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Ürün Adı:"), gbc);
        gbc.gridx = 1; dialog.add(adField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1; dialog.add(kategoriField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Renk:"), gbc);
        gbc.gridx = 1; dialog.add(renkField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Beden:"), gbc);
        gbc.gridx = 1; dialog.add(bedenField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Fiyat:"), gbc);
        gbc.gridx = 1; dialog.add(fiyatField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        dialog.add(new JLabel("Stok:"), gbc);
        gbc.gridx = 1; dialog.add(stokField, gbc);

        // Butonlar
        JPanel buttonPanel = new JPanel();
        JButton kaydetBtn = new JButton("Kaydet");
        JButton iptalBtn = new JButton("İptal");

        kaydetBtn.addActionListener(e -> {
            try {
                String kod = kodField.getText().trim();
                String ad = adField.getText().trim();
                String kategori = kategoriField.getText().trim();
                String renk = renkField.getText().trim();
                String beden = bedenField.getText().trim();
                double fiyat = Double.parseDouble(fiyatField.getText().trim());
                int stok = Integer.parseInt(stokField.getText().trim());

                if (kod.isEmpty() || ad.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Ürün kodu ve adı boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                sistem.urunEkle(Integer.parseInt(kod), ad, kategori, renk, beden, fiyat, stok);
                urunTablosunuYenile();
                logEkle("✓ Yeni ürün eklendi: " + kod + " - " + ad);
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Fiyat ve stok sayısal değer olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        iptalBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(kaydetBtn);
        buttonPanel.add(iptalBtn);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void stokGuncelleDialog() {
        int selectedRow = urunTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek ürünü seçin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long urunKodu = (long) urunTable.getValueAt(selectedRow, 0);
        String mevcutStok = urunTable.getValueAt(selectedRow, 6).toString();

        String yeniStokStr = JOptionPane.showInputDialog(this,
                "Ürün: " + urunKodu + "\nMevcut Stok: " + mevcutStok + "\n\nYeni Stok Miktarı:",
                mevcutStok);

        if (yeniStokStr != null && !yeniStokStr.trim().isEmpty()) {
            try {
                int yeniStok = Integer.parseInt(yeniStokStr.trim());
                sistem.stokGuncelle(urunKodu, yeniStok);
                urunTablosunuYenile();
                logEkle("✓ Stok güncellendi: " + urunKodu + " → " + yeniStok);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Geçersiz sayı formatı!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void urunTablosunuYenile() {
        urunTableModel.updateData(new ArrayList<>(sistem.urunler.values()));
    }

    private void stokRaporuGoster() {
        List<Urun> dusukStokUrunler = sistem.dusukStokUrunler(10);
        StringBuilder rapor = new StringBuilder();
        rapor.append("=== STOK RAPORU ===\n\n");

        if (dusukStokUrunler.isEmpty()) {
            rapor.append("✓ Tüm ürünlerde yeterli stok mevcut (>10)\n");
        } else {
            rapor.append("⚠️ DÜŞÜK STOK UYARISI (≤10):\n\n");
            for (Urun urun : dusukStokUrunler) {
                rapor.append(String.format("• %s - %s: %d adet\n",
                        urun.getUrunKodu(), urun.getUrunAdi(), urun.getStokMiktari()));
            }
        }

        JTextArea textArea = new JTextArea(rapor.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Stok Raporu", JOptionPane.INFORMATION_MESSAGE);
        logEkle("📊 Stok raporu görüntülendi");
    }

    private void dusukStokUyarisi() {
        List<Urun> dusukStokUrunler = sistem.dusukStokUrunler(10);

        if (dusukStokUrunler.isEmpty()) {
            JOptionPane.showMessageDialog(this, "✓ Tüm ürünlerde yeterli stok mevcut!", "Stok Durumu", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder mesaj = new StringBuilder();
            mesaj.append("⚠️ ").append(dusukStokUrunler.size()).append(" üründe düşük stok!\n\n");
            for (Urun urun : dusukStokUrunler) {
                mesaj.append("• ").append(urun.getUrunKodu()).append(" - ").append(urun.getStokMiktari()).append(" adet\n");
            }

            JOptionPane.showMessageDialog(this, mesaj.toString(), "Düşük Stok Uyarısı", JOptionPane.WARNING_MESSAGE);
        }
        logEkle("⚠️ Düşük stok kontrolü yapıldı");
    }

    private static void logEkle(String mesaj) {
        String zaman = java.time.LocalTime.now().toString().substring(0, 8);
        logArea.append("[" + zaman + "] " + mesaj + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void ornekVerileriYukle() {

        //Örnek Müşteriler
        sistem.musteriEkle("C001", "Ali Yılmaz", "0555 123 4567", "ali@mail.com", "Istanbul");
        sistem.musteriEkle("C002", "Ayşe Demir", "0555 987 6543", "ayse@mail.com", "Ankara");

        // Örnek ürünler
        sistem.urunEkle(240530070, "Basic T-Shirt", "Tişört", "Beyaz", "70", 270.0, 100);
        sistem.urunEkle(240539980, "Basic T-Shirt", "Tişört", "Siyah", "80", 290.0, 80);
        sistem.urunEkle(240797532, "Slim Fit Pantolon", "Pantolon", "Lacivert", "32", 660.0, 50);
        sistem.urunEkle(230128705, "Kapüşonlu Sweatshirt", "Sweatshirt", "Gri", "XL", 500.0, 8); // Düşük stok
        sistem.urunEkle(230274704, "Denim Ceket", "Ceket", "Mavi", "L", 750.0, 15);

        // Örnek siparişler
        SiparisDetay detay1 = new SiparisDetay("M", 10, "Mavi", 69.99);
        SiparisDetay detay2 = new SiparisDetay("XS", 10, "Siyah",59.99);

        List<SiparisDetay> detaylar = new ArrayList<SiparisDetay>();
        detaylar.add(detay1);
        detaylar.add(detay2);

        sistem.siparisOlustur(1, 240530070, "Ayşe Demir",  20,
                LocalDate.of(2025, 5, 27), LocalDate.of(2025, 7, 27),
                detaylar, "Hızlı gelsin");

        //sistem.siparisOlustur(2, 230274704, "Ali Yılmaz",  10, LocalDate.of(2024, 4, 27));
        //sistem.siparisOlustur(3, 240539980, "Ayşe Demir",  50, LocalDate.of(2024, 4, 27));



        urunTablosunuYenile();
        siparisTablosunuYenile();
        musteriTablosunuYenile();

        logEkle("🏭 Sistem başlatıldı - Örnek veriler yüklendi");
    }

    private List<Siparis> getSiparislerFromDataSource() {
        // Bu method'u kendi veri kaynağınıza göre implement edin
        // Örnek: sistem.getAllSiparisler() veya veritabanından çekme
        List<Siparis> siparisler = new ArrayList<>();

        try {
            // Sistem üzerinden tüm siparişleri al
            // siparisler = sistem.getAllSiparisler();

            // Eğer sistem sınıfında böyle bir method yoksa,
            // mevcut siparişleri almak için başka bir yol kullanın

        } catch (Exception ex) {
            System.err.println("Sipariş verisi alınırken hata: " + ex.getMessage());
        }

        return siparisler;
    }

    //Quick product add
                    /*
                    if (!sistem.urunler.containsKey(urunNo)) {
                        int answer = JOptionPane.showConfirmDialog(null,
                                "Ürün sistemde kayıtlı değil. Yeni ürün eklensin mi?",
                                "Yeni Ürün", JOptionPane.YES_NO_OPTION);

                        if (answer == JOptionPane.YES_OPTION) {
                            String urunAdi = JOptionPane.showInputDialog(null, "Ürün Adı:");
                            String kategori = JOptionPane.showInputDialog(null, "Kategori:");
                            String renk = JOptionPane.showInputDialog(null, "Renk:");
                            String beden = JOptionPane.showInputDialog(null, "Beden:");

                            if (urunAdi != null && kategori != null && renk != null && beden != null &&
                                    !urunAdi.isEmpty() && !kategori.isEmpty() && !renk.isEmpty() && !beden.isEmpty()) {

                                sistem.urunEkle(urunNo, urunAdi, kategori, renk, beden, birimFiyat, 0);
                                logEkle("✓ Yeni ürün eklendi: " + urunNo + " - " + urunAdi);
                            } else {
                                JOptionPane.showMessageDialog(null, "Tüm bilgiler girilmeli!", "Hata", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } else {
                            return;
                        }
                    }

                     */


    private void musteriEkleDialog() {
        JTextField adField = new JTextField(10);
        JTextField soyadField = new JTextField(10);
        JTextField emailField = new JTextField(15);
        JTextField telefonField = new JTextField(15);
        JTextField adresField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Ad:"));
        panel.add(adField);
        panel.add(new JLabel("Soyad:"));
        panel.add(soyadField);
        panel.add(new JLabel("Telefon:"));
        panel.add(telefonField);
        panel.add(new JLabel("E-posta:"));
        panel.add(emailField);
        panel.add(new JLabel("Adres:"));
        panel.add(adresField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Müşteri Ekle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String ad = adField.getText().trim();
            String soyad = soyadField.getText().trim();
            String email = emailField.getText().trim();
            String telefon = telefonField.getText().trim();
            String adres = adresField.getText().trim();

            if (!ad.isEmpty() && !soyad.isEmpty() && !email.isEmpty()) {
                sistem.musteriEkle(ad, soyad, telefon, email, adres);
                musteriTablosunuYenile();
            } else {
                JOptionPane.showMessageDialog(null, "Lütfen tüm alanları doldurun.");
            }
        }
    }
    private void musteriDuzenleDialog() {
        int selectedRow = musteriTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Lütfen düzenlemek için bir müşteri seçin.");
            return;
        }

        Musteri musteri = musteriTableModel.getMusteriAt(selectedRow);

        JTextField adField = new JTextField(musteri.getAd(), 10);
        JTextField soyadField = new JTextField(musteri.getSoyad(), 10);
        JTextField emailField = new JTextField(musteri.getEmail(), 15);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Ad:"));
        panel.add(adField);
        panel.add(new JLabel("Soyad:"));
        panel.add(soyadField);
        panel.add(new JLabel("E-posta:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Müşteri Düzenle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            musteri.setAd(adField.getText().trim());
            musteri.setSoyad(soyadField.getText().trim());
            musteri.setEmail(emailField.getText().trim());
            musteriTablosunuYenile();
        }
    }
    private void musteriSil() {
        int selectedRow = musteriTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Lütfen silmek için bir müşteri seçin.");
            return;
        }

        Musteri musteri = musteriTableModel.getMusteriAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(null,
                musteri.getAd() + " " + musteri.getSoyad() + " silinsin mi?",
                "Onayla", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            sistem.musteriSil(musteri.getId());
            musteriTablosunuYenile();
        }
    }
    private void musteriTablosunuYenile() {
        musteriTableModel.setMusteriler(new ArrayList<>(sistem.musteriler.values()));
        musteriTableModel.fireTableDataChanged();
    }

    private void siparisEkleDialog() {
        if (sistem.musteriler.isEmpty() || sistem.urunler.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Sipariş eklemek için en az 1 müşteri ve 1 ürün olmalıdır.");
            return;
        }

        JTextField siparisNoField = new JTextField(5);
        JComboBox<Musteri> musteriCombo = new JComboBox<>(sistem.musteriler.values().toArray(new Musteri[0]));
        JComboBox<Urun> urunCombo = new JComboBox<>(sistem.urunler.values().toArray(new Urun[0]));
        JTextField adetField = new JTextField("1", 5);
        JTextField notlarField = new JTextField(20);
        JTextField siparisTarihiField = new JTextField("2025-06-03", 10); // Öntanımlı bugünün tarihi
        JTextField teslimTarihiField = new JTextField("2025-06-10", 10);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Sipariş No:"));
        panel.add(siparisNoField);
        panel.add(new JLabel("Müşteri:"));
        panel.add(musteriCombo);
        panel.add(new JLabel("Ürün:"));
        panel.add(urunCombo);
        panel.add(new JLabel("Toplam Adet:"));
        panel.add(adetField);
        panel.add(new JLabel("Sipariş Tarihi (YYYY-MM-DD):"));
        panel.add(siparisTarihiField);
        panel.add(new JLabel("Teslim Tarihi (YYYY-MM-DD):"));
        panel.add(teslimTarihiField);
        panel.add(new JLabel("Notlar:"));
        panel.add(notlarField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Sipariş Ekle", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int siparisNo = Integer.parseInt(siparisNoField.getText().trim());
                Musteri musteri = (Musteri) musteriCombo.getSelectedItem();
                Urun urun = (Urun) urunCombo.getSelectedItem();
                int toplamAdet = Integer.parseInt(adetField.getText().trim());
                LocalDate siparisTarihi = LocalDate.parse(siparisTarihiField.getText().trim());
                LocalDate teslimTarihi = LocalDate.parse(teslimTarihiField.getText().trim());
                String notlar = notlarField.getText().trim();

                List<SiparisDetay> detaylar = new ArrayList<>();

                boolean devam = true;
                while (devam) {
                    JTextField bedenField = new JTextField(5);
                    JTextField miktarField = new JTextField(5);
                    JTextField fiyatField = new JTextField(5);
                    JTextField renkField = new JTextField(5);

                    JPanel detayPanel = new JPanel(new GridLayout(0, 2));
                    detayPanel.add(new JLabel("Beden:"));
                    detayPanel.add(bedenField);
                    detayPanel.add(new JLabel("Miktar:"));
                    detayPanel.add(miktarField);
                    detayPanel.add(new JLabel("Renk:"));
                    detayPanel.add(renkField);
                    detayPanel.add(new JLabel("Birim Fiyat:"));
                    detayPanel.add(fiyatField);


                    Object[] options = {"Devam", "Bitir"};
                    int detayResult = JOptionPane.showOptionDialog(null, detayPanel, "Sipariş Detay Ekle",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                            null, options, options[0]);

                    if (detayResult == 0) { // Devam
                        try {
                            String beden = bedenField.getText().trim();
                            int miktar = Integer.parseInt(miktarField.getText().trim());
                            double birimFiyat = Double.parseDouble(fiyatField.getText().trim());
                            String renk = renkField.getText().trim();

                            SiparisDetay detay = new SiparisDetay(beden, miktar, renk, birimFiyat);
                            detaylar.add(detay);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Detay bilgileri geçersiz. Lütfen tekrar deneyin.");
                        }
                    } else {
                        devam = false;
                    }
                }

                // Sipariş oluşturuluyor
                Siparis yeniSiparis = new Siparis(
                        siparisNo,
                        urun.getUrunKodu(),
                        musteri.getAd(),
                        toplamAdet,
                        siparisTarihi,
                        teslimTarihi,
                        detaylar,
                        notlar
                );

                sistem.siparisler.put(siparisNo, yeniSiparis); // Veya sistem.siparisEkle(yeniSiparis);
                siparisTablosunuYenile();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Sipariş bilgileri geçersiz. " + ex.getMessage());
            }
        }
    }

    private void siparisTablosunuYenile() {
        siparisTableModel.setSiparisler(new ArrayList<>(sistem.siparisler.values()));
        siparisTableModel.fireTableDataChanged();
    }







}
