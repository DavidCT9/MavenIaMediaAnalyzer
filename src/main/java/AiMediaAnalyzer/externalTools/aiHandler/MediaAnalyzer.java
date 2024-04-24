package AiMediaAnalyzer.externalTools.aiHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import AiMediaAnalyzer.externalTools.mediaHandler.MediaObj;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.io.*;

public class MediaAnalyzer {
    IOHandler inputOutput = new IOConsole();

    /**
     * Make the Open Ai api call using a json to receive the description of
     * every media object in the array and save the description
     * @author David
     */
    public static void openAiVision(MediaObj[] mediaArray){
        for (MediaObj mediaFile: mediaArray) {
            String jsonPath = modifyImageUrl(mediaFile.getUrl(),mediaFile.getDateOriginal());

            String[] curlCommand;
            String openAiKey = System.getenv("OPENAI_API_KEY");

            curlCommand = new String[]{"curl", "-X", "POST", "https://api.openai.com/v1/chat/completions",
                    "-H", "Content-Type: application/json",
                    "-H", "Authorization: Bearer " + openAiKey,
                    "-d", "@"+jsonPath};

            final ProcessBuilder builder = new ProcessBuilder();
            try {
                final Process process = builder.command(curlCommand).start();

                ObjectMapper mapper = new ObjectMapper();

                try (InputStream is = process.getInputStream()) {
                    JsonNode jsonResponse = mapper.readTree(is);

                    JsonNode contentNode = jsonResponse.at("/choices/0/message/content");
                    String jsonContent = contentNode.asText();

                    mediaFile.setIaDescription(jsonContent);
                }

                System.out.println("Waiting " + process.waitFor());
                process.destroy();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates a copy of the sample "analyzer.json" and modofifies the url
     * value to change it to the media object url
     * @return The path of the modified json.
     * @author David
     */
    public static String modifyImageUrl(String newImageUrl, String outputFilePath) {
        ObjectMapper mapper = new ObjectMapper();

        String absolutePath = null;
        try {
            JsonNode jsonNode = mapper.readTree(new File("src/main/resources/jsonRequests/analyzer.json"));

            ((ObjectNode) jsonNode.get("messages").get(0).get("content").get(1).get("image_url"))
                    .put("url", newImageUrl);

            String modifiedJsonString = mapper.writeValueAsString(jsonNode);

            File outputFile = new File(outputFilePath + ".json");
            outputFile.createNewFile();
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write(modifiedJsonString);
            fileWriter.flush();
            fileWriter.close();

            absolutePath = outputFile.getAbsolutePath();
        } catch (Exception e) {
            System.out.println("Error");
        }
        return absolutePath;
    }




}
