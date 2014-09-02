package com.impulsm.tenderrobot;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.server.SeleniumServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static void main(String[] args) throws Exception {
        SeleniumServer _server = new SeleniumServer();
        _server.boot();
        _server.start();

        try {
            // The Firefox driver supports javascript 
            WebDriver driver = new FirefoxDriver();

            // Go to the Google Suggest home page
            driver.get("http://utender.ru/");

            // Enter the query string "Cheese"
            WebElement login = driver.findElement(By.id("ctl00_ctl00_LeftContentLogin_ctl00_Login1_UserName"));
            login.sendKeys("chudilos");
            WebElement password = driver.findElement(By.id("ctl00_ctl00_LeftContentLogin_ctl00_Login1_Password"));
            password.sendKeys("123123test+");
            driver.findElement(By.id("ctl00_ctl00_LeftContentLogin_ctl00_Login1_Login")).submit();
            WebElement loggedId = driver.findElement(By.id("ctl00_ctl00_LeftContentLogin_ctl00_ctl00_LoginName1"));
            while (true) {
                if (loggedId.isDisplayed()) {
                    break;
                }
            }

            driver.quit();
        } catch (Exception ex) {
            logger.error("Ошибка выполнения.", ex);
        } finally {
            _server.stop();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

}
