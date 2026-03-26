package org.braun.cookbook.backend.process;

import org.braun.cookbook.backend.model.Keyword;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author mbraun
 */
public class KeywordFacadeTest extends BaseTest {
    
    public KeywordFacadeTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getByName method, of class KeywordFacade.
     */
    @Test
    public void testGetByName() {
        System.out.println("getByName");
        String name = "";
        KeywordFacade instance = getKeywordFacade();
        Keyword result = instance.getByName("Deutschland");
        System.out.println(result);
    }
    
}
