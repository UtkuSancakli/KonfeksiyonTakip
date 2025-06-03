public class SiparisDetay {

    private int miktar;
    private String beden;
    private double birimFiyat;
    private SiparisDurumu siparisDurumu;

    public SiparisDetay(String beden, int miktar, double birimFiyat) {
        this.beden = beden;
        this.miktar = miktar;
        this.birimFiyat = birimFiyat;
        this.siparisDurumu = SiparisDurumu.YENI;
    }

    public int getMiktar() { return miktar; }
    public double getBirimFiyat() { return birimFiyat; }
    public double getAraToplamFiyat() { return miktar * birimFiyat; }
    public String getBeden() { return beden; }
    public SiparisDurumu getSiparisDurumu() {
        return siparisDurumu;
    }


    @Override
    public String toString() {
        return String.format("  - x%d (%.2f TL) = %.2f TL",
                miktar, birimFiyat, getAraToplamFiyat());
    }
}
