import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Siparis {
    private int siparisNo;
    private long urunNo;
    private int toplamAdet;
    private String musteriAdi;
    private LocalDate siparisTarihi;
    private LocalDate teslimTarihi;
    private boolean isReady;
    private List<SiparisDetay> detaylar;
    private String notlar;

    public Siparis(int siparisNo, long urunNo, String musteriAdi, int toplamAdet, LocalDate siparisTarihi, LocalDate teslimTarihi, List<SiparisDetay> detaylar, String notlar) {

        this.siparisNo = siparisNo;
        this.urunNo = urunNo;
        this.musteriAdi = musteriAdi;
        this.toplamAdet = toplamAdet;
        this.siparisTarihi = siparisTarihi;
        this.teslimTarihi = teslimTarihi;
        this.isReady = false;
        this.detaylar = detaylar;
        this.notlar = notlar;
    }

    public void urunEkle(String beden, int miktar, String renk, double birimFiyat) {
        detaylar.add(new SiparisDetay(beden, miktar, renk, birimFiyat));
    }


    // Getter metodları
    public int getSiparisNo() { return siparisNo; }
    public long getUrunNo() {
        return urunNo;
    }
    public String getMusteriAdi() { return musteriAdi; }
    public LocalDate getSiparisTarihi() { return siparisTarihi; }
    public LocalDate getTeslimTarihi() { return teslimTarihi; }
    public boolean getDurum() { return isReady; }
    public int getToplamAdet() { return toplamAdet; }
    public List<SiparisDetay> getDetaylar() { return detaylar; }
    public String getNotlar() { return notlar; }
    public double getToplamFiyat() {
        return detaylar.stream().mapToDouble(SiparisDetay::getAraToplamFiyat).sum();
    }

    public void setTeslimTarihi(LocalDate teslimTarihi) { this.teslimTarihi = teslimTarihi; }
    public void setDurum(boolean durum) { this.isReady = durum; }
    public void setNotlar(String notlar) { this.notlar = notlar; }
    public void setSiparisNo(int siparisNo) {
        this.siparisNo = siparisNo;
    }
    public void setUrunNo(long urunNo) {
        this.urunNo = urunNo;
    }
    public void setMusteriAdi(String musteriAdi) {
        this.musteriAdi = musteriAdi;
    }
    public void setSiparisTarihi(LocalDate siparisTarihi) {
        this.siparisTarihi = siparisTarihi;
    }
    public void setDetaylar(List<SiparisDetay> detaylar) {
        this.detaylar = detaylar;
    }
    public void setNotlar() {
        this.notlar = notlar;
    }
    public void setToplamAdet(int toplamAdet) {
        this.toplamAdet = toplamAdet;
    }
    public void setDurum(String durum) {
        this.isReady = durum.equals("Hazır") || durum.equals("hazır");
    }

    public Siparis deepCopy() {
        List<SiparisDetay> detayKopya = new ArrayList<>();
        for (SiparisDetay d : this.detaylar) {
            detayKopya.add(d.deepCopy());
        }
        Siparis yeni = new Siparis(
                this.siparisNo,
                this.urunNo,
                this.musteriAdi,
                this.toplamAdet,
                this.siparisTarihi,
                this.teslimTarihi,
                detayKopya,
                this.notlar // varsayılan olarak immutable ya da kopyalanabilir
        );
        yeni.setDurum(this.isReady);
        return yeni;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //sb.append(String.format("Sipariş No: %s\n", siparisNo));
        sb.append(String.format("Müşteri ID: %s\n", musteriAdi));
        sb.append(String.format("Tarih: %s\n", siparisTarihi));
        sb.append(String.format("Durum: %s\n", isReady));
        sb.append("Ürünler:\n");
        for (SiparisDetay detay : detaylar) {
            sb.append(detay.toString()).append("\n");
        }
        sb.append(String.format("Toplam: %.2f TL\n", getToplamFiyat()));
        if (notlar != null && !notlar.isEmpty()) {
            sb.append(String.format("Notlar: %s\n", notlar));
        }
        return sb.toString();
    }


}