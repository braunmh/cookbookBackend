package org.braun.cookbook.backend.mapping;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.braun.cookbook.backend.entity.JobEntity;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Job;
import org.braun.cookbook.backend.model.JobStatus;

/**
 *
 * @author mbraun
 */
public class JobMapper {

    private static JobMapper instance = new JobMapper();

    private JobMapper() {
    }

    public static JobMapper getInstance() {
        return instance;
    }

    public Job map(JobEntity in) {
        if (in == null) {
            return null;
        }
        Job out = new Job()
                .id(in.getId())
                .message(in.getMessage())
                .finished(in.getFinished())
                .started(in.getStarted())
                .status(JobStatus.valueOf(in.getStatus()))
                .type(BackgroundJobType.valueOf(in.getType()))
                .addAllInformation(map(in.getInformation()));
        return out;
    }

    public JobEntity map(Job in) {
        if (in == null) {
            return null;
        }
        JobEntity out = new JobEntity(in.getId());
        out.setStatus(in.getStatus().name());
        out.setFinished(in.getFinished());
        out.setInformation(map(in.getInformation()));
        out.setMessage(in.getMessage());
        out.setStarted(in.getStarted());
        out.setType(in.getType().name());
        return out;
    }
    
    private String map(List<String> values) {
        if (values.isEmpty()) {
            return null;
        }
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        JsonArray array = Json.createArrayBuilder(values).build();
        jsonWriter.writeArray(array);
        return writer.toString();
    }

    private List<String> map(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        StringReader reader = new StringReader(value);
        JsonReader jsonReader = Json.createReader(reader);
        JsonArray array = jsonReader.readArray();
        List<String> res = array.stream()
                .filter(j -> j.getValueType() == JsonValue.ValueType.STRING)
                .map(j -> j.toString()).toList();
        return res;
    }
}
