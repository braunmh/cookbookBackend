package org.braun.cookbook.backend.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 *
 * @author mbraun
 */
public class HtmlSnippetReader extends Reader {

    private final String tagStart;
    private final String tagEnd;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private StringWriter writer;
    private StringReader reader;
    
    public HtmlSnippetReader(InputStream inputStream, String tagStart, String tagEnd) throws IOException {
        this.inputStream = inputStream;
        this.tagEnd = tagEnd;
        this.tagStart = tagStart;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new StringWriter();
        extractSnippet();
    }
    
    private void extractSnippet() throws IOException {
        String line = null;
        writer.write("<html>\n<head>\n</head>\n<body>\n");
        boolean isFirst = true;
        while (null != (line = bufferedReader.readLine())) {
            int start = line.indexOf(tagStart);
            if (start > -1) {
                writer.write(line.substring(start));
            } else {
                start = 0;
            }
            int end = line.indexOf(tagEnd, start);
            if (end > 0) {
                writer.write(line.substring(start, end + tagEnd.length()));
            }
        }
        writer.write("\n</body>\n</html>");
        reader = new StringReader(writer.toString());
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return reader.read(cbuf, off, len); 
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (bufferedReader != null) {
            bufferedReader.close();
        }
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
        reader = null;
        writer = null;
        bufferedReader = null;
        inputStream = null;
    }
}
