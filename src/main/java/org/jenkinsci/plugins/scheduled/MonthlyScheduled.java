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
public class MonthlyScheduled extends Scheduled
{
    
    String dayOfMonth;
    
    public MonthlyScheduled(String minute, String hour, String meridiem, String dayOfMonth, String description) throws InvalidInputException
    {
        super(minute, hour, meridiem, description);
        validateInput(dayOfMonth);
        this.dayOfMonth = dayOfMonth;
    }

    private void validateInput(String dayOfMonth) throws InvalidInputException
    {
        int dayOfMonthInt = Integer.valueOf(dayOfMonth);
        if (dayOfMonthInt < 1 || dayOfMonthInt > 28)
        {
            throw new InvalidInputException("Day of month must be between 1 and 28");
        }
    }
    
    @Override
    public String getSpec()
    {
        return "#" + description + "\n" + minute + " " + hour + " " + dayOfMonth + " * *";
    }
    
    @Override
    public String toString()
    {
        return this.description.substring(1) + ": Monthly on " + dayOfMonth + "at " + hour + ":" + minute;
    }
}
