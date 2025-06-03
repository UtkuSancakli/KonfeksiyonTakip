import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

// GUI için gerekli Table Model sınıfları
class MusteriTableModel extends AbstractTableModel {
    private final String[] columnNames = {"musteriId","ad", "soyad", "telefon", "email", "adres"};
    private List<Musteri> musteriler;

    public MusteriTableModel(List<Musteri> musterilerler) {
        this.musteriler = musterilerler;
    }

    @Override
    public int getRowCount() { return musteriler.size(); }

    @Override
    public int getColumnCount() { return columnNames.length; }

    @Override
    public String getColumnName(int col) { return columnNames[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Musteri musteri = musteriler.get(row);
        return switch (col) {
            case 0 -> musteri.getId();
            case 1 -> musteri.getAd();
            case 2 -> musteri.getSoyad();
            case 3 -> musteri.getTelefon();
            case 4 -> musteri.getEmail();
            case 5 -> musteri.getAdres();
            default -> null;
        };
    }

    public Musteri getMusteriAt(int row) {
        return musteriler.get(row);
    }

    public void updateData(List<Musteri> yeniMusteriler) {
        this.musteriler = yeniMusteriler;
        fireTableDataChanged();
    }

    public void setRowCount(int i) {
        if (i == 0) {
            musteriler.clear();
            fireTableDataChanged();
        } else if (i < musteriler.size()) {
            musteriler = new ArrayList<>(musteriler.subList(0, i));
            fireTableDataChanged();
        }
    }

    public void addRow(Object[] row) {
        if (row.length < 6) return;

        try {
            Musteri yeniMusteri = new Musteri(
                    Integer.parseInt(row[0].toString()), // musteriId
                    row[1].toString(),                   // ad
                    row[2].toString(),                   // soyad
                    row[3].toString(),                   // telefon
                    row[4].toString(),                   // email
                    row[5].toString()                    // adres
            );
            musteriler.add(yeniMusteri);
            fireTableRowsInserted(musteriler.size() - 1, musteriler.size() - 1);
        } catch (Exception e) {
            System.err.println("Müşteri eklenemedi: " + e.getMessage());
        }
    }

    public void setMusteriler(ArrayList<Musteri> musteris) {
        this.musteriler = musteris;
    }
}
