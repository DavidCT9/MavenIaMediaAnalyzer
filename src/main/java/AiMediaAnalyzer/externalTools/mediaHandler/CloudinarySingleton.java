package AiMediaAnalyzer.externalTools.mediaHandler;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinarySingleton {

    private static Cloudinary cloudinary;

    public static Cloudinary getCloudinary() {
        if (cloudinary == null) {
            String apiSecret = System.getenv("CLOUDINARY_API_SECRET");
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "davidct",
                    "api_key", "772141489122225",
                    "api_secret", apiSecret,
                    "secure", true));
        }
        return cloudinary;
    }
}
