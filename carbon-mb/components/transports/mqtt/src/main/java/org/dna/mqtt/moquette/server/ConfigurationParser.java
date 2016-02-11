package org.dna.mqtt.moquette.server;

import org.dna.mqtt.commons.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.Properties;

/**
 * Mosquitto configuration parser.
 * <p/>
 * A line that at the very first has # is a comment
 * Each line has key value format, where the separator used it the space.
 *
 * @author andrea
 */
class ConfigurationParser {

    private static final Logger LOG = LoggerFactory.getLogger(org.dna.mqtt.moquette.server.ConfigurationParser.class);

    private Properties properties = new Properties();

    ConfigurationParser() {
        properties.put("port", Integer.toString(Constants.PORT));
        properties.put("host", Constants.HOST);
    }

    /**
     * Parse the configuration from file.
     */
    void parse(File file) throws ParseException {
        if (file == null) {
            LOG.warn("parsing NULL file, so fallback on default configuration!");
            return;
        }
        if (!file.exists()) {
            LOG.warn(String.format("parsing not existing file %s, so fallback on default configuration!", file
                    .getAbsolutePath()));
            return;
        }
        try {
            FileReader reader = new FileReader(file);
            parse(reader);
        } catch (FileNotFoundException fex) {
            LOG.warn(String.format("parsing not existing file %s, so fallback on default configuration!", file
                    .getAbsolutePath()), fex);
            return;
        }
    }

    /**
     * Parse the configuration
     *
     * @throws java.text.ParseException if the format is not compliant.
     */
    void parse(Reader reader) throws ParseException {
        if (reader == null) {
            //just log and return default properties
            LOG.warn("parsing NULL reader, so fallback on default configuration!");
            return;
        }

        BufferedReader br = new BufferedReader(reader);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                int commentMarker = line.indexOf('#');
                if (commentMarker != -1) {
                    if (commentMarker == 0) {
                        //skip its a comment
                        continue;
                    } else {
                        //it's a malformed comment
                        throw new ParseException(line, commentMarker);
                    }
                } else {
                    if (line.isEmpty() || line.matches("^\\s*$")) {
                        //skip it's a black line
                        continue;
                    }

                    //split till the first space
                    int deilimiterIdx = line.indexOf(' ');
                    String key = line.substring(0, deilimiterIdx).trim();
                    String value = line.substring(deilimiterIdx).trim();

                    properties.put(key, value);
                }
            }
        } catch (IOException ex) {
            throw new ParseException("Failed to read", 1);
        }
    }

    Properties getProperties() {
        return properties;
    }
}
