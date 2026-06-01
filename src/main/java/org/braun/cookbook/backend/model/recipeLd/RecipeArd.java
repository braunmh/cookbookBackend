package org.braun.cookbook.backend.model.recipeLd;

import org.braun.cookbook.backend.model.RecipeLd;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.util.Constants;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
public class RecipeArd {

    private static final Logger LOG = LogManager.getLogger();

    static enum ParseType {
        image, publisher, author, recipeIngredient, recipeYield, nutrition, other,
    }

    public static RecipeLd parse(InputStream inputStream) {
        try {
            Parser reader = new Parser();
            reader.setFeature(Parser.namespacePrefixesFeature, false);

            InputSource inputSource = new InputSource(inputStream);
            ItempropFilter itempropFilter = new ItempropFilter();
            itempropFilter.setParent(reader);

            itempropFilter.parse(inputSource);
            return itempropFilter.getRecipe();
        } catch (IOException | SAXException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    static class ItempropFilter extends XMLFilterImpl {

        private ParseType step;

        private RecipeLd recipe;
        
        private Image image;
        
        private CharArrayWriter characters;
        
        private StringBuilder ingredient;
        
        private List<String> ingredients;
        
        private String ingredientTitle = "";

        @Override
        public void startDocument() throws SAXException {
            recipe = new RecipeLd();
            recipe.setImage(new ArrayList<>());
            step = ParseType.other;
            characters = new CharArrayWriter();
            image = new Image();
            ingredients = new ArrayList<>();
        }

        @Override
        public void endDocument() throws SAXException {
            if (image.isFilled()) {
                recipe.getImage().add(image.toImageObject());
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("meta".equals(localName)) {
                        if (atts.getValue("itemprop") != null) {
                            String name = atts.getValue("itemprop");
                            switch (name) {
                                case "description" -> recipe.setDescription(new Text().add(atts.getValue("content")));
                                case "datePublished" -> recipe.setDatePublished(DateTime.parse(atts.getValue("content")));
                                case "dateModified" -> recipe.setDatePublished(DateTime.parse(atts.getValue("content")));
                                case "totalTime" -> recipe.setTotalTime(RecipeDuration.parse(atts.getValue("content")));
                                case "cookTime" -> recipe.setTotalTime(RecipeDuration.parse(atts.getValue("content")));
                                case "recipeCategory" -> {
                                    String content = atts.getValue("content");
                                    if (content != null) {
                                        Text text = new Text();
                                        for (String k : content.split(Constants.Strings.KEYWORD_SPLIT)) {
                                            text.add(k.trim());
                                        }
                                        recipe.setRecipeCategory(text);
                                    }
                                }
                                case "recipeInstructions" -> {
                                    String content = atts.getValue("content");
                                    if (content != null) {
                                        recipe.setRecipeInstructions(new RecipeInstruction());
                                        for (String c : content.split("( ,)|(\\.,)")) {
                                            c = c.replaceAll("<strong>", "");
                                            c = c.replaceAll("</strong>", "");
                                            recipe.getRecipeInstructions().add(c);
                                        }
                                    }
                                }

                            }
                        } else if (atts.getValue("property") != null) {
                            switch (atts.getValue("property")) {
                                case "og:url" -> recipe.setMainEntityOfPage(new WebPage().id(atts.getValue("content")));
                                case "og:title" -> recipe.setName(new Text().add(atts.getValue("content")));
//                                case "og:image" -> {
//                                    if (image.isFilled()) {
//                                        recipe.getImage().add(image.toImageObject());
//                                    }
//                                    image.setUrl(atts.getValue("content"));
//                                }
//                                case "og:image:width" -> image.setWidth(atts.getValue("content"));
//                                case "og:image:height" -> image.setHeight(atts.getValue("content"));
                            }
                        }
                    } else if ("span".equals(localName) && atts.getValue("itemprop") != null) {
                        switch (atts.getValue("itemprop")) {
                            case "image" -> {
                                initStack();
                                if (image.isFilled()) {
                                    recipe.getImage().add(image.toImageObject());
                                }
                                image.reset();
                                step = ParseType.image;
                            }
                            case "publisher" -> {
                                initStack();
                                step = ParseType.publisher;
                            }
                            case "author" -> {
                                initStack();
                                step = ParseType.author;
                            }
                        }
                        // image, publisher, author
                    } else if ("dd".equals(localName) && atts.getValue("itemprop") != null) {
                        switch (atts.getValue("itemprop")) {
                            case "recipeYield" -> {
                                initStack();
                                step = ParseType.recipeYield;
                            }
                            case "nutrition" -> {
                                initStack();
                                step = ParseType.nutrition;
                            }
                        }

                    } else if ("div".equals(localName) && "recipe-ingredients".equals(atts.getValue("class"))) {
                        initStack();
                        step = ParseType.recipeIngredient;
                    }
                }
                case publisher -> parsePublisher(uri, localName, qName, atts);
                case author -> parseAuthor(uri, localName, qName, atts);
                case recipeYield -> parseRecipeYield(uri, localName, qName, atts);
                case recipeIngredient -> parseRecipeIngredient(uri, localName, qName, atts);
                case nutrition -> parseNutrition(uri, localName, qName, atts);
                case image -> {
                    if ("link".equals(localName) && "url contentUrl".equals(atts.getValue("itemprop"))) {
                        image.setUrl(atts.getValue("href"));
                    } else if ("meta".equals(localName)) {
                        String itemprop = atts.getValue("itemprop");
                        switch (itemprop) {
                            case null -> {}
                            case "height" -> {
                                image.setHeight(atts.getValue("content"));
                            }
                            case "width" -> {
                                image.setWidth(atts.getValue("content"));
                            }
                            default -> {}
                        }
                    }
                }
                default -> {
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            switch (step) {
                case recipeYield -> {
                    if ("dd".equals(localName)) {
                        step = ParseType.other;
                        recipe.setRecipeYield(new Text().add(characters.toString().trim()));
                    }
                }
                case nutrition -> {
                    if ("dd".equals(localName)) {
                        step = ParseType.other;
                        recipe.setNutrition(NutritionInformation.parse(characters.toString().trim()));
                    }
                }
                case recipeIngredient -> {
                    switch (localName) {
                        case "li" -> {
                            if (ingredient != null && !ingredient.isEmpty()) {
                                ingredients.add(ingredient.toString());
                            }
                        }
                        case "span" -> {
                            ingredient.append(" ").append(characters.toString().trim());
                            characters.reset();
                        }
                        case "div" -> {
                            if (stack == ptStack) {
                                step = ParseType.other;
                            }
                        }
                        case "ul" -> {
                            recipe.getRecipeIngredient().getSections().getLast()
                                        .addIngredients(ingredients);
                            ingredients.clear();
                        }
                        case "h3" -> {
                            ingredientTitle = characters.toString();
                            characters.reset();
                        }
                    }
                }
                case image, publisher, author -> {
                    if (stack == ptStack) {
                        step = ParseType.other;
                    }
                }
                case other -> super.endElement(uri, localName, qName);
                default -> {
                    LOG.info("{} not found {}", localName, step);
                    super.endElement(uri, localName, qName);
                }
            }
        }

        private int stack;

        private int ptStack;

        private void initStack() {
            ptStack = stack;
            characters.reset();
        }

        private void parsePublisher(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("meta".equals(localName) && "name".equals(atts.getValue("itemprop"))) {
                recipe.setPublisher(new Person().name(atts.getValue("content")));
            }
        }

        private void parseAuthor(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("meta".equals(localName) && "name".equals(atts.getValue("itemprop"))) {
                recipe.setAuthor(new Person().name(atts.getValue("content")));
            }
        }

        private void parseRecipeIngredient(String uri, String localName, String qName, Attributes atts) throws SAXException {
            switch (localName) {
                case "ul" -> {
                    recipe.getRecipeIngredient().addSection(new RecipeIngredientSection());
                    if (StringUtils.isNotBlank(ingredientTitle)) {
                        recipe.getRecipeIngredient().getSections().getLast().setTitle(ingredientTitle);
                        ingredientTitle = "";
                    }
                }
                case "li" -> ingredient = new StringBuilder();
                case "h3" -> characters.reset();
            }
        }

        private void parseRecipeYield(String uri, String localName, String qName, Attributes atts) throws SAXException {
//        <dd itemprop="recipeYield">4</dd>
        }

        private void parseNutrition(String uri, String localName, String qName, Attributes atts) throws SAXException {
            // <dd itemprop="nutrition">Pro Portion: 660 kcal/ 2750 kJ / 46 g Kohlenhydrate, 21 g Eiweiß, 43 g Fett</dd>
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step != ParseType.other) {
                characters.write(ch, start, length);
            }
        }

        public RecipeLd getRecipe() {
            return recipe;
        }

        class Image {
            String url;
            int width;
            int height;

            public Image() {
                reset();
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(String width) {
                
                this.width = parseInt(width);
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(String height) {
                this.height = parseInt(height);
            }
            
            private int parseInt(String value) {
                if (value == null) {
                    return 0;
                }
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            
            public boolean isFilled() {
                return url != null;
            }
            
            private void reset() {
                url = null;
                width = -1;
                height = -1;
            }
            
            public ImageObject toImageObject() {
                ImageObject io = new ImageObject().url(url).height(height).width(width);
                reset();
                return io;
            }
        }
    }

}
