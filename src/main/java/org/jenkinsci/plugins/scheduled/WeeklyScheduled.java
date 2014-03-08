/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.scheduled;

import org.jenkinsci.plugins.scheduled.exceptions.InvalidInputException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author gabi
 */
public class WeeklyScheduled extends Scheduled
{
    Map<String, String> daysOfTheWeek; 
    
    /**
     *
     * @param minute
     * @param hour
     * @param meridiem - AM/PM
     * @param daysOfWeek - in days names Sunday, Monday, etc...
     * @param description
     * @throws InvalidInputException
     */
    public WeeklyScheduled(String minute, String hour, String meridiem, ArrayList<String> daysOfWeek, String description) throws InvalidInputException
    {
        super(minute, hour, meridiem, description);
        this.daysOfTheWeek = parseDaysOfWeek(daysOfWeek);
    }
        
    /**
     *
     * @param minute
     * @param hour
     * @param meridiem - AM/PM
     * @param daysNumbers - in days numbers 1 for Sunday, 2 for Monday, etc...
     * @param description
     * @throws InvalidInputException
     */
    public WeeklyScheduled(String minute, String hour, String meridiem, String[] daysNumbers, String description) throws InvalidInputException
    {
        super(minute, hour, meridiem, description);
        ArrayList<String> daysArr = new ArrayList<String>();
        daysArr.addAll(Arrays.asList(daysNumbers));
        this.daysOfTheWeek = parseDaysOfWeek(daysArr);
    }
    
    private Map<String, String> parseDaysOfWeek(ArrayList<String> daysOfWeek) throws InvalidInputException 
    {
        Map<String, String> cronFormatDayOfTheWeek = new HashMap<String, String>();
        
        for(String dayOfWeek : daysOfWeek)
        {
            switch (dayOfWeek)
            {
                case "0":
                case "Sunday" : cronFormatDayOfTheWeek.put("0", "Sunady");
                    break;
                case "1":
                case "Monday": cronFormatDayOfTheWeek.put("1", "Monday");
                    break;
                case "2":
                case "Tuesday": cronFormatDayOfTheWeek.put("2", "Tuesday");
                    break;
                case "3":
                case "Wednesday": cronFormatDayOfTheWeek.put("3", "Wednesday");
                    break;
                case "4":
                case "Thursday": cronFormatDayOfTheWeek.put("4", "Thursday");
                    break;
                case "5":
                case "Friday": cronFormatDayOfTheWeek.put("5", "Friday");
                    break;
                case "6":
                case "Saturday": cronFormatDayOfTheWeek.put("6", "Saturday");
                    break;
                default:
                    throw new InvalidInputException((dayOfWeek + " is not a one of the ligal days in a week"));
            } 
        }
        
        return cronFormatDayOfTheWeek;
    }
    
    @Override
    public String getSpec()
    {
        String str;
        str =  "#" + description + "\n" + minute + " " + hour + " * * ";
        Iterator<String> it = daysOfTheWeek.keySet().iterator();
        
        while (it.hasNext())
        {
            str += it.next();
            if (it.hasNext() == true)
            {
                str += ",";
            }
        }
        
        return str;
    }
    
    @Override
    public String toString()
    {
        //return this.description + ": Weekly on " + hour + ":" + minute;
        String str = this.description.substring(1) + ": Weekly on ";
        Iterator<String> it = daysOfTheWeek.keySet().iterator();
        
        while (it.hasNext())
        {
            str += daysOfTheWeek.get(it.next());
            if (it.hasNext() == true)
            {
                str += ",";
            }
        }
        
        return str + " " + hour + ":" + minute;
    }
}
