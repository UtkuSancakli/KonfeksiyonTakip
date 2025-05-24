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
    private final String[] columnNames = {"Ürün Kodu", "Ürün Adı", "Kategori", "Renk", "Beden", "Fiyat", "Stok"};
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
            case 3: return urun.getRenk();
            case 4: return urun.getBeden();
            case 5: return String.format("%.2f TL", urun.getFiyat());
            case 6: return urun.getStokMiktari();
            default: return null;
        }
    }

    public void updateData(List<Urun> yeniUrunler) {
        this.urunler = yeniUrunler;
        fireTableDataChanged();
    }
}

