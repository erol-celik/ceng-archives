public class Media {
    private String imdbId;
    private String title;
    private String mediaType;
    private List<String> genres;
    private int releaseYear;
    private double imdbAverageRating;
    private int imdbNumVotes;
    private List<PlatformAndCountries> platformAndCountries;

    public Media(String url, String imdbId, String title, String mediaType, List<String> genres, int releaseYear, double imdbAverageRating, int imdbNumVotes, String platforms, String[] countries) {
        this.imdbId = imdbId;
        this.title = title;
        this.mediaType = mediaType;
        this.genres = genres;
        this.releaseYear = releaseYear;
        this.imdbAverageRating = imdbAverageRating;
        this.imdbNumVotes = imdbNumVotes;
        this.platformAndCountries = new List<PlatformAndCountries>();
        platformAndCountries.add(new PlatformAndCountries(platforms, countries,url));
    }

    public List<PlatformAndCountries> getPlatformAndCountries() {
        return platformAndCountries;
    }

    public int getPlatformSize() {
        return platformAndCountries.getLength();
    }

    public void printPlatformAndCountries(List<PlatformAndCountries> pnc) {
        System.out.println(pnc.getLength() + " Platforms Found For " + getTitle()+"\n");
        for (int i = 1; i < pnc.getLength() + 1; i++) {
            System.out.println(pnc.getEntry(i).getPlatformAndCountries()+"\n");
        }
    }

    public void addPlatformAndCountry(String platform, String[] countries,String url) {
        platformAndCountries.add(new PlatformAndCountries(platform, countries,url));
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getGenres() {
        return genres.displayList();
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public double getImdbAverageRating() {
        return imdbAverageRating;
    }

    public int getImdbNumVotes() {
        return imdbNumVotes;
    }

    public String getImdbId() {
        return imdbId;
    }

}



class PlatformAndCountries {

    private String url;
    private String platform;
    private String[] countires;

    PlatformAndCountries(String platform, String[] countires,String url) {
        this.url=url;
        this.platform = platform;
        this.countires = countires;
    }

    public String printCountires() {
        String returnCountries="";
        for (int i=0;i<countires.length;i++){
            returnCountries=returnCountries+countires[i]+" ";
        }
        return returnCountries;
    }

    public String[] getCountires() {
        return this.countires ;
    }

    public String getPlatform() {
        return platform;
    }

    public String getUrl() {return url;}

    public String getPlatformAndCountries() {
        return getPlatform() + " - " + printCountires()+"\n"+getUrl();
    }

}
