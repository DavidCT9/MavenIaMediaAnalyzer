package AiMediaAnalyzer.externalTools.mediaHandler;

import AiMediaAnalyzer.IOtools.IOConsole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class MapGenerator {
    static IOConsole inputOutput = new IOConsole();

    /**
     * Cals all of its "assistant methods" and according to the array of locations
     * executes the GOOGLE STATIC MAP API call to request an image with a pin in the
     * first and last location of the array.
     * @return The path of the generated map.
     * @author David
     */
    public static String mapCreator(MediaObj[] mediaObjs) {
        String[] location = mediaSort(mediaObjs);

        String googleApiKey = System.getenv("GOOGLE_API");
        String[] commandPrompt;

        commandPrompt = new String[]{"curl", "-X", "POST", "-H", "Content-Length: 0", "https://maps.googleapis.com/maps/api/staticmap?center=" + location[2] + "&format=png&zoom=6&size=400x400&markers=" + location[0] + "&markers=" + location[1] + "&key=" + googleApiKey};

        final ProcessBuilder builder = new ProcessBuilder();
        Path path = null;
        try {
            final Process process = builder.command(commandPrompt).start();
            byte[] imageBytes = process.getInputStream().readAllBytes();

            try {
                String mapFilePath = "map.png";
                path = Paths.get(mapFilePath);
                Files.write(path, imageBytes);
                System.out.println("PNG image saved to: " + mapFilePath);
            } catch (IOException e) {
                e.printStackTrace();
                inputOutput.showInfo("Not possible to get the map image, try again");
            }

            System.out.println("Waiting " + process.waitFor());
            process.destroy();
        } catch (IOException | InterruptedException e) {
            inputOutput.showInfo("It was not possible to create the map, check that your media has the location enabled and try again.");
            throw new RuntimeException(e);
        }

        return path.toString();
    }


    /**
     * Sort the media array according to its date created, so we can get
     * the oldest and newest file, also is obtained the middle point between
     * those locations.
     * @return A string array that contains the newest file, oldest and the midpoint location.
     * @author David
     */
    public static String[] mediaSort(MediaObj[] mediaObjs) {
        Arrays.sort(mediaObjs, Comparator.comparingLong(obj -> Long.parseLong(obj.getDateOriginal())));


        String[] location = new String[3];
        location[0] = formatLocation(mediaObjs[0].getLatitude(), mediaObjs[0].getLongitude());
        location[1] = formatLocation(mediaObjs[mediaObjs.length-1].getLatitude(), mediaObjs[mediaObjs.length-1].getLongitude());
        location[2] = calculateMidpoint(location[0] , location[1]);

        return location;
    }

    /**
     * Remove extra spaces of the original latitude and longitude values
     * and format its so can be used as doubles
     * @return A separated comma string containing the latitude and longitude formated.
     * @author David
     */
    public static String formatLocation(String latitude, String longitude) {
        String[] coordinatesLat = latitude.split(" ");
        String[] coordinatesLon = longitude.split(" ");

        latitude = coordinatesLat[0] + " " + coordinatesLat[2] + " " + coordinatesLat[3] + " " + coordinatesLat[4];
        longitude = coordinatesLon[0] + " " + coordinatesLon[2] + " " + coordinatesLon[3] + " " + coordinatesLon[4];

        double doubleLatitude = parseCoordinate(latitude);
        double doubleLongitude = parseCoordinate(longitude);

        return String.format("%.6f,%.6f", doubleLatitude, doubleLongitude);
    }

    /**
     * This method does the "dirty job" replacing ' and "" of minutes and seconds
     * values of the latitude and longitude; also calculates the location as a double.
     * Important to mention that also detects if the double needs to be negative
     * or positive, according to its polar values ("W" or "S").
     * @return The public url of the media object.
     * @author David
     */
    private static double parseCoordinate(String coordinateString) {
        String[] parts = coordinateString.split(" ");
        double degrees = Double.parseDouble(parts[0]);
        String minutesString = parts[1].replaceAll("'", "");
        String secondString = parts[2].replaceAll("\"", "");

        double minutes = Double.parseDouble(minutesString);
        double seconds = Double.parseDouble(secondString);

        double decimalDegrees = degrees + minutes / 60 + seconds / 3600;

        int directionMultiplier = (parts[3].equals("W") || parts[3].equals("S")) ? -1 : 1;
        return decimalDegrees * directionMultiplier;
    }

    /**
     * Makes the basic math to calculate the mid-point between two points
     * and parse them as doubles with precision of six decimals.
     * Finally, returned as a string
     * @return The public url of the media object.
     * @author David
     */
    public static String calculateMidpoint(String location1, String location2) {
        String[] parts1 = location1.split(",");
        String[] parts2 = location2.split(",");

        double lat1 = Double.parseDouble(parts1[0]);
        double lon1 = Double.parseDouble(parts1[1]);

        double lat2 = Double.parseDouble(parts2[0]);
        double lon2 = Double.parseDouble(parts2[1]);

        // Calculating the average latitude and longitude
        double avgLat = (lat1 + lat2) / 2;
        double avgLon = (lon1 + lon2) / 2;

        return String.format("%.6f,%.6f", avgLat, avgLon);
    }



}
