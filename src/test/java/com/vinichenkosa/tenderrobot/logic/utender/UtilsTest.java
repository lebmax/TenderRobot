/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vinichenkosa.tenderrobot.logic.utender;

import java.math.BigDecimal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static com.programmisty.numerals.Numerals.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 *
 * @author vinichenkosa
 */
public class UtilsTest {
    
    public UtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of bidToPhrase method, of class Utils.
     */
    @Test
    public void testBidToPhrase() {
        System.out.println("bidToPhrase");
        
        String expected = "Пятьсот семьдесят девять тысяч шестьсот шестьдесят рублей 74 копейки";
        BigDecimal bid = new BigDecimal("579660.74");

        
        String bidInRubles = russianRubles(bid);
        assertThat(bidInRubles, is(expected));
        
    }
    
}
