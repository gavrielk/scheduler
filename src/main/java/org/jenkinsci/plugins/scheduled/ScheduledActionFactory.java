/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.scheduled;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author gabi
 */
@Extension
public class ScheduledActionFactory extends TransientProjectActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractProject target) {
        ArrayList<Action> actions = new ArrayList<Action>();
        
        ScheduledAction newAction = new ScheduledAction(target);
        actions.add(newAction);
        
        return actions;
    }
    
}
