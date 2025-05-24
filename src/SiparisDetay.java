public class SiparisDetay {
    private String urunKodu;
    private int miktar;
    private double birimFiyat;

    public SiparisDetay(String urunKodu, int miktar, double birimFiyat) {
        this.urunKodu = urunKodu;
        this.miktar = miktar;
        this.birimFiyat = birimFiyat;
    }

    public String getUrunKodu() { return urunKodu; }
    public int getMiktar() { return miktar; }
    public double getBirimFiyat() { return birimFiyat; }
    public double getToplamFiyat() { return miktar * birimFiyat; }

    @Override
    public String toString() {
        return String.format("  - %s x%d (%.2f TL) = %.2f TL",
                urunKodu, miktar, birimFiyat, getToplamFiyat());
    }
}
