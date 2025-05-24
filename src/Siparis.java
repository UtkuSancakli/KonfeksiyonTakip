import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Siparis {
    private String siparisNo;
    private String musteriId;
    private LocalDateTime siparisTarihi;
    private LocalDate teslimTarihi;
    private SiparisDurumu durum;
    private List<SiparisDetay> detaylar;
    private String notlar;

    public Siparis(String siparisNo, String musteriId) {
        this.siparisNo = siparisNo;
        this.musteriId = musteriId;
        this.siparisTarihi = LocalDateTime.now();
        this.durum = SiparisDurumu.YENI;
        this.detaylar = new ArrayList<>();
    }

    public void urunEkle(String urunKodu, int miktar, double birimFiyat) {
        detaylar.add(new SiparisDetay(urunKodu, miktar, birimFiyat));
    }

    public double getToplamTutar() {
        return detaylar.stream().mapToDouble(SiparisDetay::getToplamFiyat).sum();
    }

    // Getter metodları
    public String getSiparisNo() { return siparisNo; }
    public String getMusteriId() { return musteriId; }
    public LocalDateTime getSiparisTarihi() { return siparisTarihi; }
    public LocalDate getTeslimTarihi() { return teslimTarihi; }
    public SiparisDurumu getDurum() { return durum; }
    public List<SiparisDetay> getDetaylar() { return detaylar; }
    public String getNotlar() { return notlar; }

    public void setTeslimTarihi(LocalDate teslimTarihi) { this.teslimTarihi = teslimTarihi; }
    public void setDurum(SiparisDurumu durum) { this.durum = durum; }
    public void setNotlar(String notlar) { this.notlar = notlar; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Sipariş No: %s\n", siparisNo));
        sb.append(String.format("Müşteri ID: %s\n", musteriId));
        sb.append(String.format("Tarih: %s\n", siparisTarihi.toLocalDate()));
        sb.append(String.format("Durum: %s\n", durum));
        sb.append("Ürünler:\n");
        for (SiparisDetay detay : detaylar) {
            sb.append(detay.toString()).append("\n");
        }
        sb.append(String.format("Toplam: %.2f TL\n", getToplamTutar()));
        if (notlar != null && !notlar.isEmpty()) {
            sb.append(String.format("Notlar: %s\n", notlar));
        }
        return sb.toString();
    }
}