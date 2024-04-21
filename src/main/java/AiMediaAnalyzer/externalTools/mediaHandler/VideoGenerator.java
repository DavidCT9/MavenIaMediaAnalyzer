package AiMediaAnalyzer.externalTools.mediaHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        String outputVideo = mediaObj.getDateOriginal() + ".mp4";

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

    public static void finalAssembly(MediaObj[] mediaObjs, VideoObj video) {

        List<String> videoPaths = new ArrayList<String>();
        for (MediaObj mediaObj : mediaObjs) {
            videoPaths.add(mediaObj.getGeneratedVideoPath() + "\n");
        }

        try {
            Path inputTxtPath = Paths.get("input.txt");
            Files.write(inputTxtPath, videoPaths);

            String[] command = {
                    "ffmpeg",
                    "-loop", "1",
                    "-i", video.getIaImagePath(),
                    "-t", "3",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", inputTxtPath.toString(),
                    "-loop", "1",
                    "-i", video.getMapImagePath(),
                    "-t", "3",
                    "-vf", "drawtext=text='" + video.getCaptions() + "':x=(w-text_w)/2:y=(h-text_h)/2:fontsize=14:fontcolor=white",
                    "-filter_complex", "[0:v][1:v]concat=n=2:v=1:a=0[v1];[v1][2:v]concat=n=2:v=1:a=0[v2]",
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "output.mp4"
            };

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

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error: An error occurred during video generation.");
        }


    }

    public static void mapToVideo(String mapPath, String captions) {
        String assSubtitle = "[Script Info]\n" +
                "Title: Subtitles\n" +
                "[V4+ Styles]\n" +
                "Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n" +
                "Style: Default,Arial,24,&HFFFFFF,&HFFFFFF,&H000000,&H000000,0,0,0,0,100,100,0,0,1,1,0,2,10,10,10,0\n" +
                "[Events]\n" +
                "Format: Layer, Start, End, Style, Actor, MarginL, MarginR, MarginV, Effect, Text\n" +
                "Dialogue: 0,0:00:00.00,0:00:03.00,Default,,0,0,0,,{\\pos(960,540)}" + captions.replace("\n", "\\N");

        String assSubtitleFilePath = "temp_subtitle.ass";
        writeAssSubtitleToFile(assSubtitle, assSubtitleFilePath);


        String[] command = {
                "ffmpeg",
                "-loop", "1",
                "-i", mapPath,
                "-f", "lavfi",
                "-i", "anullsrc",
                "-c:v", "libx264",
                "-t", "3",
                "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2,ass="+assSubtitleFilePath,
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-shortest",
                "mapVideo.mp4"
        };
        processCommand(command);

        new File(assSubtitleFilePath).delete();

    }

    private static void writeAssSubtitleToFile(String captions, String filePath) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(captions);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void iaImgToVideo(String iaImgPath) {

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
                "iaImgVideo.mp4"
        };
        processCommand(command);

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
