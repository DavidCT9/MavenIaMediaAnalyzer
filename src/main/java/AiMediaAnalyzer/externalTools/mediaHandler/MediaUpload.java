package AiMediaAnalyzer.externalTools.mediaHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class MediaUpload {
    static IOHandler inputOutput = new IOConsole();

    public void cloudinaryHandler(MediaObj[] mediaArray) {

        try {
            for (MediaObj mediaFile : mediaArray){

                String filePath = mediaFile.getAbsolutePath();
                String publicId = mediaFile.getDateOriginal();

                mediaFile.setUrl(cloudinaryUpload(filePath, publicId));
                if (mediaFile.getUrl()!=null){
                    inputOutput.showInfo("Upload successful!");
                    inputOutput.showInfo("Public URL: " + mediaFile.getUrl());
                }

            }
        }catch (Exception e){
            inputOutput.showInfo("All files couldn't be uploaded, check your media and try again");
        }



    }

    public static String cloudinaryUpload(String filePath, String publicId) {
        Cloudinary cloudinary = CloudinarySingleton.getCloudinary();
        String publicUrl = null;
        try {
            String fileType = Files.probeContentType(Paths.get(filePath));

            Map uploadResult;
            if (fileType != null) {
                if (fileType.startsWith("image/")) {
                    uploadResult = cloudinary.uploader().upload(filePath,
                            ObjectUtils.asMap(
                                    "public_id", publicId,
                                    "resource_type", "image"
                            )
                    );
                } else if (fileType.startsWith("video/")) {
                    String thumbnailImg = getVideoThumbnail(filePath, publicId);

                    uploadResult = cloudinary.uploader().upload(thumbnailImg,
                            ObjectUtils.asMap(
                                    "public_id", "Thumbnail"+publicId,
                                    "resource_type", "image"
                            )
                    );
                } else {
                    System.err.println("Unsupported file type for upload: " + filePath);
                    return null;
                }
            } else {
                System.err.println("Failed to determine file type: " + filePath);
                return null;
            }

            publicUrl = (String) uploadResult.get("url");

        } catch (Exception e) {
            System.err.println("Error uploading file: " + e.getMessage());
            return null;
        }

        return publicUrl;
    }


    private static String getVideoThumbnail(String filePath, String fileName){
        System.out.println(filePath);
        try {
            Process metadataProcess = Runtime.getRuntime().exec("ffmpeg -i "+filePath+" -vframes 1 "+fileName+".jpg");
        } catch (IOException e) {
            inputOutput.showInfo("Cant execute the thumbnail command: " + e.getMessage());
            e.printStackTrace();
        }
        //thumbnailFilePath = "C:\\Users\\david\\Documents\\DAVID\\UP\\4_Semestre\\ComputerGraphics\\ProjectP2\\MavenIaMediaAnalyzer\\"+fileName+".jpg";
        return "C:\\Users\\david\\Documents\\DAVID\\UP\\4_Semestre\\ComputerGraphics\\ProjectP2\\AiMediaAnalyze\\Media\\"+fileName+".jpg";

    }



}