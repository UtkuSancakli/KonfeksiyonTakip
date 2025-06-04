import javax.swing.table.AbstractTableModel;
import java.util.List;

public class SiparisDetayTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Beden", "Miktar", "Renk", "Birim Fiyat (TL)", "Ara Toplam (TL)", "Durum"};
    private List<SiparisDetay> detaylar;

    public SiparisDetayTableModel(List<SiparisDetay> detaylar) {
        this.detaylar = detaylar;
    }

    public void updateDetay(int rowIndex, SiparisDetay yeniDetay) {
        if (rowIndex >= 0 && rowIndex < detaylar.size()) {
            detaylar.set(rowIndex, yeniDetay);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }


    @Override
    public int getRowCount() {
        return detaylar != null ? detaylar.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SiparisDetay detay = detaylar.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> detay.getBeden();
            case 1 -> detay.getMiktar();
            case 2 -> detay.getRenk();
            case 3 -> detay.getBirimFiyat();
            case 4 -> detay.getAraToplamFiyat();
            case 5 -> detay.getSiparisDurumu().toString();
            default -> null;
        };
    }

    public void setDetaylar(List<SiparisDetay> yeniDetaylar) {
        this.detaylar = yeniDetaylar;
        fireTableDataChanged();
    }

    public SiparisDetay getRowAt(int selectedRow) {
        return detaylar.get(selectedRow);
    }
}