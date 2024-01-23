/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { ButtonBase } from "./ButtonBase";
import { ChatButton } from "./ChatButton";
import { JoystickButton } from "./JoystickButton";
import { LogButton } from "./LogButton";
import { MenuButton } from "./MenuButton";
import { RotateLButton } from "./RotateLButton";
import { RotateRButton } from "./RotateRButton";
import { SettingsButton } from "./SettingsButton";
import { SoundButton } from "./SoundButton";

import { Paths } from "../../data/Paths";


/**
 * Main button to show/hide quick menu buttons.
 */
export class QMButton {

	private static initialized = false;
	private static expanded = false;
	private static readonly buttonListX: ButtonBase[] = [];
	private static readonly buttonListY: ButtonBase[] = [];


	private constructor() {
		// do nothing
	}

	public static init() {
		if (QMButton.initialized) {
			console.warn("WARNING: attempted to re-initialize quick menu buttons")
			return;
		}
		QMButton.initialized = true;

		const btn_main = document.getElementById("qm-main")! as HTMLImageElement;
		// ensure main button is visible at startup
		btn_main.style["display"] = "block";
		btn_main.style["cursor"] = "url(" + Paths.sprites + "/cursor/highlight.png) 1 3, auto";
		btn_main.draggable = false;

		// horizontal sub-buttons
		QMButton.buttonListX.push(new MenuButton());
		QMButton.buttonListX.push(new SettingsButton());
		QMButton.buttonListX.push(new LogButton());
		QMButton.buttonListX.push(new ChatButton());
		QMButton.buttonListX.push(new SoundButton());
		QMButton.buttonListX.push(new JoystickButton());
		// vertical sub-buttons
		QMButton.buttonListY.push(new RotateLButton());
		QMButton.buttonListY.push(new RotateRButton());

		btn_main.onload = () => {
			// remove listener
			btn_main.onload = null;
			this.refresh();
		}
		btn_main.src = Paths.gui + "/quickmenu/main.png";

		btn_main.addEventListener("click", function(e) {
			QMButton.toggle();
		});

		// hide buttons by default
		QMButton.update();
	}

	/**
	 * Updates button positioning.
	 */
	public static refresh() {
		// place buttons in upper-right corner of viewport
		const btn_main = document.getElementById("qm-main")! as HTMLImageElement;
		const rect = document.getElementById("gamewindow")!.getBoundingClientRect();

		let drawLeft = rect.right - btn_main.width;
		let drawTop = rect.top;
		// main button
		btn_main.style["left"] = drawLeft + "px";
		btn_main.style["top"] = drawTop + "px";

		// horizontal buttons
		for (const btn of QMButton.buttonListX) {
			// all buttons should be same size
			drawLeft -= btn_main.width;
			btn.setPos(drawLeft, drawTop);
		}

		// vertical buttons
		drawLeft = rect.right - btn_main.width
		for (const btn of QMButton.buttonListY) {
			// all buttons should be same size
			drawTop += btn_main.height;
			btn.setPos(drawLeft, drawTop);
		}
	}

	private static toggle() {
		QMButton.expanded = !QMButton.expanded;
		QMButton.update();
	}

	private static update() {
		(document.getElementById("qm-main")! as HTMLImageElement).style["transform"] = "rotate("
				+ (QMButton.expanded ? 90 : 0) + "deg)";
		for (const btn of [...QMButton.buttonListX, ...QMButton.buttonListY]) {
			btn.setVisible(QMButton.expanded);
		}
	}
}