package org.braun.cookbook.backend.model.recipe;

import java.util.Comparator;

/**
 *
 * @author mbraun
 */
public class ImageSorter implements Comparator<IImage> {

    @Override
    public int compare(IImage o1, IImage o2) {
        if (o2 == null) {
            return 1;
        }
        if (o1 == null) {
            return -1;
        }
        int res = o1.getDifference()- o2.getDifference();
        if (res < 0) {
            return -1;
        } else if (res > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    
}
