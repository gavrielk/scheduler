package org.jenkinsci.plugins.scheduled;

import org.jenkinsci.plugins.scheduled.exceptions.InvalidInputException;
import antlr.ANTLRException;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.security.Permission;
import hudson.triggers.TimerTrigger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author gabi
 */
public class ScheduledAction implements Action{
    
    private final AbstractProject<?, ?> project;
    
    public ScheduledAction() 
    {
        project = null;
    }
    
    public ScheduledAction(AbstractProject<?, ?> project) 
    {
    	this.project = project;
    }
    
    public String getJobName()
    {
        return project.getName();
    }
    
    public ArrayList<String> getActiveSchedules()
    {
        ArrayList<Scheduled> schedules;
        ArrayList<String> schedulesDescription = new ArrayList<String>();
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);
        if (timerTrigger != null)
        {
            try
            {
                schedules = parseSpec(timerTrigger.getSpec());
                Iterator<Scheduled> it = schedules.iterator();
                while (it.hasNext())
                {
                    String currentScheduleDescription = it.next().toString();
                    schedulesDescription.add(currentScheduleDescription);
                }
            }catch(InvalidInputException ex){
                    Logger.getLogger(ScheduledAction.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Error while parsing spec file");
            }
        }
        
        return schedulesDescription;
    }

