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
package games.stendhal.server.entity.npc.action;

import java.util.HashMap;
import java.util.Map;

import games.stendhal.common.Grammar;
import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.StringUtils;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

/**
 * States the name of the item, with formatting/grammar rules, stored in the quest slot
 */
public class SayRequiredItemAction implements ChatAction {
	private static Logger logger = Logger.getLogger(DropRecordedItemAction.class);

	private final String questname;
	private final String message;
	private final int index;

	/**
	 * Creates a new SayRequiredItemAction.
	 * 
	 * @param questname
	 *            name of quest-slot to check
	 * @param index
	 *            position of the itemname,amount within the quest slot 'array'
	 * @param message
	 *            message to come before statement of item name to collect
	 *            
	 */
	public SayRequiredItemAction(final String questname, final int index, final String message) {
		this.questname = questname;
		this.index = index;
		this.message = message;
	} 
	/**
	 * Creates a new SayRequiredItemAction.
	 * 
	 * @param questname
	 *            name of quest-slot to check
	 * @param message
	 *            message to come before statement of item name to collect
	 *            
	 */

	public SayRequiredItemAction(final String questname, final String message) {
		this.questname = questname;
		this.message = message;
		this.index = -1;
	}

	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		if (!player.hasQuest(questname)) {
			logger.error(player.getName() + " does not have quest " + questname);
			return;
		} else {
			final String questSubString = player.getQuest(questname, index); 
			final String[] elements = questSubString.split("=");
			String itemname = elements[0];
			int amount = 1;
			
			// some older quests may have stored an item name but not the amount
			// so we use the initial value of 1 if the string can't be split
			if(elements.length > 1) {
				amount=MathHelper.parseIntDefault(elements[1], 1);
			}

			Map<String, String> substitutes = new HashMap<String, String>();
			substitutes.put("item", Grammar.quantityplnoun(amount, itemname, "a"));
			substitutes.put("#item", Grammar.quantityplnounWithHash(amount, itemname));
			substitutes.put("the item", "the " + Grammar.plnoun(amount, itemname));
			
			
			raiser.say(StringUtils.substitute(message,substitutes));		
		}
	}

	@Override
	public String toString() {
		return "SayRequiredItemAction <" + questname +  "\"," + index + ",\"" + message + ">";
	}
	

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false,
				SayRequiredItemAction.class);
	}

	

}
