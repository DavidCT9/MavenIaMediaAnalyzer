package AiMediaAnalyzer;


import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import AiMediaAnalyzer.externalTools.aiHandler.ImageGenerator;
import AiMediaAnalyzer.externalTools.aiHandler.MediaAnalyzer;
import AiMediaAnalyzer.externalTools.aiHandler.TextGenerator;
import AiMediaAnalyzer.externalTools.aiHandler.VoiceGenerator;
import AiMediaAnalyzer.externalTools.mediaHandler.MediaObj;
import AiMediaAnalyzer.externalTools.mediaHandler.MediaUpload;
import AiMediaAnalyzer.externalTools.mediaHandler.VideoGenerator;
import AiMediaAnalyzer.pipelineProcess.PipelineManager;

import javax.print.attribute.standard.Media;
import java.awt.*;

public class Main {
    static IOHandler inputOutput = new IOConsole();

    public static void main(String[] args) {
        String folderPath = inputOutput.getString("Please provide the path of the folder with the desired media to analyze", "Not valid Path");
        PipelineManager.MainPipeline(folderPath);
    }

}
