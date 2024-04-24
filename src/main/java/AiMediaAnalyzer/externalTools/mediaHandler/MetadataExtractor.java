package AiMediaAnalyzer.externalTools.mediaHandler;


import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class MetadataExtractor {

    private String mediaFolderPath;
    private File folder;
    private File[] mediaFiles;
    IOHandler inputOutput = new IOConsole();
    public String getmediaFolderPath() {
        return mediaFolderPath;
    }

    public void setmediaFolderPath(String mediaFolderPath) {
        this.mediaFolderPath = mediaFolderPath;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public File[] getMediaFiles() {
        return mediaFiles;
    }

    private void setMediaFiles(File[] mediaFiles) {
        this.mediaFiles = mediaFiles;
    }


    public MediaObj[] ObtainMetadata(String folderPath) {
        String tempLat = "";
        String tempLon ="";
        String tempDate = "";
        setmediaFolderPath(folderPath);

        setFolder(new File(getmediaFolderPath()));
        setMediaFiles(getFolder().listFiles());
        MediaObj[] mediaArray = new MediaObj[getMediaFiles().length];

        int mediaObjCounter=0;
        if (getMediaFiles() != null) {
            for (File file : getMediaFiles()) {
                if (file.isFile()) {
                    try {
                        Process metadataProcess = Runtime.getRuntime().exec("exiftool -createdate -GPSLatitude -GPSLongitude \"" + file.getAbsolutePath() + "\"");
                        String tempMediaPath = file.getAbsolutePath();
                        BufferedReader fileReader = new BufferedReader(new InputStreamReader(metadataProcess.getInputStream()));
                        String readLine;
                        while ((readLine = fileReader.readLine()) != null){
                            if (readLine.startsWith("Create Date")){
                                String[] parts = readLine.split(":");
                                tempDate="";
                                for (int i=1; i<parts.length; i++){
                                    tempDate += parts[i].trim();
                                }
                                //Erase the space that separates the dat from the hour
                                tempDate = tempDate.trim().replace(" ", "");

                            }
                            else if (readLine.startsWith("GPS Latitude")){
                                String[] parts = readLine.split(":");
                                tempLat = parts[1].trim();
                            }
                            else if (readLine.startsWith("GPS Longitude")){
                                String[] parts = readLine.split(":");
                                tempLon = parts[1].trim();

                            }

                        }

                        metadataProcess.waitFor();
                        mediaArray[mediaObjCounter] = new MediaObj(tempMediaPath,tempLat, tempLon, tempDate);
                        inputOutput.showInfo(mediaArray[mediaObjCounter].getLatitude()+" "+mediaArray[mediaObjCounter].getLongitude()+" "+mediaArray[mediaObjCounter].getDateOriginal());
                        mediaObjCounter+=1;

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            inputOutput.showInfo("The folder is empty or corrupted, please try with another one");
        }

        return mediaArray;
    }
}

