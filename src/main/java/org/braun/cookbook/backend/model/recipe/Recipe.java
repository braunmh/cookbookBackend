package org.braun.cookbook.backend.model.recipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.recipe.sax.RecipeHandler;
import org.braun.cookbook.backend.model.recipe.sax.RecipeXmlReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public class Recipe implements EmptyElement {

    private static final Logger LOG = LogManager.getLogger();

    private String id;

    private boolean evaluated;

    private String imageUrl;

    private Integer width;

    private Integer height;

    private Integer rating;

    private String country;

    private String title;

    private String relativeName;

    private long lastModified;

    private Description description;

    private Source source;

    private Yield yield;

    private Nutrients nutrients;

    private Categories categories;

    private final List<Ingredients> ingredients;

    public Recipe() {
        ingredients = new ArrayList<>();
    }

    public static Recipe unmarshal(InputSource inputSource) throws SAXException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            RecipeHandler recipeHandler = new RecipeHandler();
            parser.parse(inputSource, recipeHandler);
            return recipeHandler.getRecipe();
        } catch (ParserConfigurationException e) {
            LOG.error("Parser configuration", e);
        } catch (IOException e) {
            LOG.error("Reading recipe", e);
        }
        throw new SAXException("Parsing recipe");
    }

    public static Recipe unmarshal(String baseDirectory, String relativeName) throws SAXException {
        try {
            File file = getFromPath(baseDirectory, relativeName);
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            Recipe recipe = unmarshal(new InputSource(reader));
            recipe.setRelativeName(relativeName);
            recipe.setLastModified(file.lastModified());
            return recipe;
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOG.error("Reading file", ex);
        }
        throw new SAXException("Reading file=" + relativeName);
    }

    private static File getFromPath(String baseDirectory, String url) {
        return new File(baseDirectory + "/" + url);
    }

    public void marshall(String directory, String entryName) throws IOException {
        File file = new File(directory + "/" + entryName);
        marshall(file);
    }

    public void marshall(File file) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        marshall(writer);
        setLastModified(file.lastModified());
    }

    public void marshall(OutputStream outputStream) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        marshall(writer);
    }

    public void marshall(Writer writer) throws IOException {
        try {
            RecipeXmlReader reader = new RecipeXmlReader();
            RecipeXmlReader.RecipeInputSource inputSource = reader.new RecipeInputSource((this));
            SAXSource saxSource = new SAXSource(reader, inputSource);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(writer);
            transformer.transform(saxSource, result);
        } catch (TransformerException ex) {
            LOG.error("Marshall failed", ex);
        } finally {
            if (writer != null) try {
                writer.close();
            } catch (IOException e) {
                LOG.error("Closing writer");
            }
        }
    }

    @Override
    public void toSaxStream(ContentHandler contentHandler) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        addAttribute(attrs, "docId", getId());
        addAttribute(attrs, "id", getId());
        addAttribute(attrs, "title", title);

        addAttribute(attrs, "country", country);
        addAttribute(attrs, "imageUrl", imageUrl);
        if (width != null) {
            addAttribute(attrs, "width", String.valueOf(width));
        }
        if (height != null) {
            addAttribute(attrs, "height", String.valueOf(height));
        }
        if (rating != null) {
            addAttribute(attrs, "rating", String.valueOf(rating));
        }
        addAttribute(attrs, "evaluated", Boolean.toString(evaluated));
        contentHandler.startElement("", "Recipe", "Recipe", attrs);
        if (source != null) {
            source.toSaxStream(contentHandler);
        }
        if (yield != null) {
            yield.toSaxStream(contentHandler);
        }
        if (categories != null) {
            categories.toSaxStream(contentHandler);
        }
        if (nutrients != null) {
            nutrients.toSaxStream(contentHandler);
        }
        if (description != null) {
            description.toSaxStream(contentHandler);
        }
        if (ingredients != null) {
            for (Ingredients ings : ingredients) {
                ings.toSaxStream(contentHandler);
            }
        }
        contentHandler.endElement("", "Recipe", "Recipe");
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean isEmpty() {
        return (title == null || title.isBlank()) || description.isEmpty();
    }

    public void setId(String id) {
        this.id = id;
    }

    public Recipe id(String value) {
        id = value;
        return this;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public Recipe evaluated(boolean value) {
        evaluated = value;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String value) {
        this.imageUrl = value;
    }

    public Recipe imageUrl(String value) {
        imageUrl = value;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Recipe title(String value) {
        title = value;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Recipe width(int value) {
        width = value;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Recipe height(int value) {
        height = value;
        return this;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Recipe rating(int value) {
        rating = value;
        return this;
    }

    public String getRelativeName() {
        return relativeName;
    }

    public void setRelativeName(String relativeName) {
        this.relativeName = relativeName;
    }

    public Recipe relativeName(String value) {
        relativeName = value;
        return this;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public Recipe lastModified(long value) {
        lastModified = value;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Recipe country(String value) {
        country = value;
        return this;
    }

    public Source getSource() {
        if (source == null) {
            source = new Source();
        }
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Description getDescription() {
        if (description == null) {
            description = new Description();
        }
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Yield getYield() {
        if (yield == null) {
            yield = new Yield();
        }
        return yield;
    }

    public void setYield(Yield yield) {
        this.yield = yield;
    }

    public Nutrients getNutrients() {
        if (nutrients == null) {
            nutrients = new Nutrients();
        }
        return nutrients;
    }

    public void setNutrients(Nutrients nutrients) {
        this.nutrients = nutrients;
    }

    public Categories getCategories() {
        if (categories == null) {
            categories = new Categories();
        }
        return categories;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    public List<Ingredients> getIngredients() {
        return ingredients;
    }

    public Recipe addIngredients(Ingredients value) {
        ingredients.add(value);
        return this;
    }

    @Override
    public String getTagName() {
        return "Recipe";
    }
}
