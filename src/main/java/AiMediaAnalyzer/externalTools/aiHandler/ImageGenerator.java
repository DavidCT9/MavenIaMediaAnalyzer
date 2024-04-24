package AiMediaAnalyzer.externalTools.aiHandler;

import AiMediaAnalyzer.IOtools.IOConsole;
import AiMediaAnalyzer.IOtools.IOHandler;
import AiMediaAnalyzer.externalTools.mediaHandler.MediaObj;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageGenerator {
    static IOHandler inputOutput = new IOConsole();

    public static String generateImage(MediaObj[] mediaArray) {
        String jsonString = jsonModifier(mediaArray);

        String[] curlIaPrompt;
        String openAiKey = System.getenv("OPENAI_API_KEY");

        curlIaPrompt = new String[]{"curl", "-X", "POST", "https://api.openai.com/v1/images/generations",
                "-H", "Content-Type: application/json",
                "-H", "Authorization: Bearer " + openAiKey,
                "-d", "@" + jsonString};

        final ProcessBuilder builder = new ProcessBuilder();
        String IaImgURL;
        try {
            final Process process = builder.command(curlIaPrompt).start();

            ObjectMapper mapper = new ObjectMapper();
            try (InputStream is = process.getInputStream()) {
                JsonNode jsonResponse = mapper.readTree(is);

                IaImgURL = jsonResponse.get("data").get(0).get("url").asText();
            }

            System.out.println("Waiting " + process.waitFor());
            process.destroy();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        String IaImgAbsolutePath = iaImgDownloader(IaImgURL);
        return IaImgAbsolutePath;
    }

    public static String jsonModifier(MediaObj[] mediaArray) {
        ObjectMapper mapper = new ObjectMapper();
        String absolutePath = null;
        StringBuilder prompt = new StringBuilder("Based on the next image descriptions, i NEED you to create a image that summaries and express the essence of the descriptions (as output just show me the string containing the phrase): ");

        for (MediaObj mediaObj: mediaArray){
            prompt.append(mediaObj.getIaDescription());
        }

        try {
            JsonNode jsonNode = mapper.readTree(new File("src/main/resources/jsonRequests/imageGenerator.json"));

            ((ObjectNode) jsonNode).put("prompt", String.valueOf(prompt));


            String modifiedJsonString = mapper.writeValueAsString(jsonNode);

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(now);

            File outputFile = new File("IAjson"+ timestamp + ".json");
            outputFile.createNewFile();
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write(modifiedJsonString);
            fileWriter.flush();
            fileWriter.close();

            absolutePath = outputFile.getAbsolutePath();
        } catch (Exception e) {
            System.out.println("Error modifying json: " + e.getMessage());
        }
        return absolutePath;
    }

    public static String iaImgDownloader(String imageUrl) {
        String destinationFile = "IaGeneratedImage.png";

        File file = null;
        try {
            URL ImgUrl = new URL(imageUrl);
            InputStream inputStream = ImgUrl.openStream();
            OutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[2048];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            System.out.println("IA image downloaded successfully!");

            file = new File(destinationFile);

        } catch (IOException e) {
            e.printStackTrace();
            inputOutput.showInfo("And error happened while downloading the image, check your url and media");
        }

        return file.getAbsolutePath();
    }

}
