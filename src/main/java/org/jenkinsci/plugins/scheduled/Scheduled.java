/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.scheduled;

import org.jenkinsci.plugins.scheduled.exceptions.InvalidInputException;

/**
 *
 * @author gabi
 */
public class Scheduled{// implements Describable<Scheduled>{
    
    String description;
    String minute;
    String hour;
    String meridiem;

    public Scheduled(String minute, String hour, String meridiem, String description) throws InvalidInputException
    {
        validateInput(minute, hour, meridiem);
        this.minute = minute;
        this.hour = convertHourByMeridiem(hour, meridiem);
        this.description = description;
    }
    
    public String getSpec()
    {
        return "#" + description + "\n" + minute + " " + hour + " * * *";
    }
    
    @Override
    public String toString()
    {
        return this.description.substring(1) + ": Daily at " + hour + ":" + minute;
    }
    
    private void validateInput(String minute, String hour, String meridiem) throws InvalidInputException
    {
        int minuteInt = Integer.valueOf(minute);
        int hourInt = Integer.valueOf(hour);
        
        if (minuteInt < 0 || minuteInt > 55)
        {
            throw new InvalidInputException("Minute must be between 0 and 55");
        }
        if (hourInt < 0 || hourInt > 11)
        {
            throw new InvalidInputException("Hour must be between 0 and 11");
        }
        if (meridiem.equals("PM") == false && meridiem.equals("AM") == false)
        {
            throw new InvalidInputException("Meridiem must be PM or AM");
        }
    }
    
    private String convertHourByMeridiem(String hour, String meridiem)
    {
        int hourNum;
        
        if (meridiem.equals("PM"))
        {
            hourNum = Integer.parseInt(hour);
            hourNum += 12;
            hour = String.valueOf(hourNum);
        }
        
        return hour;
    }
    
    public static void getHourAndMerdidiem(String unmeridiemedHour, StringBuilder hour, StringBuilder meridiem) 
    {
        int unmeridiemedHourInt = Integer.valueOf(unmeridiemedHour);
        if (unmeridiemedHourInt >= 0 && unmeridiemedHourInt <= 11)
        {
            hour.append(unmeridiemedHour);
            meridiem.append("AM");
        }
        else if (unmeridiemedHourInt >= 12 && unmeridiemedHourInt <= 21)
        {
            hour.append("0").append(String.valueOf(unmeridiemedHourInt - 12));
            meridiem.append("PM");
        }
        else if (unmeridiemedHourInt >= 22 && unmeridiemedHourInt <= 23)
        {
            hour.append(String.valueOf(unmeridiemedHourInt - 12));
            meridiem.append("PM");
        }
    }
}
