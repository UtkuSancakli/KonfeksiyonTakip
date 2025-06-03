public class Musteri {
    private final int musteriId;
    private String ad;
    private String soyad;
    private String telefon;
    private String email;
    private String adres;

    public Musteri(int musteriId, String ad, String soyad, String telefon, String email, String adres) {
        this.musteriId = musteriId;
        this.ad = ad;
        this.soyad = soyad;
        this.telefon = telefon;
        this.email = email;
        this.adres = adres;
    }

    public int getId() { return musteriId; }
    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getTelefon() { return telefon; }
    public String getEmail() { return email; }
    public String getAdres() { return adres; }

    public void setAdres(String adres) { this.adres = adres; }
    public void setSoyad(String soyad) { this.soyad = soyad; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
    public void setEmail(String email) { this.email = email; }
    public void setAd(String ad) { this.ad = ad; }


    @Override
    public String toString() {
        return String.format("Müşteri[%s - %s %s - Tel: %s]", musteriId, ad, soyad, telefon);
    }
}