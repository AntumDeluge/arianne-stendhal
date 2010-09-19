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
package games.stendhal.server.entity.spell.effect;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

public class DamageEffect extends AbstractEffect {

	public DamageEffect(Nature nature, int amount, int atk, int def,
			double lifesteal, int rate, int regen) {
		super(nature, amount, atk, def, lifesteal, rate, regen);
	}

	public void act(Player caster, Entity target) {
		actInternal(caster, (RPEntity) target);
	}
	
	private void actInternal(Player caster, RPEntity target) {
		
	}

}
