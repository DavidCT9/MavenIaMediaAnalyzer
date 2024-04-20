package AiMediaAnalyzer.externalTools.mediaHandler;

public class VideoObj {
    public float duration;
    public String iaImagePath;
    public String mapImagePath;
    public String captions;

    public String getMapImagePath() {
        return mapImagePath;
    }

    public void setMapImagePath(String mapImagePath) {
        this.mapImagePath = mapImagePath;
    }

    public String getCaptions() {
        return captions;
    }

    public void setCaptions(String captions) {
        this.captions = captions;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getIaImagePath() {
        return iaImagePath;
    }

    public void setIaImagePath(String iaImagePath) {
        this.iaImagePath = iaImagePath;
    }

    public VideoObj(String mapImagePath, String iaImagePath, String captions) {
        setIaImagePath(iaImagePath);
        setMapImagePath(mapImagePath);
        setCaptions(captions);
    }
}
