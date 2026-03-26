package org.braun.cookbook.backend.process;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class DirectoryComparerTester {
   
   public static class XmlFilter implements FileFilter {

      @Override
      public boolean accept(File pathname) {
         return pathname.isDirectory() || pathname.getName().endsWith(".xml");
      }

   }
   
   @Test
   public void getParentPath() {
       String path = "ARD/ndr/2026/12345.xml";
       int l = path.lastIndexOf('/');
       System.out.println(path.substring(0, l));
   }
   public void test() {
      
      try (DirectoryComparer dc = new DirectoryComparer("/opt/solr/data/cookbook/content", new XmlFilter());) {
         int i= 0;
         while (dc.hasNext()) {
            i++;
            DirectoryComparer.Entry entry = dc.next();
            System.out.println(entry);
         }
         System.out.println(i);
      } catch (IOException ex) {
         Logger.getLogger(DirectoryComparerTester.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
}
