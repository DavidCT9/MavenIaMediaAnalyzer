package AiMediaAnalyzer.pipelineProcess;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import AiMediaAnalyzer.externalTools.aiHandler.ImageGenerator;
import AiMediaAnalyzer.externalTools.aiHandler.MediaAnalyzer;
import AiMediaAnalyzer.externalTools.aiHandler.TextGenerator;
import AiMediaAnalyzer.externalTools.aiHandler.VoiceGenerator;
import AiMediaAnalyzer.externalTools.mediaHandler.*;

public class PipelineManager {
    static IOHandler inputOutput = new IOConsole();

    public static void MainPipeline() {
        inputOutput.showInfo("Extracting metadata...");
        MetadataExtractor obtainMeta = new MetadataExtractor();
        MediaObj[] mediaArray = obtainMeta.ObtainMetadata("src/main/resources/Media");
//        inputOutput.showInfo("Uploading files...");
//        MediaUpload uploadFiles = new MediaUpload();
//        uploadFiles.cloudinaryHandler(mediaArray);
//        inputOutput.showInfo("Analyzing media...");
//        MediaAnalyzer.openAiVision(mediaArray);
//        inputOutput.showInfo("Generating IA image...");
//        String iaImgPath = ImageGenerator.generateImage(mediaArray);
//        inputOutput.showInfo("Creating inspirational phrase...");
//          String captions = TextGenerator.inspirtionalPhrase(mediaArray);
        inputOutput.showInfo("Geolocation of your media and creating the map...");
        String mapImgPath = MapGenerator.mapCreator(mediaArray);
//        inputOutput.showInfo("Generating voices...");
//        VoiceGenerator.generateAudios(mediaArray);
//        inputOutput.showInfo("Creating frames...");
//        VideoGenerator.framesCreation(mediaArray);

//        inputOutput.showInfo("Assembling final video!");
//        VideoObj finalVideo = new VideoObj(iaImgPath, mapImgPath, captions);




    }

}
