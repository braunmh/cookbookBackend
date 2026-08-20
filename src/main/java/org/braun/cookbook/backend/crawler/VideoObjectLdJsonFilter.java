package org.braun.cookbook.backend.crawler;

import java.io.CharArrayWriter;
import java.io.StringReader;
import org.braun.cookbook.backend.model.VideoObjectLdJson;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
public class VideoObjectLdJsonFilter extends XMLFilterImpl {

    enum Step {
        parse, script, done;
    };
    
    private Step step = Step.parse;
    
    private VideoObjectLdJson videoObjectLdJson;
    
    private CharArrayWriter writer = new CharArrayWriter();
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (step == Step.parse) {
            if ("script".equals(qName) && "application/ld+json".equals(atts.getValue("type"))) {
                step = Step.script;
                writer.reset();
            }
        }
        super.startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (step == Step.script && "script".equals(qName)) {
            String script = writer.toString();
            if (script.isBlank() || script.length() > 256000) {
                step = Step.parse;
                return;
            }
            videoObjectLdJson = VideoObjectLdJson.parse(new StringReader(script));
            step = (videoObjectLdJson == null) ? Step.parse : Step.done; 
        }
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (step == Step.script) {
            writer.write(ch, start, length);
        }
        super.characters(ch, start, length); 
    }

    public VideoObjectLdJson getVideoObjectLdJson() {
        return videoObjectLdJson;
    }
    
}
