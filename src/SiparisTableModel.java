import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class SiparisTableModel extends AbstractTableModel {
    private final String[] columnNames = {"siparisNo", "musteriAdi", "urunNo", "Kapalı mı?", "toplamAdet", "toplamFiyat", "siparisTarihi", "teslimTarihi"};
    private List<Siparis> siparisler;

    public SiparisTableModel(List<Siparis> siparisler) {
        this.siparisler = siparisler;
    }

    @Override
    public int getRowCount() {
        return siparisler.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return switch (col) {
            case 0 -> "Sipariş No";
            case 1 -> "Müşteri Adı";
            case 2 -> "Ürün No";
            case 3 -> "Durum";
            case 4 -> "Toplam Adet";
            case 5 -> "Toplam Fiyat";
            case 6 -> "Sipariş Tarihi";
            case 7 -> "Teslim Tarihi";
            default -> columnNames[col];
        };
    }

    @Override
    public Object getValueAt(int row, int col) {
        Siparis siparis = siparisler.get(row);
        return switch (col) {
            case 0 -> siparis.getSiparisNo();
            case 1 -> siparis.getMusteriAdi();
            case 2 -> siparis.getUrunNo();
            case 3 -> siparis.getDurum();
            case 4 -> siparis.getToplamAdet();
            case 5 -> siparis.getToplamFiyat();
            case 6 -> siparis.getSiparisTarihi();
            case 7 -> siparis.getTeslimTarihi();
            default -> null;
        };
    }

    public void updateData(List<Siparis> yeniSiparisler) {
        this.siparisler = yeniSiparisler;
        fireTableDataChanged();
    }

    public Siparis getSiparisAt(int row) {
        if (row >= 0 && row < siparisler.size()) {
            return siparisler.get(row);
        }
        return null;
    }

    public void setRowCount(int i) {
        if (i == 0) {
            siparisler.clear();
            fireTableDataChanged();
        } else if (i < siparisler.size()) {
            siparisler = new ArrayList<>(siparisler.subList(0, i));
            fireTableDataChanged();
        }
    }

    public void addRow(Object[] row) {
        if (row.length < 8) return;

        try {
            int siparisNo = Integer.parseInt(row[0].toString());
            String musteriAdi = row[1].toString();
            long urunNo = Long.parseLong(row[2].toString());
            boolean durum = Boolean.parseBoolean(row[3].toString()); // true/false bekleniyor
            int toplamAdet = Integer.parseInt(row[4].toString());
            // toplamFiyat hesaptan gelecek, doğrudan alınmaz
            LocalDate siparisTarihi = LocalDate.parse(row[6].toString());
            LocalDate teslimTarihi = LocalDate.parse(row[7].toString());

            // Detaylar için varsayılan tek detay oluşturuluyor, istenirse özelleştirilebilir
            List<SiparisDetay> detaylar = new ArrayList<>();
            double varsayilanFiyat = 100.0; // gerekirse değiştirilebilir
            detaylar.add(new SiparisDetay("Standart", toplamAdet, varsayilanFiyat));

            Siparis siparis = new Siparis(siparisNo, urunNo, musteriAdi, toplamAdet, siparisTarihi, teslimTarihi, detaylar, null);
            siparis.setDurum(durum); // true/false

            siparisler.add(siparis);
            fireTableRowsInserted(siparisler.size() - 1, siparisler.size() - 1);
        } catch (Exception e) {
            System.err.println("Sipariş eklenemedi: " + e.getMessage());
        }
    }


    public void setSiparisler(ArrayList<Siparis> siparisler) {
        this.siparisler = siparisler;
    }
}