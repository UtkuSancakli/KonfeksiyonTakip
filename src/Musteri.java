public class Musteri {
    private final String musteriId;
    private String ad;
    private String soyad;
    private String telefon;
    private String email;
    private String adres;

    public Musteri(String musteriId, String ad, String soyad, String telefon, String email, String adres) {
        this.musteriId = musteriId;
        this.ad = ad;
        this.soyad = soyad;
        this.telefon = telefon;
        this.email = email;
        this.adres = adres;
    }

    public String getMusteriId() { return musteriId; }
    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getTelefon() { return telefon; }
    public String getEmail() { return email; }
    public String getAdres() { return adres; }

    @Override
    public String toString() {
        return String.format("Müşteri[%s - %s %s - Tel: %s]", musteriId, ad, soyad, telefon);
    }
}