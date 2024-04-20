package AiMediaAnalyzer.externalTools.mediaHandler;

public class MediaObj {
    private String absolutePath;
    private String latitude;
    private String longitude;
    private String dateOriginal="";
    public String url;
    public String iaDescription;
    public boolean isVideo;


    public MediaObj(String absolutePath, String latitude, String longitude, String dateOriginal) {
        setAbsolutePath(absolutePath);
        setLatitude(latitude);
        setLongitude(longitude);
        setDateOriginal(dateOriginal);
        setVideo(false);
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public String getIaDescription() {
        return iaDescription;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setIaDescription(String iaDescription) {
        this.iaDescription = iaDescription;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    private void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getLatitude() {
        return latitude;
    }

    private void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    private void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDateOriginal() {
        return dateOriginal;
    }

    private void setDateOriginal(String dateOriginal) {
        this.dateOriginal = dateOriginal;
    }

}
