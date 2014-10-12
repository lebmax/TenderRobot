/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vinichenkosa.tenderrobot.model.utender;

import static org.hamcrest.CoreMatchers.*;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vinichenkosa
 */
public class UtenderHttpCommonTest {

    public UtenderHttpCommonTest() {
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

    @Test
    public void testGetTime() {
        System.out.println("getTime");
        DateTime result = UtenderHttpCommon.getTime("http://utender.ru/");
        System.out.println("Result is " + result);
        assertThat(result, notNullValue());

    }

}
