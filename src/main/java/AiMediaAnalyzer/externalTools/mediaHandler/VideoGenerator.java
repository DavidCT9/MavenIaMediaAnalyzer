package AiMediaAnalyzer.externalTools.mediaHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;

import javax.print.attribute.standard.Media;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VideoGenerator {
    static IOHandler inputOutput = new IOConsole();

    public static void framesCreation(MediaObj[] mediaObjs) {
        for (MediaObj mediaObj : mediaObjs) {

            try {
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
            } catch (Exception e) {
                System.err.println("Error determining file type: " + e.getMessage());

            }

        }
    }


    private static void imgToVideo(MediaObj mediaObj) {
        String imageInputPath = mediaObj.getAbsolutePath();
        String audioInputPath = mediaObj.getAudioPath();
        String outputVideo = mediaObj.getDateOriginal()+".mp4";

        try {
            ProcessBuilder ffprobeBuilder = new ProcessBuilder("ffprobe", "-i", audioInputPath, "-show_entries", "format=duration", "-v", "quiet", "-of", "csv=p=0");
            Process ffprobeProcess = ffprobeBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeProcess.getInputStream()))) {
                String durationStr = reader.readLine();
                double duration = Double.parseDouble(durationStr);

                // Run ffmpeg to create video
                String[] command = new String[]{"ffmpeg", "-loop", "1", "-i", imageInputPath, "-i", audioInputPath, "-c:v", "libx264", "-c:a", "aac", "-shortest", "-t", String.valueOf(duration), outputVideo};
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

            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void videoToVideo(MediaObj mediaObj) {
        String imageInputPath = mediaObj.getAbsolutePath();
        String audioInputPath = mediaObj.getAudioPath();
        String outputVideo = mediaObj.getDateOriginal()+".mp4";

        try {
            ProcessBuilder ffprobeBuilder = new ProcessBuilder("ffprobe", "-i", audioInputPath, "-show_entries", "format=duration", "-v", "quiet", "-of", "csv=p=0");
            Process ffprobeProcess = ffprobeBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeProcess.getInputStream()))) {
                String durationStr = reader.readLine();
                double duration = Double.parseDouble(durationStr);

                String[] command = new String[]{"ffmpeg", "-i", imageInputPath, "-i", audioInputPath, "-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0", outputVideo};
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
                System.out.println("Process exited with code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void finalAssembly(MediaObj[] mediaObjs, VideoObj video){

    }


}
