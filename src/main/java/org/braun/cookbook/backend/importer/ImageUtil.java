package org.braun.cookbook.backend.importer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

/**
 *
 * @author mbraun
 */
public class ImageUtil {

   /**
    * 
    * @param inputStream
    * @return width and height of an Image
    * @throws IOException 
    */
   public static int[] getDimension(InputStream inputStream) throws IOException {
      BufferedImage image = ImageIO.read(inputStream);

      int width = image.getWidth();
      int height = image.getHeight();
      return new int[] {width, height};
      
   }

   public static int[] resizeToWidth(InputStream inputStream, OutputStream outputStream, double widthMax) throws IOException {
      return resizeToWidth(inputStream, outputStream, widthMax, "image/jpeg");
   }   
   /**
    * scale an image to
    * <code>widthMax</code> width if the given width is greater than 400px and
    * convert it to format "jpg"
    *
    * @param inputStream
    * @param outputStream
    * @param widthMax
     * @param contentType
    * @throws IOException
    * @return width and height of the scaled image as int[2]
    */
   public static int[] resizeToWidth(InputStream inputStream, OutputStream outputStream, double widthMax, String contentType) throws IOException {
      BufferedImage image = ImageIO.read(inputStream);
      if (image == null) return null;

      if (image.getType() == BufferedImage.TYPE_CUSTOM) {
         image = convert(image, BufferedImage.TYPE_INT_RGB);
      }
      
      int width = image.getWidth();
      int height = image.getHeight();
      Iterator<ImageWriter> iter = ImageIO.getImageWritersByMIMEType(contentType);
      if (!iter.hasNext()) {
         return new int[]{width, height};
      }

      
      BufferedImage tmpImage = null;
      if (width > widthMax) {
         if ((width / (float) height) > 2.5f) {
            int x = (width - (int) widthMax) / 2;
            width = (int) widthMax;
            tmpImage = new BufferedImage(width, height, image.getType());
            Graphics2D graphics2D = tmpImage.createGraphics();

            BufferedImage croppedImage = image.getSubimage(x, 0, width, height);
            graphics2D.drawImage(croppedImage, null, 0, 0);
         } else {
            height = (int) (height * widthMax / width);
            width = (int) widthMax;
         }
      }

      
      if (tmpImage == null) {
         tmpImage = new BufferedImage(width, height, image.getType());
         Graphics2D graphics2D = tmpImage.createGraphics();
         graphics2D.drawImage(image, 0, 0, width, height, null);
      }

      ImageWriter writer = iter.next();
      try {
         ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();
         if ("image/jpeg".equals(contentType)) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(0.70f);
         }

         writer.setOutput(ImageIO.createImageOutputStream(outputStream));
         writer.write(null, new IIOImage(tmpImage, null, null), imageWriteParam);
         return new int[]{width, height};
      } finally {
         if (writer != null) {
            writer.dispose();
         }
      }
   }
   public static BufferedImage convert(BufferedImage src, int bufImgType) {
    BufferedImage img= new BufferedImage(src.getWidth(), src.getHeight(), bufImgType);
    Graphics2D g2d= img.createGraphics();
    g2d.drawImage(src, 0, 0, null);
    g2d.dispose();
    return img;
}
}
