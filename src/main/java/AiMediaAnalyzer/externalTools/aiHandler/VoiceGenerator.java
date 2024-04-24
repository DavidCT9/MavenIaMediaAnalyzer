package AiMediaAnalyzer.externalTools.aiHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import AiMediaAnalyzer.externalTools.mediaHandler.MediaObj;
import org.cloudinary.json.JSONObject;

import java.io.*;

public class VoiceGenerator {
    static IOHandler inputOutput = new IOConsole();

    public static void generateAudios(MediaObj[] mediaObjs) {
        String openAiKey = System.getenv("OPENAI_API_KEY");
        inputOutput.showInfo("Media length: "+mediaObjs.length);

        for (MediaObj mediaObj : mediaObjs) {

            String audioName = jsonModifier(mediaObj);

            String[] command;
            command = new String[]{"curl", "-X", "POST", "https://api.openai.com/v1/audio/speech",
                    "-H", "Authorization: Bearer " + openAiKey,
                    "-H", "Content-Type: application/json",
                    "-d", "@" + "src/main/resources/jsonRequests/tts.json", "--output", audioName+".mp3"};

            final ProcessBuilder builder = new ProcessBuilder();
            try {
                final Process process = builder.command(command).start();
                byte[] audioBytes = process.getInputStream().readAllBytes();

                try (OutputStream outputStream = new FileOutputStream("output.mp3")) {
                    outputStream.write(audioBytes);
                } catch (IOException e) {
                    inputOutput.showInfo("Could not generate audio for: "+audioName);
                    e.printStackTrace();
                }

                System.out.println("Waiting " + process.waitFor());
                process.destroy();
                mediaObj.setAudioPath(audioName+".mp3");
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static String jsonModifier(MediaObj mediaObj) {
        String jsonFilePath = "src/main/resources/jsonRequests/tts.json";
        try {
            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line).append("\n");
                }
            }

            JSONObject json = new JSONObject(jsonContent.toString());

            System.out.println("TEXT: "+mediaObj.getIaDescription());
            json.put("input", mediaObj.getIaDescription());

            try (FileWriter writer = new FileWriter(jsonFilePath)) {
                writer.write(json.toString(4));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing audio for input: " + mediaObj.getAbsolutePath(), e);
        }

        return mediaObj.getDateOriginal();
    }

}
