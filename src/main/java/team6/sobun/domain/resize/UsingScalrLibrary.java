package team6.sobun.domain.resize;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class UsingScalrLibrary {
    public static BufferedImage resizeImageByScalr(MultipartFile image, int maxWidth, int maxHeight) throws IOException {
        BufferedImage orgImg = ImageIO.read(image.getInputStream());
        return Scalr.resize(orgImg, Scalr.Method.AUTOMATIC, Mode.AUTOMATIC, maxWidth, maxHeight);
    }
}