public class SiparisDetay {

    private int miktar;
    private String beden;
    private double birimFiyat;
    private SiparisDurumu siparisDurumu;
    private String renk;

    public SiparisDetay(String beden, int miktar, String renk, double birimFiyat) {
        this.beden = beden;
        this.miktar = miktar;
        this.renk = renk;
        this.birimFiyat = birimFiyat;
        this.siparisDurumu = SiparisDurumu.YENI;
    }

    public int getMiktar() { return miktar; }
    public double getBirimFiyat() { return birimFiyat; }
    public double getAraToplamFiyat() { return miktar * birimFiyat; }
    public String getBeden() { return beden; }
    public String getRenk() { return renk; }
    public SiparisDurumu getSiparisDurumu() {
        return siparisDurumu;
    }


    @Override
    public String toString() {
        return String.format("  - x%d %s (%.2f TL) = %.2f TL",
                miktar, renk, birimFiyat, getAraToplamFiyat());
    }

    public SiparisDetay deepCopy() {
        return new SiparisDetay(this.beden, this.miktar, this.renk, this.birimFiyat);
    }
}
