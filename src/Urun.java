public class Urun {
    private final long urunKodu;
    private final String urunAdi;
    private final String kategori;
    private final String renk;
    private final String beden;
    private double fiyat;
    private int stokMiktari;

    public Urun(long urunKodu, String urunAdi, String kategori, String renk, String beden, double fiyat, int stokMiktari) {
        this.urunKodu = urunKodu;
        this.urunAdi = urunAdi;
        this.kategori = kategori;
        this.renk = renk;
        this.beden = beden;
        this.fiyat = fiyat;
        this.stokMiktari = stokMiktari;
    }

    // Getter ve Setter metodları
    public long getUrunKodu() { return urunKodu; }
    public String getUrunAdi() { return urunAdi; }
    public String getKategori() { return kategori; }
    public String getRenk() { return renk; }
    public String getBeden() { return beden; }
    public double getFiyat() { return fiyat; }
    public int getStokMiktari() { return stokMiktari; }
    public void setStokMiktari(int stokMiktari) { this.stokMiktari = stokMiktari; }

    @Override
    public String toString() {
        return String.format("Ürün[%s - %s %s %s - Fiyat: %.2f TL - Stok: %d]",
                urunKodu, urunAdi, renk, beden, fiyat, stokMiktari);
    }
}