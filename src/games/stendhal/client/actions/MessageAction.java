/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.actions;

import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TELL;
import static games.stendhal.common.constants.Actions.TEXT;
import static games.stendhal.common.constants.Actions.TYPE;

import games.stendhal.client.ClientSingletonRepository;
import marauroa.common.game.RPAction;

/**
 * Send a message to a player.
 */
class MessageAction implements SlashAction {

	private String lastPlayerTell;

	/**
	 * Execute a chat command.
	 *
	 * TODO:
	 * - rename to "TellAction" to match server
	 *
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 *
	 * @return <code>true</code> if command was handled.
	 */
	@Override
	public boolean execute(final String[] params, final String remainder) {
		lastPlayerTell = params[0];

		if (!remainder.isEmpty()) {
			RPAction tell = new RPAction();

			tell.put(TYPE, TELL);
			tell.put(TARGET, lastPlayerTell);
			tell.put(TEXT, remainder);

			ClientSingletonRepository.getClientFramework().send(tell);
			return true;
		}

		return false;
	}

	/**
	 * Get the maximum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMaximumParameters() {
		return 1;
	}

	/**
	 * Get the minimum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMinimumParameters() {
		return 1;
	}

	/**
	 * Gets the last player we have sent something using /tell.
	 *
	 * @return player name
	 */
	String getLastPlayerTell() {
		return lastPlayerTell;
	}
}
