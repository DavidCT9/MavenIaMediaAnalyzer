package AiMediaAnalyzer.externalTools.mediaHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;

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

    public static void framesCreation(MediaObj[] mediaObjs) {
        for (MediaObj mediaObj : mediaObjs) {

            try {
                Path file = Paths.get(mediaObj.getAbsolutePath());
                String fileType = Files.probeContentType(file);
                if (fileType != null) {
                    if (fileType.startsWith("image/")) {
                        imgToVideo(mediaObj);
                    } else if (fileType.startsWith("video/")) {
                        System.out.println("Video detected");
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
        String outputVideo = mediaObj.getDateOriginal() + ".mp4";

        try {
            ProcessBuilder ffprobeBuilder = new ProcessBuilder("ffprobe", "-i", audioInputPath, "-show_entries", "format=duration", "-v", "quiet", "-of", "csv=p=0");
            Process ffprobeProcess = ffprobeBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeProcess.getInputStream()))) {
                String durationStr = reader.readLine();
                double duration = Double.parseDouble(durationStr);

                String[] command = new String[]{"ffmpeg", "-loop", "1", "-i", imageInputPath, "-i", audioInputPath,
                        "-c:v", "libx264", "-c:a", "aac",
                        "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2",
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

            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void videoToVideo(MediaObj mediaObj) {
        String imageInputPath = mediaObj.getAbsolutePath();
        String audioInputPath = mediaObj.getAudioPath();
        String outputVideo = mediaObj.getDateOriginal() + ".mp4";

        try {
            ProcessBuilder ffprobeBuilder = new ProcessBuilder("ffprobe", "-i", audioInputPath, "-show_entries", "format=duration", "-v", "quiet", "-of", "csv=p=0");
            Process ffprobeProcess = ffprobeBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeProcess.getInputStream()))) {
                String durationStr = reader.readLine();
                double duration = Double.parseDouble(durationStr);

                String[] command = new String[]{"ffmpeg", "-i", imageInputPath, "-i", audioInputPath,
                        "-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map",
                        "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2",
                        "1:a:0", outputVideo};
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

    public static void finalAssembly(MediaObj[] mediaObjs, VideoObj video) {

        List<String> videoPaths = new ArrayList<String>();
        String mapVideoPath = mapToVideo(video.getMapImagePath(), video.getCaptions());
        String iaVideoPath = iaImgToVideo(video.getIaImagePath());

        videoPaths.add(mapVideoPath);
        for (MediaObj mediaObj : mediaObjs) {
            videoPaths.add(mediaObj.getGeneratedVideoPath());
        }
        videoPaths.add(iaVideoPath);

        String txtPath = createInputTxtFile(videoPaths);
        if (txtPath != null) {
            String[] command = {
                    "ffmpeg",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", txtPath,
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-vf", "format=yuv420p",
                    "output.mp4"
            };

            processCommand(command);
        }else{
            inputOutput.showInfo("Input text cant be created");
        }

    }


    public static String createInputTxtFile(List<String> videoPaths) {
        String filePath = "finalAssembly.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String videoPath : videoPaths) {
                writer.write("file '" + videoPath + "'\n");
            }
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String mapToVideo(String mapPath, String captions) {
        String assSubtitleFilePath = "temp_subtitle.srt";
        writeCaptionsToFile(captions, assSubtitleFilePath);
        String outputFile = "mapVideo.mp4";

        String[] command = {
                "ffmpeg",
                "-loop", "1",
                "-i", mapPath,
                "-f", "lavfi",
                "-i", "anullsrc",
                "-c:v", "libx264",
                "-t", "3",
                "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2,subtitles=" + assSubtitleFilePath,
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-shortest",
                outputFile
        };
        processCommand(command);

        new File(assSubtitleFilePath).delete();
        return outputFile;
    }

    private static void writeCaptionsToFile(String captions, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8), 8192)) {
            writer.write("1\n00:00:00,000 --> 00:00:03,000\n" + captions + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2",
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-shortest",
                outputFile
        };
        processCommand(command);
        return outputFile;
    }


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
                System.out.println(line);
            }
//            System.out.println(process.getOutputStream());

            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
            process.destroy();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
