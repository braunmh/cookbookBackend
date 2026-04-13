package org.braun.cookbook.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mbraun
 */
public class DialogParameters {

    private final Map<String, List<String>> params;
    
    private DialogParameters() {
        params = new HashMap<>();
    }
    
    public static class Parameter {
        
        private final String key;
        private final List<String> values;
        
        private Parameter(String key) {
            this.key = key;
            values = new ArrayList<>();
        }
        
        public Parameter add(String value) {
            values.add(value);
            return  this;
        }
        
        public Parameter add(boolean value) {
            values.add(String.valueOf(value));
            return  this;
        }
        
        public Parameter add(int value) {
            values.add(String.valueOf(value));
            return  this;
        }
        
        public Parameter add(long value) {
            values.add(String.valueOf(value));
            return  this;
        }

        public String getKey() {
            return key;
        }

        public List<String> getValues() {
            return values;
        }
        
        public static Parameter builder(String key) {
            return new Parameter(key);
        }
        
    }
    
    public DialogParameters parameter(Parameter param) {
        params.put(param.getKey(), param.getValues());
        return this;
    }
    
    public Map<String, List<String>> build() {
        return params;
    }
    
    public static DialogParameters builder() {
        return new DialogParameters();
    }
}
