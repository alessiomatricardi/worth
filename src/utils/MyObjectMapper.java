package worth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;

/**
 * Created by alessiomatricardi on 12/01/21
 *
 * ObjectMapper con funzionalità estese
 */
public class MyObjectMapper extends ObjectMapper {

    public MyObjectMapper() {
        super();

        // abilita indentazione
        this.enable(SerializationFeature.INDENT_OUTPUT);

        // formattazione data
        this.registerModule(new JavaTimeModule());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        this.setDateFormat(dateFormat);

        // non stampare data come timestamp
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

}
