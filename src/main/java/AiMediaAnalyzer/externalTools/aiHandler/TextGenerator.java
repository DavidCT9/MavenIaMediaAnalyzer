package AiMediaAnalyzer.externalTools.aiHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import AiMediaAnalyzer.externalTools.mediaHandler.MediaObj;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextGenerator {
    static IOHandler inputOutput = new IOConsole();

    /**
     * Executes the API call to create just one description or phrase that
     * summarizes all the descriptions of all the media.
     * @return A string that contains the phrase.
     * @author David
     */
    public static String inspirtionalPhrase(MediaObj[] mediaObjs) {
        String[] commandPrompt;
        String openAiKey = System.getenv("OPENAI_API_KEY");
        String jsonPath = jsonModifier(mediaObjs);

        commandPrompt = new String[]{"curl", "-X", "POST", "https://api.openai.com/v1/chat/completions",
                "-H", "Content-Type: application/json",
                "-H", "Authorization: Bearer " + openAiKey,
                "-d", "@" + jsonPath};

        final ProcessBuilder builder = new ProcessBuilder();
        String inspirationalPhrase;
        try {
            final Process process = builder.command(commandPrompt).start();
            InputStream is = process.getInputStream();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(is);

            inspirationalPhrase = jsonResponse.get("choices").get(0).get("message").get("content").asText();
            System.out.println(inspirationalPhrase);

            System.out.println("Waiting " + process.waitFor());
            process.destroy();
        } catch (IOException | InterruptedException e) {
            inputOutput.showInfo("Something went wrong while trying to create the inspirational phrase, try again.");
            throw new RuntimeException(e);
        }

        return inspirationalPhrase;
    }

    /**
     * Access the json template tree to modify the value, appending all
     * descriptions of the media array and saving it
     * @return The path of the modified json.
     * @author David
     */
    public static String jsonModifier(MediaObj[] mediaObjs) {

        ObjectMapper mapper = new ObjectMapper();

        String absolutePath = null;
        try {
            JsonNode jsonNode = mapper.readTree(new File("src/main/resources/jsonRequests/inspirationalPhrase.json"));
            StringBuilder promptText = new StringBuilder("I will give you a list of image descriptions (in a json), with this ones you will create a inspirational phrase that summarizes and describe the essence of all descriptions just in one phrase MAXIMUM 15 CHARACTERS. The descriptions: ");

            for (MediaObj mediaFile : mediaObjs) {
                promptText.append(mediaFile.getIaDescription());
            }

            JsonNode userMessage = jsonNode.at("/messages/1");
            ((ObjectNode) userMessage).put("content", promptText.toString());

            String modifiedJsonString = mapper.writeValueAsString(jsonNode);

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(now);

            File outputFile = new File("inspirationalPhrase" + timestamp + ".json");
            outputFile.createNewFile();
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write(modifiedJsonString);
            fileWriter.flush();
            fileWriter.close();

            absolutePath = outputFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while creating the phrase");
        }

        return absolutePath;
    }

}
