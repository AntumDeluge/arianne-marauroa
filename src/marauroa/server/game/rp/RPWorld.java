/* $Id: RPWorld.java,v 1.3 2007/02/04 13:37:07 arianne_rpg Exp $ */
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
import java.util.Iterator;

import marauroa.common.Log4J;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.common.game.RPSlot;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;

import org.apache.log4j.Logger;

public class RPWorld implements Iterable<IRPZone> {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(RPWorld.class);

	/** The Singleton instance */
	private static RPWorld instance;

	HashMap<IRPZone.ID, IRPZone> zones;

	PlayerEntryContainer playerContainer;

	protected RPWorld() {
		zones = new HashMap<IRPZone.ID, IRPZone>();
		try {
			playerContainer=PlayerEntryContainer.getContainer();
		} catch (Exception e) {
			/* This should never happen... but... */
			logger.warn(e);
		}
	}
	
	public static RPWorld get() {
		if (instance == null) {
			instance = new RPWorld();
		}
		return instance;
	}

	/** This method is called when RPWorld is created */
	public void onInit() {
	}

	/** This method is called when server is going to shutdown. */
	public void onFinish() {
	}

	/** Adds a new zone to World */
	public void addRPZone(IRPZone zone) {
		zones.put(zone.getID(), zone);
	}

	/** Returns true if world has such zone */
	public boolean hasRPZone(IRPZone.ID zoneid) {
		return zones.containsKey(zoneid);
	}

	/** Returns the zone or null if it doesn't exists */
	public IRPZone getRPZone(IRPZone.ID zoneid) {
		return zones.get(zoneid);
	}

	/** Returns the zone or null if it doesn't exists */
	public IRPZone getRPZone(RPObject.ID objectid) {
		return zones.get(new IRPZone.ID(objectid.getZoneID()));
	}

	public void add(RPObject object) throws RPObjectInvalidException {
		if (object.has("zoneid")) {
			IRPZone zone = zones.get(new IRPZone.ID(object.get("zoneid")));
			zone.assignRPObjectID(object);

			// Changing too the objects inside the slots
			Iterator<RPSlot> it = object.slotsIterator();
			while (it.hasNext()) {
				RPSlot slot = it.next();
				String zoneid = object.get("zoneid");

				for (RPObject item : slot) {
					item.put("zoneid", zoneid);
					zone.assignRPObjectID(item);
				}
			}

			zone.add(object);

			/** NOTE: Document this hack */
			if (object.has("clientid")) {
				PlayerEntry entry=playerContainer.get(object.getInt("clientid"));
				entry.requestedSync=true;
			}
		}
	}

	public RPObject get(RPObject.ID id) throws RPObjectInvalidException {
		IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
		return zone.get(id);
	}

	public boolean has(RPObject.ID id) throws RPObjectInvalidException {
			IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
			return zone.has(id);
	}

	public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException {
			IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
			return zone.remove(id);
	}

	public Iterator<IRPZone> iterator() {
		return zones.values().iterator();
	}

	public void modify(RPObject object) {
		IRPZone zone = zones.get(new IRPZone.ID(object.get("zoneid")));
		zone.modify(object);
	}

	public void changeZone(IRPZone.ID oldzoneid, IRPZone.ID newzoneid, RPObject object) throws RPObjectInvalidException {
		Log4J.startMethod(logger, "changeZone");
		try {
			if (newzoneid.equals(oldzoneid)) {
				return;
			}

			// newzone is never used?
			// Yes, because RPWorld.add seems to be more than RPZone.add and that is also plainly bad.
			// BUG: RPWorld.add has more than it says.			
			// IRPZone newzone=getRPZone(newzoneid);
			IRPZone oldzone = getRPZone(oldzoneid);

			oldzone.remove(object.getID());

			object.put("zoneid", newzoneid.getID());

			add(object);
		} catch (Exception e) {
			logger.error("error changing Zone", e);
			throw new RPObjectInvalidException("zoneid");
		} finally {
			Log4J.finishMethod(logger, "changeZone");
		}
	}

	public void changeZone(String oldzone, String newzone, RPObject object) throws RPObjectInvalidException {
		changeZone(new IRPZone.ID(oldzone), new IRPZone.ID(newzone), object);
	}

	public void nextTurn() {
		Log4J.startMethod(logger, "nextTurn");
		for (IRPZone zone : zones.values()) {
			zone.nextTurn();
		}

		Log4J.finishMethod(logger, "nextTurn");
	}

	public int size() {
		int size = 0;

		for (IRPZone zone : zones.values()) {
			size += zone.size();
		}

		return size;
	}
}