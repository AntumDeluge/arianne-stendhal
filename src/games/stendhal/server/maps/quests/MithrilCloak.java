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
package games.stendhal.server.maps.quests;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.item.scroll.TwilightMossScroll;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.mithrilcloak.MithrilCloakQuestChain;

/**
 * QUEST: Mithril Cloak
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li>Ida, a seamstress in Ados.</li>
 * <li>Imperial scientists, in kalavan basement</li>
 * <li>Mithrilbourgh wizards, in kirdneh and magic city</li>
 * <li>Hogart, a retired master dwarf smith, forgotten below the dwarf mines in
 * Orril.</li>
 * <li>Terry, the dragon hatcher in semos caves.</li>
 * <li>Ritati Dragontracker, odds and ends buyer in ados abandoned keep</li>
 * <li>Pdiddi, the dodgy dealer from Semos</li>
 * <li>Josephine, young woman from Fado</li>
 * <li>Pedinghaus, the mithril casting wizard in Ados</li>
 * </ul>
 * <p>
 * STEPS:
 * <ul>
 * <li>Ida needs sewing machine fixed, with one of three items from a list</li>
 * <li>Once machine fixed and if you have done mithril shield quest, Ida offers you cloak</li>
 * <li>Kampusch tells you to how to make the fabric</li>
 * <li>Imperial scientists take silk glands and make silk thread</li>
 * <li>Kampusch fuses mithril nuggets into the silk thread</li>
 * <li>Whiggins weaves mithril thread into mithril fabric</li>
 * <li>Ida takes fabric then asks for scissors</li>
 * <li>Hogart makes the scissors which need eggshells</li>
 * <li>Terry swaps eggshells for poisons</li>
 * <li>Ida takes the scissors then asks for needles</li>
 * <li>Needles come from Ritati Dragontracker</li>
 * <li>Ida breaks a random number of needles, meaning you need to get more each time</li>
 * <li>Ida pricks her finger on the last needle and goes to twilight zone</li>
 * <li>Pdiddi sells the moss to get to twilight zone</li> 
 * <li>A creature in the twilight zone drops the elixir to heal lda</li>
 * <li>After being ill Ida asks you to take a blue striped cloak to Josephine</li>
 * <li>After taking cloak to Josephine and telling Ida she asks for mithril clasp</li>
 * <li>Pedinghaus makes mithril clasp</li>
 * <li>The clasp completes the cloak</li>
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li>Mithril Cloak</li>
 * <li> XP</li>
 * <li> Karma</li>
 * </ul>
 * <p>
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 * 
 * @author kymara
 */
public class MithrilCloak extends AbstractQuest {
	private static final String QUEST_SLOT = "mithril_cloak";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Mithril Cloak",
				"Do you want to have one of the best armors which currently is on the market? It is shiny and strong and you can be someone with the power to bring Ida the things she needs for it.",
				false);
		
		// login notifier to teleport away players logging into the twilight zone.
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			public void onLoggedIn(final Player player) {
				TwilightMossScroll scroll = (TwilightMossScroll) SingletonRepository.getEntityManager().getItem("twilight moss");
				scroll.teleportBack(player);
			}

		});
		
		MithrilCloakQuestChain mithrilcloak = new MithrilCloakQuestChain();
		mithrilcloak.addToWorld();
	}




	@Override
	public String getName() {
		return "MithrilCloak";
	}
	
	// it's a long quest so they can always start it before they can necessarily finish all
	@Override
	public int getMinLevel() {
		return 100;
	}
}
