/* $Id: RPScheduler.java,v 1.4 2007/02/04 13:10:42 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.rp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

import org.apache.log4j.Logger;

/**
 * This class represents a scheduler to deliver action by turns, so every action
 * added to the scheduler is executed on the next turn. Each object can cast as
 * many actions as it wants.
 */
public class RPScheduler {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(RPScheduler.class);

	/** a HashMap<RPObject,RPActionList> of entries for this turn */
	private Map<RPObject, List<RPAction>> actualTurn;

	/** a HashMap<RPObject,RPActionList> of entries for next turn */
	private Map<RPObject, List<RPAction>> nextTurn;

	/** Turn we are executing now */
	private int turn;

	/** Constructor */
	public RPScheduler() {
		turn = 0;
		actualTurn = new HashMap<RPObject, List<RPAction>>();
		nextTurn = new HashMap<RPObject, List<RPAction>>();
	}

	/**
	 * Add an RPAction to the scheduler for the next turn
	 * @param object 
	 * 
	 * @param action the RPAction to add.
	 */
	public synchronized boolean addRPAction(RPObject object, RPAction action,IRPRuleProcessor ruleProcessor) {
		try {
			List<RPAction> list=nextTurn.get(object);
			
			if(list==null) {
				list=new LinkedList<RPAction>();
				nextTurn.put(object, list);
			}

			if (ruleProcessor.onActionAdd(object, action, list)) {
				list.add(action);
			}
			
			return true;
		} catch (AttributeNotFoundException e) {
			logger.error("cannot add action to RPScheduler, Action(" + action + ") is missing a required attributes", e);
			return false;
		}
	}

	/**
	 * This method clears the actions that may exist in actual turn or the next one for the 
	 * giver object id.
	 * @param id object id to remove actions
	 */
	public synchronized void clearRPActions(RPObject object) {
		nextTurn.remove(object);
		actualTurn.remove(object);
	}

	/**
	 * For each action in the actual turn, make it to be run in the
	 * ruleProcessor Depending on the result the action needs to be added for
	 * next turn.
	 */
	public synchronized void visit(IRPRuleProcessor ruleProcessor) {
		for (Map.Entry<RPObject, List<RPAction>> entry : actualTurn.entrySet()) {
			RPObject object = entry.getKey();
			List<RPAction> list = entry.getValue();

			for (RPAction action : list) {
				try {
					ruleProcessor.execute(object,action);
				} catch (Exception e) {
					logger.error("error in visit()", e);
				}
			}
		}
	}

	/**
	 * This method moves to the next turn and deletes all the actions in the
	 * actual turn
	 */
	public synchronized void nextTurn() {
		++turn;

		/* we cross-exchange the two turns and erase the contents of the next turn */
		Map<RPObject, List<RPAction>> tmp = actualTurn;
		actualTurn = nextTurn;
		nextTurn = tmp;
		nextTurn.clear();
	}
}