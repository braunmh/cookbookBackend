package org.braun.cookbook.backend.model.recipe.sax;

import java.io.CharArrayWriter;
import org.braun.cookbook.backend.model.recipe.Categories;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.model.recipe.Description;
import org.braun.cookbook.backend.model.recipe.Heading;
import org.braun.cookbook.backend.model.recipe.Ingredient;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipe.ListElement;
import org.braun.cookbook.backend.model.recipe.ListItem;
import org.braun.cookbook.backend.model.recipe.Nutrient;
import org.braun.cookbook.backend.model.recipe.Nutrients;
import org.braun.cookbook.backend.model.recipe.OrderedList;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Source;
import org.braun.cookbook.backend.model.recipe.UnorderedList;
import org.braun.cookbook.backend.model.recipe.Yield;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author mbraun
 */
public class RecipeHandler extends DefaultHandler {

    private Categories categories;
    private Yield yield;
    private Ingredients ingredients;
    private Ingredient ingredient;
    private Description description;
    private Nutrients nutrients;
    private Nutrient nutrient;
//   Recipes recipes;
    Recipe recipe;
    private Source source;

    private Paragraph paragraph;
    private ListElement listTag;
    private ListItem listItem;
    private Heading heading;

    private CharArrayWriter writer;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        writer.write(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        writer.reset();
        writer = null;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName.toLowerCase()) {
            case "recipe":
                if (yield == null) {
                    recipe.setYield(new Yield());
                }
                if (source == null) {
                    recipe.setSource(new Source());
                }
                if (categories == null) {
                    recipe.setCategories(new Categories());
                }
                break;
            case "description":
                recipe.setDescription(description);
                break;
            case "ingredients":
                recipe.getIngredients().add(ingredients);
                break;
            case "yield":
                yield.setValue(getContent());
                recipe.setYield(yield);
                break;
            case "nutrients":
                recipe.setNutrients(nutrients);
                break;
            case "categories":
                recipe.setCategories(categories);
                break;
            case "ingredient":
                ingredient.setValue(getContent());
                ingredients.add(ingredient);
                break;
            case "nutrient":
                nutrients.add(nutrient);
                break;
            case "source":
                source.setValue(getContent());
                recipe.setSource(source);
                break;
            case "p":
                paragraph.setValue(getContent());
                description.add(paragraph);
                break;
            case "li":
                listItem.setValue(getContent());
                listTag.add(listItem);
                break;
            case "ul":
            case "ol":
                description.add(listTag);
                break;
            case "h1":
            case "h2":
            case "h3":
                heading.setValue(getContent());
                description.add(heading);
                break;
            default:
                break;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        writer = new CharArrayWriter();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        writer.reset();
        switch (qName.toLowerCase()) {
            case "recipe":
                recipe = new Recipe()
                        .title(getValue(attributes, "title", "name"))
                        .imageUrl(getValue(attributes, "imageUrl"))
                        .id(getValue(attributes, "id", "docId"))
                        .country(getValue(attributes, "country"))
                        .width(getIntValue(attributes, "width"))
                        .height(getIntValue(attributes, "height"))
                        .rating(getIntValue(attributes, "rating"))
                        .evaluated(getBoolValue(attributes, "evaluated"));
                break;
            case "description":
                description = new Description();
                break;
            case "ingredients":
                ingredients = new Ingredients();
                ingredients.setTitle(getValue(attributes, "title"));
                break;
            case "yield":
                yield = new Yield();
                yield.setUnit(getValue(attributes, "unit"));
                break;
            case "nutrients":
                nutrients = new Nutrients();
                nutrients.setUnit(getValue(attributes, "unit"));
                break;
            case "categories":
                categories = new Categories();
                break;
            case "category":
                categories.add(new Category().name(getValue(attributes, "name")));
                break;
            case "ingredient":
                ingredient = new Ingredient();
                ingredient.setCount(getValue(attributes, "count"));
                ingredient.setUnit(getValue(attributes, "unit"));
                String content = getValue(attributes, "description");
                if (content.length() > 0) {
                    writer.write(content, 0, content.length());
                }   break;
            case "nutrient":
                nutrient = new Nutrient();
                nutrient.setUnit(getValue(attributes, "unit"));
                nutrient.setCount(getValue(attributes, "count"));
                nutrient.setContent(getValue(attributes, "content"));
                break;
            case "source":
                source = new Source();
                source.setUrl(getValue(attributes, "url"));
                break;
            case "p":
                paragraph = new Paragraph();
                break;
            case "h1":
            case "h2":
            case "h3":
                heading = new Heading();
                break;
            case "li":
                listItem = new ListItem();
                break;
            case "ul":
                listTag = new UnorderedList();
                break;
            case "ol":
                listTag = new OrderedList();
                break;
            default:
                break;
        }
    }

    public Recipe getRecipe() {
        return recipe;
    }

    private String getContent() {
        String value = writer.toString();
        writer.reset();
        return value;
    }

    private String getValue(Attributes attributes, String name, String alternateName) {
        String value = attributes.getValue(name);
        return (value == null) ? getValue(attributes, alternateName) : value.trim();
    }

    private String getValue(Attributes attributes, String name) {
        String value = attributes.getValue(name);
        return (value == null) ? "" : value.trim();
    }

    private int getIntValue(Attributes attributes, String name) {
        String value = attributes.getValue(name);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean getBoolValue(Attributes attributes, String name) {
        String value = attributes.getValue(name);
        if (value == null) {
            return false;
        }
        return "true".equals(value);
    }
}
