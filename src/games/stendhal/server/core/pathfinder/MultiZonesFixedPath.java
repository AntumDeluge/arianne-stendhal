/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.pathfinder;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.GuidedEntity;
import games.stendhal.server.entity.Registrator;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import marauroa.common.Pair;

/**
 * class for NPC's multi-zones traveling
 * @author yoriy
 */
public class MultiZonesFixedPath implements Observer {
		private final GuidedEntity ent;
		private final List<Pair<StendhalRPZone, List<Node>>> route;
		private Integer count;
		private StendhalRPZone zone;
		private final Registrator finishnotifier = new Registrator();
	
	/**
	 * constructor
	 * @param entity - pathnotifier owner
	 */
	public MultiZonesFixedPath(
			final GuidedEntity entity, 
			final List<Pair<StendhalRPZone, List<Node>>> rt, 
			final Observer o) {
		ent=entity;
		count=0;
		route=rt;
		finishnotifier.setObserver(o);
	}
	
	/**
	 *  remove npc from his zone
	 */
	private void removeFromZone() {
		ent.getZone().remove(ent);		
	}
	
	/**
	 *  add npc to next zone in list
	 */
	private void addToZone() {
		int x= route.get(count).second().get(0).getX();
		int y= route.get(count).second().get(0).getY();
		ent.setPosition(x, y);
		zone = route.get(count).first();
		ent.setPath(new FixedPath(route.get(count).second(), false));	
		zone.add(ent);
	}
	
	public void update(Observable o, Object arg) {
		// will run at local path's end; have to change path to another
		if(count!=(route.size()-1)) {
			removeFromZone();
			++count;
			addToZone();
		} else {
			// last route finished
			finishnotifier.setChanges();
			finishnotifier.notifyObservers();
		}
	}	
}

