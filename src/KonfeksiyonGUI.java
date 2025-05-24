import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class KonfeksiyonGUI extends JFrame {
    private KonfeksiyonTakipSistemi sistem;
    private JTabbedPane tabbedPane;
    private UrunTableModel urunTableModel;
    private JTable urunTable;
    private JTextArea logArea;

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
        urunTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
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

        // Butonlar
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton musteriEkleBtn = new JButton("➕ Müşteri Ekle");
        JButton yenileBtn = new JButton("↻ Yenile");

        buttonPanel.add(musteriEkleBtn);
        buttonPanel.add(yenileBtn);
        panel.add(buttonPanel, BorderLayout.NORTH);

        // Müşteri tablosu
        DefaultTableModel musteriModel = new DefaultTableModel(new Object[]{"ID", "Ad Soyad", "Telefon", "E-posta"}, 0);
        JTable musteriTable = new JTable(musteriModel);
        JScrollPane scrollPane = new JScrollPane(musteriTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Örnek veri ve işlevsellik
        List<String[]> musteriler = new ArrayList<>();
        musteriler.add(new String[]{"C001", "Ali Yılmaz", "0555 123 4567", "ali@mail.com"});
        musteriler.add(new String[]{"C002", "Ayşe Demir", "0555 987 6543", "ayse@mail.com"});

        for (String[] m : musteriler) {
            musteriModel.addRow(m);
        }

        // Müşteri Ekle dialogu
        musteriEkleBtn.addActionListener(e -> {
            JTextField idField = new JTextField(10);
            JTextField adField = new JTextField(15);
            JTextField telField = new JTextField(12);
            JTextField mailField = new JTextField(15);

            JPanel inputPanel = new JPanel(new GridLayout(4, 2));
            inputPanel.add(new JLabel("ID:")); inputPanel.add(idField);
            inputPanel.add(new JLabel("Ad Soyad:")); inputPanel.add(adField);
            inputPanel.add(new JLabel("Telefon:")); inputPanel.add(telField);
            inputPanel.add(new JLabel("E-posta:")); inputPanel.add(mailField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Müşteri Ekle", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                musteriModel.addRow(new Object[]{idField.getText(), adField.getText(), telField.getText(), mailField.getText()});
                logEkle("✓ Yeni müşteri eklendi: " + idField.getText() + " - " + adField.getText());
            }
        });

        yenileBtn.addActionListener(e -> {
            logEkle("👥 Müşteri tablosu yenilendi");
        });

        return panel;
    }

    private JPanel createSiparisPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Butonlar
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton siparisEkleBtn = new JButton("➕ Sipariş Ekle");
        JButton yenileBtn = new JButton("↻ Yenile");

        buttonPanel.add(siparisEkleBtn);
        buttonPanel.add(yenileBtn);
        panel.add(buttonPanel, BorderLayout.NORTH);

        // Sipariş tablosu
        DefaultTableModel siparisModel = new DefaultTableModel(
                new Object[]{"Sipariş No", "Müşteri", "Ürün", "Adet", "Toplam Fiyat"}, 0);
        JTable siparisTable = new JTable(siparisModel);
        JScrollPane scrollPane = new JScrollPane(siparisTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Örnek veri
        siparisModel.addRow(new Object[]{"S001", "Ali Yılmaz", "Basic T-Shirt", 2, "90.00 TL"});
        siparisModel.addRow(new Object[]{"S002", "Ayşe Demir", "Denim Ceket", 1, "180.00 TL"});

        // Sipariş Ekle dialogu
        siparisEkleBtn.addActionListener(e -> {
            JTextField noField = new JTextField(10);
            JTextField musteriField = new JTextField(15);
            JTextField urunField = new JTextField(15);
            JTextField adetField = new JTextField(5);

            JPanel inputPanel = new JPanel(new GridLayout(4, 2));
            inputPanel.add(new JLabel("Sipariş No:")); inputPanel.add(noField);
            inputPanel.add(new JLabel("Müşteri:")); inputPanel.add(musteriField);
            inputPanel.add(new JLabel("Ürün:")); inputPanel.add(urunField);
            inputPanel.add(new JLabel("Adet:")); inputPanel.add(adetField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Sipariş Ekle", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    int adet = Integer.parseInt(adetField.getText());
                    Urun dummy = new Urun("N/A", "Bilinmeyen", "N/A", "N/A", "N/A", 0.0, 0);
                    double fiyat = sistem.urunler.getOrDefault(urunField.getText().trim(), dummy).getFiyat();
                    double toplam = fiyat * adet;
                    siparisModel.addRow(new Object[]{
                            noField.getText(), musteriField.getText(), urunField.getText(), adet, toplam + " TL"});
                    logEkle("✓ Yeni sipariş eklendi: " + noField.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Geçersiz giriş!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        yenileBtn.addActionListener(e -> {
            logEkle("📋 Sipariş tablosu yenilendi");
        });

        return panel;
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

                sistem.urunEkle(kod, ad, kategori, renk, beden, fiyat, stok);
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

        String urunKodu = (String) urunTable.getValueAt(selectedRow, 0);
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

    private void logEkle(String mesaj) {
        String zaman = java.time.LocalTime.now().toString().substring(0, 8);
        logArea.append("[" + zaman + "] " + mesaj + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void ornekVerileriYukle() {
        // Örnek ürünler
        sistem.urunEkle("T001", "Basic T-Shirt", "Tişört", "Beyaz", "M", 45.00, 100);
        sistem.urunEkle("T002", "Basic T-Shirt", "Tişört", "Siyah", "L", 45.00, 80);
        sistem.urunEkle("P001", "Slim Fit Pantolon", "Pantolon", "Lacivert", "32", 120.00, 50);
        sistem.urunEkle("G001", "Kapüşonlu Sweatshirt", "Sweatshirt", "Gri", "XL", 85.00, 8); // Düşük stok
        sistem.urunEkle("J001", "Denim Ceket", "Ceket", "Mavi", "L", 180.00, 15);

        urunTablosunuYenile();
        logEkle("🏭 Sistem başlatıldı - Örnek veriler yüklendi");
    }
}
