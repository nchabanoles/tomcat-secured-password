package fr.chabanoles.tomcat.util.properties;

import static java.lang.Class.forName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import fr.chabanoles.encryption.PropertyDecoder;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils.PropertySource;

/**
 * A PropertySource that allows Tomcat to read properties in an external file whose values are crypted.
 *
 * Typical use-case is to externalize the password of jdbc datasources, to hide it from non authorized users.
 *
 * @author Nicolas Chabanoles
 */
public class EncryptedPropertiesSource implements PropertySource {

    private static final String CATALINA_PROPERTIES = "conf/catalina.properties";
    private static final String ENCRYPTED_PROPERTIES_FILE_PROPERTY = "fr.chabanoles.encrypted.properties.file";
    private static final Log LOGGER = LogFactory.getLog(EncryptedPropertiesSource.class);
    private Properties externalProperties;
    private PropertyDecoder propertyDecoder;

    public EncryptedPropertiesSource() {
        try {
            Properties catalinaProperties = loadCatalinaProperties();
            String encryptedPropertiesFilePath = getEncryptedPropertiesFilePath(catalinaProperties);
            FileInputStream fileInputStream = new FileInputStream(encryptedPropertiesFilePath);
            externalProperties = new Properties();
            externalProperties.load(fileInputStream);
            propertyDecoder = loadDecoder(catalinaProperties);
        } catch (IOException e) {
            LOGGER.fatal("Unable to read the encrypted property file", e);
            externalProperties = null;
        } catch (Exception e) {
            LOGGER.fatal("Unable to load implementation class to decrypt properties. Please make sure to have it in your classpath and that it is accessible.", e);
            externalProperties = null;
        }
    }

    private PropertyDecoder loadDecoder(Properties externalProperties) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String className = externalProperties.getProperty("fr.chabanoles.encrypted.properties.encryption.class");
        Class<?> clazz = Class.forName(className);
        return (PropertyDecoder) clazz.newInstance();
    }

    private String getEncryptedPropertiesFilePath(Properties properties) throws IOException {

        String externalPropertiesFile = properties.getProperty(ENCRYPTED_PROPERTIES_FILE_PROPERTY);
        if (externalPropertiesFile == null || externalPropertiesFile.isEmpty()) {
            throw new IOException("The encrypted property file location is not set in " + CATALINA_PROPERTIES + " (expected value for " + ENCRYPTED_PROPERTIES_FILE_PROPERTY + ")");
        }
        return externalPropertiesFile;
    }

    private Properties loadCatalinaProperties() throws IOException {
        String catalinaBase = System.getProperty("catalina.base");
        File catalinaPropertiesFile = new File(catalinaBase, CATALINA_PROPERTIES);
        if (!catalinaPropertiesFile.exists()) {
            throw new IOException("Unable to find the file " + CATALINA_PROPERTIES + " in CATALINA_BASE (" + catalinaBase + ")");
        }
        FileInputStream catalinaFileInputStream = new FileInputStream(catalinaPropertiesFile);
        Properties catalinaProperties = new Properties();
        catalinaProperties.load(catalinaFileInputStream);
        return catalinaProperties;
    }

    @Override
    public String getProperty(String string) {
        if (externalProperties != null)  {
            return decrypt(externalProperties.getProperty(string));
        } else {
            // If the property is not found, we return null (and Tomcat will leave the ${propertyname})
            return null;
        }
    }

    private String decrypt(String property) {
        try {
            return propertyDecoder.decrypt(property);
        } catch (Exception e) {
            LOGGER.fatal("Unable read the encrypted property", e);
            // if the property cannot be read, we return null (and Tomcat will leave the ${propertyname})
            return null;
        }
    }
}