    @Override
    public String getIconFileName() {
        if (CheckBuildPermissions() == true){
            return "/plugin/scheduler/schedule-icon-add.png";
        }
        else{
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        if (CheckBuildPermissions() == true){
            return "Scheduler";
        }
        else{
            return null;
        }
    }

    @Override
    public String getUrlName() {
        if (CheckBuildPermissions() == true){
            return "scheduler";
        }
        else{
            return null;
        }
    }
    
    // Set a new Corntab entry using TimerTrigger class
    public void doSubmitSchedule(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException 
    {
        try
        {
            Scheduled schedule = generateScheduleClassFromRequest(req);

            addEntryToSpec(schedule.getSpec());

        } catch(InvalidInputException ex){
            Logger.getLogger(ScheduledAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getReferer());
    }
    
    public void doRemoveSchedule(StaplerRequest req, StaplerResponse rsp) throws IOException    
    {
        // the schedules as they appear to the user are in toString() format which is more
        // informative but not the same as it appears in the cron spec, we will extract the
        // schedule description (which is the content that appears until the ":") and search for
        // objects with the same description
        String userChoice = req.getParameter("existingSchedules");
        int indexOfColon = userChoice.indexOf(':');
        String scheduleName = userChoice.substring(0, indexOfColon);
        
        try 
        {
            removeEntryFromSpec(scheduleName);
        } catch (InvalidInputException ex) {
            Logger.getLogger(ScheduledAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getReferer());
    }
    
    /**
     *
     * @param newEntry should be in the following format: 
     * #Name\n* * * * *
     * "*" might be replaced by the desired value
     */
    private void addEntryToSpec(String newEntry)
    {
        String prevSpec, newSpec;
        
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);

        if (timerTrigger != null && timerTrigger.getSpec().equals("") == false)
        {
            prevSpec = timerTrigger.getSpec();
            newSpec = prevSpec + "\n" + newEntry;
        }
        else
        {
            System.out.println("No previous TimerTrigger");
            newSpec = newEntry;
        }

        updateSpec(timerTrigger, newSpec);
    }
    
    private void removeEntryFromSpec(String name) throws InvalidInputException
    {
        String spec = "";
        String[] specArr;
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);

        if (timerTrigger != null)
        {
            specArr = timerTrigger.getSpec().split("\n");
            
            for (int i = 0; i < specArr.length; i+=2)
            {
                // we extract the specArray back to spec except the description and the following
                // cron entry which coresponds with the input name
                if (specArr[i].startsWith("#" + name) == false)
                {
                    spec += specArr[i] + "\n" + specArr[i+1] + "\n";
                }
            }
        }
        else
        {
            throw new InvalidInputException("timerTrigger is empty can't remove " + name);
        }
        
        updateSpec(timerTrigger, spec);
    }
    
    private void updateSpec(TimerTrigger timerTrigger, String newSpec)
    {
        try 
        {
            if (timerTrigger != null)
            {
                timerTrigger.stop();
                project.removeTrigger(timerTrigger.getDescriptor());
            }

            timerTrigger = new TimerTrigger(newSpec);
            timerTrigger.start(project, true);
            project.addTrigger(timerTrigger);
        } catch (ANTLRException | IOException ex) {
            Logger.getLogger(ScheduledAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean CheckBuildPermissions(){
        for ( Permission permission : Permission.getAll())
        {
            if (permission.name.equals("Build") == true)
            {
                if (Jenkins.getInstance().hasPermission(permission) == true)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Scheduled generateScheduleClassFromRequest(StaplerRequest req) throws InvalidInputException 
    {
        String name = req.getParameter("scheduleName");
        String hour = req.getParameter("ScheduleTime-hour");
        String minute = req.getParameter("ScheduleTime-minute");
        String meridiem = req.getParameter("ScheduleTime-meridiem");
        String type = req.getParameter("type");
        ArrayList<String> daysOfWeek = new ArrayList<>();
        String dateOfMonth;
        
        Scheduled schedule;
        
        switch (type) {
            case "Daily":
                schedule = new Scheduled(minute, hour, meridiem, name);
                break;
            case "Weekly":
                if (req.getParameter("Sunday") != null)
                    daysOfWeek.add("Sunday");
                if (req.getParameter("Monday") != null)
                    daysOfWeek.add("Monday");
                if (req.getParameter("Tuesday") != null)
                    daysOfWeek.add("Tuesday");
                if (req.getParameter("Wednesday") != null)
                    daysOfWeek.add("Wednesday");
                if (req.getParameter("Thursday") != null)
                    daysOfWeek.add("Thursday");
                if (req.getParameter("Friday") != null)
                    daysOfWeek.add("Friday");
                if (req.getParameter("Saturday") != null)
                    daysOfWeek.add("Saturday");
                schedule = new WeeklyScheduled(minute, hour, meridiem, daysOfWeek, name);
                break;
            case "Monthly":
                dateOfMonth = req.getParameter("DateOfMonth");
                schedule = new MonthlyScheduled(minute, hour, meridiem, dateOfMonth, name);
                break;
            default: 
                throw new InvalidInputException(type + " is an unrecognized schedule type, must be Daily, Weekly or Monthly");
        }
        
        return schedule;
    }
    
    public String getSchdules()
    {
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);
        if(timerTrigger == null)
        {
            return "";
        }
        String spec = timerTrigger.getSpec();
        
        return spec;
    }
    
    private ArrayList<Scheduled> parseSpec(String spec) throws InvalidInputException
    {
        String[] specArr = spec.split("\n");
        ArrayList<Scheduled> schedules = new ArrayList<Scheduled>();
        
        for (int i = 0; i < specArr.length; i+=2)
        {
            if (specArr[i].startsWith("#"))
            {
                String description = specArr[i];                                //description init
                String[] cronEntry = specArr[i+1].split(" ");
                
                // Initializing Schduled parameters
                String minute = cronEntry[0];
                StringBuilder hour = new StringBuilder();
                StringBuilder  meridiem = new StringBuilder();                  //minute init
                Scheduled.getHourAndMerdidiem(cronEntry[1], hour, meridiem);    //hour & meridiem init
             
                if(cronEntry[4].equals("*") == false)
                {
                    String[] daysArr = cronEntry[4].split(",");                 //days init                    

                    schedules.add(new WeeklyScheduled(minute, hour.toString(), meridiem.toString(), daysArr, description));
                }
                else if(cronEntry[2].equals("*") == false)
                {
                    String dayOfMonth = cronEntry[2];
                    
                    schedules.add(new MonthlyScheduled(minute, hour.toString(), meridiem.toString(), dayOfMonth, description));
                }
                else
                {
                    schedules.add(new Scheduled(minute, hour.toString(), meridiem.toString(), description));
                }
            }
        }
        return schedules;
    }
}
