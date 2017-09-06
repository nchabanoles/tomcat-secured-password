package fr.chabanoles.tomcat.util.properties;

import java.io.File;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Nicolas Chabanoles
 */
public class EncryptedPropertiesSourceTest {
    private static final String PASSWORD_READABLE = "secret";
    private static final String PROPERTY = "db.password";

    EncryptedPropertiesSource externalPropertySource;

    @Before
    public void setup() throws Exception {
        // Simulate catalina.base with test resources.
        URL tomcatFolderURL = Thread.currentThread().getContextClassLoader().getResource("tomcat");
        String absolutePathDir = new File(tomcatFolderURL.toURI()).getAbsolutePath();
        System.setProperty("catalina.base", absolutePathDir);

        // Instantiate the ExternalPropertySource (as Tomcat would)
        externalPropertySource = new EncryptedPropertiesSource();
    }



    @Test
    public void should_decrypt_property() throws Exception {

        //Given:
        //When:
         // Read a property like Tomcat would when it finds a ${property} in conf/server.xml
         String property = externalPropertySource.getProperty(PROPERTY);

        //Then:
         // Check that the value found matches the expected password
         assertEquals(PASSWORD_READABLE, property);
    }

    @Test
    public void should_return_null_if_property_file_not_found() throws Exception {

        //Given:
         // a catalina base not valid to prevent Property Source to find its configuration file
         System.setProperty("catalina.base", "nowhere");
         externalPropertySource = new EncryptedPropertiesSource();

        //When:
         String property = externalPropertySource.getProperty(PROPERTY);

        //Then:
         assertNull(property);
    }

    @Test
    public void should_return_null_if_external_property_not_found() throws Exception {
        //Given:
        //When:
         String property = externalPropertySource.getProperty("non-existing-property-name");

        //Then:
         assertNull(property);
    }

}