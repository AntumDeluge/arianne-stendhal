/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { RPEntity } from "./RPEntity";

import { EntityOverlayRegistry } from "../data/EntityOverlayRegistry";

import { Color } from "../data/color/Color";

import { SkillEffect } from "../sprite/action/SkillEffect";

declare var marauroa: any;
declare var stendhal: any;

export class Creature extends RPEntity {

	override minimapStyle = Color.CREATURE;
	override spritePath = "monsters";
	override titleStyle = "#ffc8c8";


	override set(key: string, value: string) {
		super.set(key, value);
		if (key === "name") {
			// overlay animation
			this.overlay = EntityOverlayRegistry.get("creature", this);
		} else if (key === "subclass") {
			this.updateSprite();
		}
	}

	override unset(key: string) {
		super.unset(key);
		if (key === "subclass") {
			this.updateSprite();
		}
	}

	override updateSprite() {
		if (typeof(this["outfit_ext"]) !== "undefined" || typeof(this["outfit"]) !== "undefined") {
			super.updateSprite();
		} else if (typeof(this["class"]) !== "undefined" && typeof(this["subclass"]) !== "undefined") {
			let filename = stendhal.paths.sprites + "/" + this.spritePath + "/" + this["class"] + "/"
					+ this["subclass"];
			// check for safe image
			if (!stendhal.config.getBoolean("effect.blood")
					&& stendhal.data.sprites.hasSafeImage(filename)) {
				filename = filename + "-safe.png";
			} else {
				filename = filename + ".png";
			}
			this.sprite = stendhal.data.sprites.get(filename);
		} else {
			this.sprite = undefined;
		}
	}

	override onclick(_x: number, _y: number) {
		var action = {
				"type": "attack",
				"target": "#" + this["id"]
			};
		marauroa.clientFramework.sendAction(action);
	}

	// Overrides the one in creature
	override say(text: string) {
		if (stendhal.config.getBoolean("speech.creature")) {
			this.addSpeechBubble(text);
		}
	}

	override getCursor(_x: number, _y: number) {
		return "url(" + stendhal.paths.sprites + "/cursor/attack.png) 1 3, auto";
	}

	/**
	 * Shows a temporary animation overlay for certain entities.
	 *
	 * FIXME: does not restore previous overlay
	 */
	protected override onTransformed() {
		if (!this["name"].startsWith("vampire")) {
			return;
		}
		const delay = 100;
		const frames = 5;
		this.overlay = new SkillEffect("transform", delay, delay * frames);
	}
}
