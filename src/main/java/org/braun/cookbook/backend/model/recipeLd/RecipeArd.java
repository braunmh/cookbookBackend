package org.braun.cookbook.backend.model.recipeLd;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

        @Override
        public void startDocument() throws SAXException {
            recipe = new RecipeLd();
            recipe.setImage(new ArrayList<>());
            step = ParseType.other;
            characters = new CharArrayWriter();
            image = new Image();
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
                case other:
                    if ("meta".equals(localName)) {
                        if (atts.getValue("itemprop") != null) {
                            String name = atts.getValue("itemprop");
                            switch (name) {
                                case "description":
                                    recipe.setDescription(new Text().add(atts.getValue("content")));
                                    break;
                                case "datePublished":
                                    recipe.setDatePublished(DateTime.parse(atts.getValue("content")));
                                    break;
                                case "dateModified":
                                    recipe.setDatePublished(DateTime.parse(atts.getValue("content")));
                                    break;
                                case "totalTime":
                                    recipe.setTotalTime(RecipeDuration.parse(atts.getValue("content")));
                                    break;
                                case "cookTime":
                                    recipe.setTotalTime(RecipeDuration.parse(atts.getValue("content")));
                                    break;
                                case "recipeCategory":
                                    String content = atts.getValue("content");
                                    if (content != null) {
                                        recipe.setRecipeCategory(new Text().addAll(Arrays.asList(content.split(" ,"))));
                                    }
                                    break;
                                case "recipeInstructions":
                                    content = atts.getValue("content");
                                    if (content != null) {
                                        recipe.setRecipeInstructions(new RecipeInstruction());
                                        recipe.getRecipeInstructions().addAll(Arrays.asList(content.split(" ,")));
                                    }
                                    break;

                            }
                        } else if (atts.getValue("property") != null) {
                            switch (atts.getValue("property")) {
                                case "og:url":
                                    recipe.setMainEntityOfPage(new WebPage().id(atts.getValue("content")));
                                    break;
                                case "og:title":
                                    recipe.setName(new Text().add(atts.getValue("content")));
                                    break;
                                case "og:image":
                                    if (image.isFilled()) {
                                        recipe.getImage().add(image.toImageObject());
                                    }
                                    image.setUrl(atts.getValue("content"));
                                    break;
                                case "og:image:width":
                                    image.setWidth(atts.getValue("content"));
                                    break;
                                case "og:image:height":
                                    image.setHeight(atts.getValue("content"));
                                    break;
                            }
                        }
                    } else if ("span".equals(localName) && atts.getValue("itemprop") != null) {
                        switch (atts.getValue("itemprop")) {
                            case "image":
                                initStack();
                                imageUrl = null;
                                width = -1;
                                height = -1;
                                step = ParseType.image;
                                break;
                            case "publisher":
                                initStack();
                                step = ParseType.publisher;
                                break;
                            case "author":
                                initStack();
                                step = ParseType.author;
                                break;
                        }
                        // image, publisher, author
                    } else if ("dd".equals(localName) && atts.getValue("itemprop") != null) {
                        switch (atts.getValue("itemprop")) {
                            case "recipeYield":
                                initStack();
                                step = ParseType.recipeYield;
                                break;
                            case "nutrition":
                                initStack();
                                step = ParseType.nutrition;
                                break;
                        }

                    } else if ("div".equals(localName) && "recipe-ingredients".equals(atts.getValue("class"))) {
                        initStack();
                        step = ParseType.recipeIngredient;
                    }
                    break;
                case publisher:
                    parsePublisher(uri, localName, qName, atts);
                    break;
                case author:
                    parseAuthor(uri, localName, qName, atts);
                    break;
                case recipeYield:
                    parseRecipeYield(uri, localName, qName, atts);
                    break;
                case recipeIngredient:
                    parseRecipeIngredient(uri, localName, qName, atts);
                    break;
                case nutrition:
                    parseNutrition(uri, localName, qName, atts);
                    break;
                default:
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            switch (step) {
                case recipeYield:
                    if ("dd".equals(localName)) {
                        step = ParseType.other;
                        recipe.setRecipeYield(new Text().add(characters.toString().trim()));
                    }
                    break;
                case nutrition:
                    if ("dd".equals(localName)) {
                        step = ParseType.other;
                        recipe.setNutrition(NutritionInformation.parse(characters.toString().trim()));
                    }
                    break;
                case recipeIngredient:
                    switch (localName) {
                        case "li":
                            if (ingredient != null && !ingredient.isEmpty()) {
                                recipe.getRecipeIngredient().getSections().get(0)
                                    .addIngredients(ingredient.toString().trim());
                            }
                            break;
                        case "span":
                            ingredient.append(" ").append(characters.toString().trim());
                            characters.reset();
                            break;
                        case "div":
                            if (stack == ptStack) {
                                step = ParseType.other;
                            }
                            break;
                    }
                    break;
                case image:
                case publisher:
                case author:
                    if (stack == ptStack) {
                        step = ParseType.other;
                    }
                    break;
                case other:
                    super.endElement(uri, localName, qName);
                    break;
                default:
                    System.out.println(localName + " not found " + step);
                    super.endElement(uri, localName, qName);
            }
        }

        private int stack;

        private int ptStack;

        String imageUrl;
        int width;
        int height;

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
            if (localName.equals("li")) {
                recipe.getRecipeIngredient().addSection(new RecipeIngredientSection());
                ingredient = new StringBuilder();
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
