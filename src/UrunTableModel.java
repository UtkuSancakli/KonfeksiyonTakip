import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

// GUI için gerekli Table Model sınıfları
class UrunTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Ürün Kodu", "Ürün Adı", "Kategori", "Fiyat", "Stok"};
    private List<Urun> urunler;

    public UrunTableModel(List<Urun> urunler) {
        this.urunler = urunler;
    }

    @Override
    public int getRowCount() { return urunler.size(); }

    @Override
    public int getColumnCount() { return columnNames.length; }

    @Override
    public String getColumnName(int col) { return columnNames[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Urun urun = urunler.get(row);
        switch (col) {
            case 0: return urun.getUrunKodu();
            case 1: return urun.getUrunAdi();
            case 2: return urun.getKategori();
            case 3: return String.format("%.2f TL", urun.getFiyat());
            case 4: return urun.getStokMiktari();
            default: return null;
        }
    }

    public void updateData(List<Urun> yeniUrunler) {
        this.urunler = yeniUrunler;
        fireTableDataChanged();
    }

    public void setRowCount(int i) {
        if (i == 0) {
            urunler.clear();
            fireTableDataChanged();
        } else if (i < urunler.size()) {
            urunler = new ArrayList<>(urunler.subList(0, i));
            fireTableDataChanged();
        }
        // Eğer i > urunler.size() ise, genelde tabloya boş satır eklemek istenmez. Gerekirse orası da yapılabilir.
    }

    public void addRow(Object[] row) {
        if (row.length < 7) return;

        try {
            Urun yeniUrun = new Urun(
                    Integer.parseInt(row[0].toString()),    // Ürün Kodu
                    row[1].toString(),                      // Ürün Adı
                    row[2].toString(),                      // Kategori
                    Double.parseDouble(row[3].toString().replace(" TL", "").replace(",", ".")),  // Fiyat
                    Integer.parseInt(row[4].toString())     // Stok
            );
            urunler.add(yeniUrun);
            fireTableRowsInserted(urunler.size() - 1, urunler.size() - 1);
        } catch (Exception e) {
            System.err.println("Satır eklenemedi: " + e.getMessage());
        }
    }

}

