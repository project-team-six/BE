<<<<<<< HEAD:src/main/java/team6/sobun/domain/resize/ImageUtils.java
//package team6.sobun.domain.resize;
=======
package team6.sobun.global.utils.resize;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;

//File file = new File(_파일경로_, _파일명_);
>>>>>>> 88ac2abf872bb5faf8bb740033d53672ae29bf78:src/main/java/team6/sobun/global/utils/resize/ImageUtils.java
//
//import org.apache.commons.io.FilenameUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.awt.image.PixelGrabber;
//import java.io.File;
//import java.io.IOException;
//
////File file = new File(_파일경로_, _파일명_);
////
////        File flTgt = new File(_생성경로_, _생성파일명_+".jpg");
////
////        boolean isConvert = ImageUtils.convertFmt(file, flTgt);
////        if(isConvert) {
////        ImageUtils.resizeImage(flTgt.getPath(), 1920, 1080);
////        }
//
//// 이미지 처리 관련 유틸리티 메서드들을 정의하는 코드
//public class ImageUtils {
//    public static File multipartFileToFile(MultipartFile multipartFile) throws IOException, IOException {
//        File file = new File(multipartFile.getOriginalFilename());
//        multipartFile.transferTo(file);
//        return file;
//    }
//
//    public static BufferedImage resizeImage(MultipartFile image, int maxWidth, int maxHeight) throws Exception{
//
//        int convertedWidth = 0;
//        int convertedHeight = 0;
//        float maxRatio = maxHeight / (float)maxWidth;
//        String ext = FilenameUtils.getExtension(image.getOriginalFilename());
//
//        // MultipartFile to BufferedImage
//        File inputFile = ImageUtils.multipartFileToFile(image);
//        BufferedImage inputImage = ImageIO.read(inputFile);
//
//        // current width & height
//        int width = inputImage.getWidth();
//        int height = inputImage.getHeight();
//        float ratio = height / (float)width;
//
//        // decide to convert or not
//        if(width > maxWidth || height > maxHeight) {
//
//            if(ratio < maxRatio) {
//                convertedWidth = (int)(width * (maxWidth / (float)width));
//                convertedHeight = (int)(height * (maxWidth / (float)width));
//            }else{
//                convertedWidth = (int)(width * (maxHeight / (float)height));
//                convertedHeight = (int)(height * (maxHeight / (float)height));
//            }
//        } else {
//            return inputImage;
//        }
//
//        Image srcImg;
//
//        if(ext.equals("bmp") || ext.equals("png") || ext.equals("gif")){
//            srcImg = ImageIO.read(inputFile);
//        } else {
//            srcImg = new ImageIcon(inputFile.toURL()).getImage();
//        }
//
//        Image imgTarget = srcImg.getScaledInstance(convertedWidth, convertedHeight, Image.SCALE_SMOOTH);
//        int pixels[] = new int[convertedWidth * convertedHeight];
//        PixelGrabber pg = new PixelGrabber(imgTarget, 0, 0, convertedWidth, convertedHeight, pixels, 0, convertedWidth);
//        pg.grabPixels();
//
//        BufferedImage outputImage = new BufferedImage(convertedWidth, convertedHeight, inputImage.getType());
//        outputImage.setRGB(0, 0,  convertedWidth, convertedHeight, pixels, 0, convertedWidth);
//
//        return outputImage;
//    }
//}
