/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.impulsm.tenderrobot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;

/**
 *
 * @author vinichenkosa
 */
public class Common {

    @Test
    public void DecoderBase64StringAndSave() throws SQLException {
        try(Connection connection = DriverManager.getConnection("jdbc:mysql://vinichenkosa.org:3306/tender_robot?zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8", "tender_robot", "tl-wn821NC");
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM TASK");
                ResultSet rs = ps.executeQuery();){
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSSSSSSS");
            while(rs.next()){
                
                System.out.println("Start: "+sdf.format(new Date(rs.getTimestamp("STARTTIME").getTime())));
                System.out.println("End: "+sdf.format(new Date(rs.getTimestamp("ENDTIME").getTime())));
            }
        }
        
    }

}
