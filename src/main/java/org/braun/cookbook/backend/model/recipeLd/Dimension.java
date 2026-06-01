package org.braun.cookbook.backend.model.recipeLd;

import java.util.List;

class Dimension {

    public static final List<Dimension> DEFAULTS = List.of(
        new Dimension(1200, 700), new Dimension(1200, 900), new Dimension(1200, 1200)
    );
    
    final private int width, height;

    public Dimension(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
