package AiMediaAnalyzer.externalTools.mediaHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoGenerator {
    static IOHandler inputOutput = new IOConsole();

    /**
     * Iterates along the media array to classify each media file and call
     * the appropriate method so the video can be created, also intercepts possible
     * errors with corrupted or not accepted files
     * @author David
     */
    public static void framesCreation(MediaObj[] mediaObjs) {
        for (MediaObj mediaObj : mediaObjs) {
            try {
                if (mediaObj != null && mediaObj.getAbsolutePath() != null) {
                    Path file = Paths.get(mediaObj.getAbsolutePath());
                    String fileType = Files.probeContentType(file);
                    if (fileType != null) {
                        if (fileType.startsWith("image/")) {
                            imgToVideo(mediaObj);
                        } else if (fileType.startsWith("video/")) {
                            videoToVideo(mediaObj);
                        } else {
                            System.err.println("Unsupported file type: " + fileType);
                        }
                    } else {
                        System.err.println("Failed to determine file type for: " + mediaObj.getAbsolutePath());
                    }
                } else {
                    System.err.println("Media object or its absolute path is null");
                }
            } catch (IOException e) {
                System.err.println("Error determining file type: " + e.getMessage());
            }
        }
    }


    /**
     * This method is in charge of building the necessary ffmpeg command
     * to apply the filter aspect ratio, add the mp3 file and set the duration
     * of the video created from the input image.
     * @author David
     */
    private static void imgToVideo(MediaObj mediaObj) {
        String imageInputPath = mediaObj.getAbsolutePath();
        String audioInputPath = mediaObj.getAudioPath();
        String outputVideo = mediaObj.getDateOriginal() + ".mp4";

        try {
            ProcessBuilder ffprobeBuilder = new ProcessBuilder("ffprobe", "-i", audioInputPath, "-show_entries", "format=duration", "-v", "quiet", "-of", "csv=p=0");
            Process ffprobeProcess = ffprobeBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeProcess.getInputStream()))) {
                String durationStr = reader.readLine();
                if (durationStr != null) {
                    double duration = Double.parseDouble(durationStr.trim());

                    String[] command = new String[]{"ffmpeg", "-loop", "1", "-i", imageInputPath, "-i", audioInputPath,
                            "-c:v", "libx264", "-c:a", "aac",
                            "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2,setsar=1",
                            "-pix_fmt", "yuv420p",
                            "-shortest", "-t", String.valueOf(duration), outputVideo};
                    ProcessBuilder builder = new ProcessBuilder(command);
                    builder.redirectErrorStream(true);
                    Process process = builder.start();
                    try (InputStream is = process.getInputStream();
                         InputStreamReader isr = new InputStreamReader(is);
                         BufferedReader br = new BufferedReader(isr)) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            //System.out.println(line);
                        }
                    }
                    int exitCode = process.waitFor();
                    System.out.println("Process exited with code: " + exitCode);

                    mediaObj.setGeneratedVideoPath(outputVideo);
                } else {
                    System.err.println("Duration string is null.");
                }
            }
        } catch (IOException | InterruptedException e) {
            inputOutput.showInfo("image to video failed in: "+mediaObj.getAbsolutePath());
            throw new RuntimeException(e);
        }
    }


    /**
     * This method is in charge of building the necessary ffmpeg command
     * to apply the filter aspect ratio, add the mp3 file and create
     * the new video based on the media object file.
     * @author David
     */
    private static void videoToVideo(MediaObj mediaObj) {
        String imageInputPath = mediaObj.getAbsolutePath();
        String audioInputPath = mediaObj.getAudioPath();
        String outputVideo = mediaObj.getDateOriginal() + ".mp4";

        try {
            ProcessBuilder ffprobeBuilder = new ProcessBuilder("ffprobe", "-i", audioInputPath, "-show_entries", "format=duration", "-v", "quiet", "-of", "csv=p=0");
            Process ffprobeProcess = ffprobeBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeProcess.getInputStream()))) {
                String durationStr = reader.readLine();
                //double duration = Double.parseDouble(durationStr);

                String[] command = new String[]{"ffmpeg", "-i", imageInputPath, "-i", audioInputPath,
                        "-c:v", "libx264", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0",
                        "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2,setsar=1",
                        outputVideo};
                ProcessBuilder builder = new ProcessBuilder(command);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                try (InputStream is = process.getInputStream();
                     InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader br = new BufferedReader(isr)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        //System.out.println(line);
                    }
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            System.err.println("Error: " + errorLine);
                        }
                    }
                }

                mediaObj.setGeneratedVideoPath(outputVideo);
                System.out.println("Process exited with code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * This is the "golden broach" of the program, it gets all the processed videos
     * created earlier and concatenate all of them in a ffmpeg command
     * to finally get the output video of the program.
     * @author David
     */
    public static void finalAssembly(MediaObj[] mediaObjs, VideoObj video) {
        String mapVideoPath = mapToVideo(video.getMapImagePath(), video.getCaptions());
        String iaVideoPath = iaImgToVideo(video.getIaImagePath());

        List<String> inputPaths = new ArrayList<>();

        inputPaths.add("-i");
        inputPaths.add(iaVideoPath);

        for (MediaObj mediaObj : mediaObjs) {
            inputPaths.add("-i");
            inputPaths.add(mediaObj.getGeneratedVideoPath());
        }

        inputPaths.add("-i");
        inputPaths.add(mapVideoPath);

        String[] command = {
                "ffmpeg"
        };

        command = ArrayUtils.addAll(command, inputPaths.toArray(new String[0]));

        StringBuilder filterComplex = new StringBuilder();
        filterComplex.append("[0:v] [0:a] ");
        for (int i = 1; i <= mediaObjs.length; i++) {
            filterComplex.append("[").append(i).append(":v] [").append(i).append(":a] ");
        }
        filterComplex.append("[").append(mediaObjs.length + 1).append(":v] [").append(mediaObjs.length + 1).append(":a] ");
        filterComplex.append("concat=n=").append(mediaObjs.length + 2).append(":v=1:a=1 [vv] [aa]");

        command = ArrayUtils.addAll(command, "-filter_complex", filterComplex.toString());
        command = ArrayUtils.addAll(command, "-map", "[vv]", "-map", "[aa]");
        String[] outputCommand = {
                "-c:v", "libx264",
                "FinalVideo.mp4"
        };
        command = ArrayUtils.addAll(command, outputCommand);

        System.out.println(Arrays.toString(command));
        processCommand(command);
    }


    /**
     * Add the subtitles or captions to the map video because is
     * the last image and build the video like the image to video method.
     * Also format the captions before sending it to the write captions method.
     * @return The path of the map video.
     * @author David
     */
    public static String mapToVideo(String mapPath, String captions) {

        if (captions.startsWith("\n")) {
            captions = captions.substring(1);
        }
        captions = captions.replace("\n","");
        captions = captions.replaceAll("^.*\"[^\"]*\"([^\"]*)\"[^\"\"]*$", "$1");

        String assSubtitleFilePath = "temp_subtitle.srt";
        writeCaptionsToFile(captions, assSubtitleFilePath);
        String outputFile = "mapVideo.mp4";
        System.out.println("CAPTIONS: "+captions);

        String[] command = {
                "ffmpeg",
                "-loop", "1",
                "-i", mapPath,
                "-f", "lavfi",
                "-i", "anullsrc",
                "-c:v", "libx264",
                "-t", "3",
                "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2,subtitles=" + assSubtitleFilePath+",setsar=1",
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-shortest",
                outputFile
        };
        processCommand(command);

        new File(assSubtitleFilePath).delete();
        return outputFile;
    }


    /**
     * Creates the file (that will be a srt type file) that be used in the map video
     * process, writing the captions into a new file.
     * @author David
     */
    private static void writeCaptionsToFile(String captions, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8), 8192)) {
            writer.write("1\n00:00:00,000 --> 00:00:03,000\n" + captions + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * very similar to the img to video method only that this is designed specially for
     * the AI image and create the first video of the final video
     * @return The path of the AI generated image video.
     * @author David
     */
    public static String iaImgToVideo(String iaImgPath) {
        String outputFile = "iaImgToVideo.mp4";

        String[] command = {
                "ffmpeg",
                "-loop", "1",
                "-i", iaImgPath,
                "-f", "lavfi",
                "-i", "anullsrc",
                "-c:v", "libx264",
                "-t", "3",
                "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2,setsar=1",
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-shortest",
                outputFile
        };
        processCommand(command);
        return outputFile;
    }


    /**
     * Receives a string array that will be processed to create a new command
     * and execute it, cathcing possible errors of the execution.
     * @author David
     */
    static void processCommand(String[] command) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
            process.destroy();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
