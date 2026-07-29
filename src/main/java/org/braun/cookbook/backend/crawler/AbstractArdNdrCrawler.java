package org.braun.cookbook.backend.crawler;

import org.braun.cookbook.common.EndOfProcessing;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Named;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.JsonFilter;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeLd;
import static org.braun.cookbook.backend.model.RecipeLd.getRecipeFromJson;
import org.braun.cookbook.backend.model.recipe.Ingredient;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipeLd.NutritionInformation;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
public abstract class AbstractArdNdrCrawler extends Crawler {
    
    @Override
    protected Recipe getRecipe(String url) {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            return parse(inputStream);
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }
    
    
    @Override
    protected List<String> getNewRecipes() {
        String prefix = "https://www.ndr.de";
        OverviewFilter filter = new OverviewFilter(prefix);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + getSite()))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            Parser reader = new Parser();
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            InputSource inputSource = new InputSource(inputStream);
            filter.setParent(reader);
            filter.parse(inputSource);
        } catch (EndOfProcessing e) {
            // Just ignore
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return filter.getUrls();
    }

    protected String getSite() {
        return "/ratgeber/kochen";
    }
    
    protected Recipe parse(InputStream inputStream) throws SAXException, IOException {
        Parser parser = new Parser();
        parser.setFeature(Parser.namespacePrefixesFeature, false);
        InputSource inputSource = new InputSource(inputStream);
        
        JsonFilter jsonFilter = new JsonFilter();
        jsonFilter.setParent(parser);
        
        IngredientFilter ingredientFilter = new IngredientFilter();
        ingredientFilter.setParent(jsonFilter);
        
        NdrNutrientFilter ndrNutrientFilter = new NdrNutrientFilter();
        ndrNutrientFilter.setParent(ingredientFilter);
        
        ndrNutrientFilter.parse(inputSource);
        RecipeLd recipeLd = getRecipeFromJson(jsonFilter.getJson());
        
        if (ndrNutrientFilter.getNutrient() != null) {
            recipeLd.setNutrition(NutritionInformation.parse(ndrNutrientFilter.getNutrient()));
        }
        Recipe recipe = toRecipe(recipeLd, "ARD SWR");
        if (!ingredientFilter.getIngredients().isEmpty()) {
            recipe.getIngredients().clear();
            for (Ingredients i : ingredientFilter.getIngredients()) {
                recipe.addIngredients(i);
            }
        }
        return recipe;
    }
    
    class OverviewFilter  extends XMLFilterImpl {

        enum ParseType  {
            other, overviewSection, divTeaser, href
        };
        
        List<String> sectionOverviewClasses = List.of("teasergroup", "mosaik", "group-s-100", "group-m-33");
        private ParseType step;
        private final List<String> urls;
        protected int stack;
        protected int ptStack;
        protected CharArrayWriter characters;
        private final String prefix;

        public OverviewFilter(String prefix) {
            urls = new ArrayList<>();
            stack = 0;
            characters = new CharArrayWriter();
            step = ParseType.other;
            this.prefix = prefix;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("section".equals(localName) && classAttributesToList(atts).containsAll(sectionOverviewClasses)) {
                        step = ParseType.overviewSection;
                    }
                }
                case overviewSection -> {
                    if ("div".equals(localName) && "teaser".equals(atts.getValue("class"))) {
                        step = ParseType.divTeaser;
                    }
                }
                case divTeaser -> {
                    if ("h2".equals(localName)) {
                        step = ParseType.href;
                    }
                }
                case href -> {
                    if ("a".equals(localName)) {
                        String url = atts.getValue("href");
                        if (url.startsWith(prefix)) {
                            urls.add(url);
                        } else {
                            urls.add(prefix + url);
                        }
                        step = ParseType.divTeaser;
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            switch (step) {
                case other -> super.endElement(uri, localName, qName);
                case divTeaser -> {
                    if ("section".equals(localName)) {
                        throw new EndOfProcessing();
                    }
                }
            }
        }

        List<String> classAttributesToList(Attributes atts) {
            String value = atts.getValue("class");
            if (StringUtils.isBlank(value)) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            for (String v : value.split(" ")) {
                result.add(v);
            }
            return result;
        }
        
        protected void initStack() {
            ptStack = stack;
            characters.reset();
        }

        public List<String> getUrls() {
            return urls;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step != ParseType.other) {
                characters.write(ch, start, length);
            }
        }

    }
    
    /**
     * <ul>
     * <li>suche nach article</li>
     * <li> h2 class="ingredients-headline" falls Inhalt nur "Zutaten:" ignorieren 
     * ansonsten kürezen um Zutaten für als title</li>
     * <li> ul class="ingredients"</li>
     * <li> li content als ingredient</li>
     * </ul>
     */
    class IngredientFilter extends XMLFilterImpl {
        
        enum Step {other, article, headline, ingredients, ingredient, finish};
        
        private List<Ingredients> ingredients;
        
        private Step step;
        
        private int stack;
        private CharArrayWriter writer;
        
        public IngredientFilter() {
            step = Step.other;
            ingredients = new ArrayList<>();
            writer = new CharArrayWriter();
            stack = 0;
        }

        public List<Ingredients> getIngredients() {
            return ingredients;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("article".equals(localName)) {
                        step = Step.article;
                        stack = 0;
                    }
                }
                case article -> {
                    if ("h2".equals(localName) && "ingredients-headline".equals(atts.getValue("class"))) {
                        writer.reset();
                        step = Step.headline;
                    }
                }
                case ingredients -> {
                    if ("ul".equals(localName) && "ingredients".equals(atts.getValue("class"))) {
                        step = Step.ingredient;
                    }
                }
                case ingredient -> {
                    if ("li".equals(localName)) {
                        writer.reset();
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            switch(step) {
                case article -> {
                   if ("article".equals(localName)) {
                       step = Step.finish;
                   } 
                }
                case headline -> {
                    if ("h2".equals(localName)) {
                        String title = writer.toString();
                        if ("Zutaten:".equals(title)) {
                            title = "";
                        }
                        ingredients.add(new Ingredients().title(title));
                        step = Step.ingredient;
                    }
                }
                case ingredient -> {
                    if ("ul".equals(localName)) {
                        step = Step.article;
                    } else if ("li".equals(localName)) {
                        Ingredient ingredient = Ingredient.parse(writer.toString());
                        ingredients.getLast().add(ingredient);
                    } else {
                        writer.append(" ");
                    }
                }
            }
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step == Step.headline || step == Step.ingredient) {
                writer.write(ch, start, length);
            }
            super.characters(ch, start, length);
        }
        
        
    }
    
    class NdrNutrientFilter extends XMLFilterImpl {
    
        enum Step {other, nutrient, content, finish};
        
        private Step step = Step.other;
        private CharArrayWriter writer = new CharArrayWriter();
        private String nutrient;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            switch (step) {
                case other -> {
                    if ("h2".equals(localName) && "Naehrwerte-pro-Portion".equals(atts.getValue("id"))) {
                        step = Step.nutrient;
                    }
                }
                case nutrient -> {
                    if ("p".equals(localName)) {
                        step = Step.content;
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (step == Step.content && "p".equals(localName)) {
                step = step.finish;
                nutrient = "Nährwerte / Portion: " + writer.toString();
            }
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step == Step.content) {
                writer.write(ch, start, length);
            }
            super.characters(ch, start, length);
        }

        public String getNutrient() {
            return nutrient;
        }
        
    }
}
