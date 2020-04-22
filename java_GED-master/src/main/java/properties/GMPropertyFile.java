package properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: Michael Stauffer
 * Org.: University of Applied Sciences and Arts Northwestern Switzerland
 * Date: 24.05.17
 * Time: 10:07
 */
public class GMPropertyFile {

    public static void export(String propertyFile, HashMap<String,String> values){

        Properties prop = new Properties();
        OutputStream output = null;

        try {

            File propertyPath = new File(propertyFile);
            propertyPath.getParentFile().mkdir();

            output = new FileOutputStream(propertyFile);

            // set the properties value
            for(Map.Entry<String,String> entry : values.entrySet()){
                prop.setProperty(entry.getKey(),entry.getValue());
            }

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    static void addValue(HashMap<String, String> map, String key, String value, String defaultValue) {
        if (value != null) {
            map.put(key,value);
        } else {
            map.put(key,defaultValue);
        }
    }
}
