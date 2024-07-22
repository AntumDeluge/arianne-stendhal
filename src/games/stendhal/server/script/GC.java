/* $Id$ */
/***************************************************************************
 *                 Copyright © 2007-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.List;

import games.stendhal.server.core.scripting.impl.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Runs the garbage collector manually (for memory profiling).
 *
 * @author hendrik
 */
public class GC extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		System.gc();
	}
}
