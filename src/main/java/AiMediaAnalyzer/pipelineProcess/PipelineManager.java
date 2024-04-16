package AiMediaAnalyzer.pipelineProcess;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import AiMediaAnalyzer.externalTools.aiHandler.ImageGenerator;
import AiMediaAnalyzer.externalTools.aiHandler.MediaAnalyzer;
import AiMediaAnalyzer.externalTools.aiHandler.TextGenerator;
import AiMediaAnalyzer.externalTools.mediaHandler.*;

public class PipelineManager {
    static IOHandler inputOutput = new IOConsole();

    public static void MainPipeline() {
        inputOutput.showInfo("Extracting metadata...");
        MetadataExtractor obtainMeta = new MetadataExtractor();
        MediaObj[] mediaArray = obtainMeta.ObtainMetadata("C:\\Users\\david\\Documents\\DAVID\\UP\\4_Semestre\\ComputerGraphics\\ProjectP2\\AiMediaAnalyze\\Media\\");
        inputOutput.showInfo("Uploading files...");
        MediaUpload uploadFiles = new MediaUpload();
        uploadFiles.cloudinaryHandler(mediaArray);
        inputOutput.showInfo("Analyzing media...");
        MediaAnalyzer.openAiVision(mediaArray);
        inputOutput.showInfo("Generating IA image...");
        String iaImgPath = ImageGenerator.generateImage(mediaArray);
        inputOutput.showInfo("Creating inspirational phrase...");
        TextGenerator.inspirtionalPhrase(mediaArray);
        inputOutput.showInfo("Geolocation of your media and creating the map...");
        String mapImgPath = MapGenerator.mapCreator(mediaArray);
        inputOutput.showInfo("Building the video...");
//        String txtPath = VideoGenerator.txtCreator(mediaArray,iaImgPath,mapImgPath);
//        inputOutput.showInfo(txtPath);

        VideoObj video = new VideoObj(iaImgPath, mapImgPath);
        VideoGenerator.videoAssembly(video, mediaArray);

        
    }

}
