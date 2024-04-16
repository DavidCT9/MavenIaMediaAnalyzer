package AiMediaAnalyzer.externalTools.mediaHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;

import javax.print.attribute.standard.Media;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VideoGenerator {
    static IOHandler inputOutput = new IOConsole();

    public static void videoAssembly(VideoObj videoObj, MediaObj[] mediaObjs){
        String txtPath = txtCreator(mediaObjs, videoObj.getIaImagePath(), videoObj.getMapImagePath());
        try{
            Process videoCommand = Runtime.getRuntime().exec("ffmpeg -f concat -safe 0 -i "+txtPath+" -c copy video.mp4");

            BufferedReader reader = new BufferedReader(new InputStreamReader(videoCommand.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(videoCommand.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println("Error: " + errorLine);
            }

            int exitCode = videoCommand.waitFor();
            if (exitCode == 0) {
                inputOutput.showInfo("Video assembly successful.");
            } else {
                inputOutput.showInfo("Video assembly failed. Exit code: " + exitCode);
            }

        } catch (Exception e) {
            inputOutput.showInfo("Video Assembly Error");
            throw new RuntimeException(e);
        }
    }

    public static String txtCreator(MediaObj[] mediaObjs, String iaImgPath, String mapImgPath) {
        String fileName = "mediaPaths.txt";
        StringBuilder pathContent = new StringBuilder();

        pathContent.append("file '"+iaImgPath+"'\n ");
        for (MediaObj mediaFile : mediaObjs) {
            pathContent.append("file '"+mediaFile.getAbsolutePath()+"'\n ");
        }
        pathContent.append("file '"+mapImgPath+"'");

        String txtPath;
        try {
            FileWriter txtWriter = new FileWriter(fileName);
            txtWriter.write(String.valueOf(pathContent));
            txtWriter.close();
            txtPath = String.valueOf(Paths.get(fileName));
            System.out.println("Text file created successfully.");
        } catch (Exception e) {
            inputOutput.showInfo("An error occurred while creating the text file.");
            throw new RuntimeException(e);
        }

        return txtPath;
    }
}
