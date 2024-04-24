package AiMediaAnalyzer.externalTools.mediaHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

public class MediaUpload {
    static IOHandler inputOutput = new IOConsole();

    /**
     * Cycle the media objects array just to upload the
     * media and handle upload errors
     * @author David
     */
    public void cloudinaryHandler(MediaObj[] mediaArray) {

        try {
            for (MediaObj mediaFile : mediaArray) {

                String filePath = mediaFile.getAbsolutePath();
                String publicId = mediaFile.getDateOriginal();

                mediaFile.setUrl(cloudinaryUpload(filePath, publicId));
                if (mediaFile.getUrl() != null) {
                    inputOutput.showInfo("Upload successful!");
                    inputOutput.showInfo("Public URL: " + mediaFile.getUrl());
                    inputOutput.showInfo("Local URL: " + mediaFile.getAbsolutePath());
                } else {
                    inputOutput.showInfo("Upload failed! Null Url. Path: "+mediaFile.getAbsolutePath());
                    mediaArray = deleteObj(mediaArray, mediaFile);
                }

            }
        } catch (Exception e) {
            inputOutput.showInfo("All files couldn't be uploaded, check your media and try again");
        }


    }

    /**
     * Classifies the media object if it is a video or image and call
     * the method according to the file, handle file mismatch
     * @return The public url of the media object.
     * @author David
     */
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
                                    "public_id", "Thumbnail" + publicId,
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

    /**
     * Execute an easy ffmpeg command to extract the thumbnail of a video
     * @return The path of the thumbnail.
     * @author David
     */
    private static String getVideoThumbnail(String filePath, String fileName) {
        String[] command;
        command = new String[]{"ffmpeg", "-i", filePath, "-vf", "select='eq(n,0)'",
                "-vframes", "1", fileName + ".jpg"};

        VideoGenerator.processCommand(command);

        return fileName + ".jpg";
    }


    /**
     * When the object for some file type issue returns url null
     * this method is called to delete the object and upload the array
     * @return new or the same media array.
     * @author David
     */
    public static MediaObj[] deleteObj(MediaObj[] arr, MediaObj objToDelete) {
        int indexToRemove = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(objToDelete)) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove != -1) {
            MediaObj[] nuevoArr = new MediaObj[arr.length - 1];
            for (int i = 0, j = 0; i < arr.length; i++) {
                if (i != indexToRemove) {
                    nuevoArr[j++] = arr[i];
                }
            }
            return nuevoArr;
        } else {
            return arr;
        }
    }
}
