/***************************************************************************
 *                 Copyright © 2003-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var marauroa: any;
declare var stendhal: any;

import { ui } from "./UI";
import { singletons } from "../SingletonRepo";


export interface Sound extends HTMLAudioElement {
	basevolume: number;
	radius: number;
	x: number;
	y: number;
	basename: string;
}

export class SoundManager {

	/** Layer names & ordering. */
	readonly layers: string[];
	/** Session cache. */
	private cacheGlobal: {[source: string]: HTMLAudioElement};
	/** Cache for current map. */
	private cache: {[source: string]: HTMLAudioElement};
	/** Actively playing sounds. */
	private active: {[layer: string]: Sound[]};
	/*
	private active: {[layer: string]: Sound[]} = {
		["music"]: [],
		["ambient"]: [],
		["creature"]: [],
		["sfx"]: [],
		["gui"]: []
	};
	*/

	/** Music instance played globally. */
	private globalMusic?: Sound;

	/** Singleton instance. */
	private static instance: SoundManager;


	/**
	 * Retrieves singleton instance.
	 */
	static get(): SoundManager {
		if (!SoundManager.instance) {
			SoundManager.instance = new SoundManager();
		}
		return SoundManager.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		this.layers = ["music", "ambient", "creature", "sfx", "gui"];
		this.cacheGlobal = {};
		this.cache = {};
		this.active = {};
		for (const layerName of this.layers) {
			this.active[layerName] = [];
		}
	}

	/**
	 * Retrieves active sounds.
	 *
	 * @param includeGui {boolean}
	 *   Will include sounds from the gui layer (default: false).
	 * @return {Sound[]}
	 *   Array of active sounds.
	 */
	getActive(includeGui=false): Sound[] {
		const active: Sound[] = [];
		for (const layerName of this.layers) {
			if (layerName === "gui" && !includeGui) {
				continue;
			}
			for (const snd of this.active[layerName]) {
				active.push(snd);
			}
		}
		return active;
	}

	/**
	 * Retrieves a single active sound.
	 *
	 * @param soundName {string}
	 *   Basename of sound file.
	 * @param includeGui {boolean}
	 *   Will include sounds from the gui layer (default: false).
	 * @return {ui.SoundManager.Sound}
	 *   Active sound instance or `undefined`.
	 */
	getActiveByName(soundName: string, includeGui=false): Sound|undefined {
		for (const sound of this.getActive(includeGui)) {
			if (soundName === sound.basename) {
				return sound;
			}
		}
	}

	/**
	 * Initializes an audio object & loads into the cache.
	 *
	 * @param id
	 *     Identifier string used to retrieve from cache.
	 * @param filename
	 *     Path to sound file.
	 * @param global
	 *     Store in session cache instead of map.
	 * @return
	 *     New audio object.
	 */
	private load(id: string, filename: string, global=false): HTMLAudioElement {
		const snd = new Audio(filename);
		snd.autoplay = false;
		// load into cache
		if (global) {
			// globally cached sounds are not removed on map change
			this.cacheGlobal[id] = snd;
		} else {
			this.cache[id] = snd;
		}
		return snd;
	}

	/**
	 * Retrieves list of valid layer string identifiers.
	 */
	getLayerNames(): string[] {
		return this.layers;
	}

	/**
	 * Checks for valid layer.
	 *
	 * @param layer {any}
	 *   Layer name or index.
	 * @return {string}
	 *   Layer name or `undefined`.
	 */
	checkLayer(layer: any): string {
		let layerIndex = -1;
		const ltype = typeof(layer);
		if (ltype === "number") {
			layerIndex = layer;
		} else if (ltype === "string") {
			if (this.layers.indexOf(layer) > -1) {
				return layer;
			}
			if (!Number.isNaN(layer)) {
				layerIndex = parseInt(layer, 10);
			}
		}
		const layerName = this.layers[layerIndex];
		if (!layerName) {
			throw new Error("invalid layer: " + layer);
		}
		return layerName;
	}

	/**
	 * Retrieves the string identifier of the associated layer index.
	 *
	 * @deprecated
	 */
	getLayerName(layer: number): string {
		// DEBUG:
		console.log("getLayerName: " + layer);

		console.warn("SoundManager.getLayerName is deprecated");
		// default to GUI
		let layername = "gui";
		if (layer >= 0 && layer < this.layers.length) {
			layername = this.layers[layer];
		} else {
			console.warn("unknown layer index: " + layer);
		}
		return layername;
	}

	/**
	 * Retrieves array index value of layer name.
	 *
	 * @param layerName {string}
	 *   Name of layer or index representation as string.
	 * @return {number}
	 *   Index or -1 if not found.
	 * @deprecated
	 */
	getLayerIndex(layerName: string): number {
		// DEBUG:
		console.log("layer name: " + layerName + " (" + typeof(layerName) + ", NaN: " + Number.isNaN(layerName) + ")");

		console.warn("SoundManager.getLayerIndex is deprecated");
		if (!Number.isNaN(layerName)) {
			return parseInt(layerName, 10);
		}
		return this.layers.indexOf(layerName);
	}

	/**
	 * Sets event handlers for when sound finishes.
	 *
	 * @param layername
	 *     Name of layer sound will play on.
	 * @param sound
	 *     The playing sound.
	 */
	private onSoundAdded(layername: string, sound: Sound) {
		sound.onended = (e) => {
			// remove from active sounds
			const idx = this.active[layername].indexOf(sound);
			if (idx > -1) {
				this.active[layername].splice(idx, 1);
			}
		};
		this.active[layername].push(sound);
	}

	/**
	 * Plays a sound.
	 *
	 * @param soundname {string}
	 *   Sound file basename.
	 * @param layername {string}
	 *   Name of layer sound will play on.
	 * @param volume {number}
	 *   Volume level between 0.0 and 1.0.
	 * @param loop {boolean}
	 *   Whether or not sound should be looped.
	 * @return {ui.SoundManager.Sound}
	 *   The new sound instance.
	 */
	private playEffect(soundname: string, layername: string, volume=1.0, loop=false): Sound|undefined {
		const muted = !stendhal.config.getBoolean("sound");
		if (muted && !loop) {
			// don't add non-looping sounds when muted
			return;
		}

		// check volume sanity
		volume = this.normVolume(volume);
		// apply layer volume adjustments
		const actualvolume = this.getAdjustedVolume(layername, volume);

		// check cache first
		let snd = this.cache[soundname] || this.cacheGlobal[soundname];
		if (!snd) {
			// add new sound to cache
			snd = this.load(soundname, stendhal.paths.sounds + "/" + soundname + ".ogg");
		}

		if (!this.cache[soundname]) {
			// add globally cached sounds to map cache
			this.cache[soundname] = snd;
		}

		if (layername === "gui" && !this.cacheGlobal[soundname]) {
			// keep gui sounds in global cache
			this.cacheGlobal[soundname] = snd;
		}

		// create a copy so multiple instances can be played simultaneously
		const scopy = snd.cloneNode() as Sound;
		scopy.autoplay = true;
		scopy.basevolume = volume;
		scopy.volume = Math.min(actualvolume, volume);
		scopy.loop = loop;
		scopy.muted = muted;
		scopy.basename = soundname;

		this.onSoundAdded(layername, scopy);
		return scopy;
	}

	/**
	 * Plays a sound with volume relative to distance.
	 *
	 * @param x
	 *     X coordinate of sound source.
	 * @param y
	 *     Y coordinate of sound source.
	 * @param radius
	 *     Radius at which sound can be heard.
	 * @param layer
	 *     Channel index sound will play on.
	 * @param soundname
	 *     Sound file basename.
	 * @param volume
	 *     Volume level between 0.0 and 1.0.
	 * @param loop
	 *     Whether or not sound should be looped.
	 * @return
	 *     The new sound instance.
	 */
	playLocalizedEffect(x: number, y: number, radius: number, layer: number, soundname: string,
			volume=1.0, loop=false): Sound|undefined {
		//~ const layerName = this.getLayerName(layer);
		const layerName = this.checkLayer(layer);
		//~ if (!layerName) {
			//~ console.error("invalid sound layer:", layer, new Error());
			//~ return;
		//~ }
		const snd = this.playEffect(soundname, layerName, volume, loop);
		if (!snd) {
			return;
		}

		// Further adjustments if the sound has a radius
		if (radius) {
			if (!marauroa.me || !x) {
				// can't calculate distance yet
				snd.volume = 0.0;
			} else {
				this.adjustForDistance(layerName, snd, radius, x, y, marauroa.me["_x"], marauroa.me["_y"]);
			}
		}

		snd.radius = radius;
		snd.x = x;
		snd.y = y;
		return snd;
	}

	/**
	 * Plays a sound with uniform volume.
	 *
	 * @param soundname
	 *     Sound file basename.
	 * @param layer
	 *     Channel index sound will play on.
	 * @param volume
	 *     Volume level between 0.0 and 1.0.
	 * @param loop
	 *     Whether or not sound should be looped.
	 * @return
	 *     The new sound instance.
	 */
	playGlobalizedEffect(soundname: string, layer?: number, volume=1.0, loop=false): any {
		// default to gui layer
		if (typeof(layer) === "undefined") {
			layer = this.getLayerIndex("gui");
		}
		const layerName = this.checkLayer(layer);
		//~ if (!layerName) {
			//~ console.error("invalid sound layer:", layer, new Error());
			//~ return;
		//~ }
		return this.playEffect(soundname, layerName, volume, loop);
	}

	/**
	 * Loops a sound with volume relative to distance.
	 *
	 * @param x
	 *     X coordinate of sound source.
	 * @param y
	 *     Y coordinate of sound source.
	 * @param radius
	 *     Radius at which sound can be heard.
	 * @param layer
	 *     Channel index sound will play on.
	 * @param soundname
	 *     Sound file basename.
	 * @param volume
	 *     Volume level between 0.0 and 1.0.
	 * @return
	 *     The new sound instance.
	 */
	playLocalizedLoop(x: number, y: number, radius: number, layer: number,
			soundname: string, volume=1.0): any {
		return this.playLocalizedEffect(x, y, radius, layer, soundname,
				volume, true);
	}

	/**
	 * Loops a sound with uniform volume.
	 *
	 * @param soundname
	 *     Sound file basename.
	 * @param layer
	 *     Channel index sound will play on.
	 * @param volume
	 *     Volume level between 0.0 and 1.0.
	 * @return
	 *     The new sound instance.
	 */
	playGlobalizedLoop(soundname: string, layer?: number, volume=1.0): any {
		return this.playGlobalizedEffect(soundname, layer, volume, true);
	}

	/**
	 * Loops a sound with volume relative to distance.
	 *
	 * @param x
	 *     X coordinate of sound source.
	 * @param y
	 *     Y coordinate of sound source.
	 * @param radius
	 *     Radius at which sound can be heard.
	 * @param layer
	 *     Channel on which to be played (currently not supported).
	 * @param musicName
	 *     Sound file basename.
	 * @param volume
	 *     Volume level between 0.0 and 1.0.
	 * @return
	 *     The new sound instance.
	 */
	playLocalizedMusic(x: number, y: number, radius: number, layer: number, musicName: string,
			volume=1.0): Sound|undefined {
		// load into cache so playEffect doesn't look in "data/sounds"
		if (!this.cache[musicName]) {
			this.load(musicName,
					stendhal.paths.music + "/" + musicName + ".ogg");
		}
		return this.playLocalizedLoop(x, y, radius, layer, musicName, volume);
	}

	/**
	 * Loops a sound with uniform volume.
	 *
	 * @param musicName
	 *     Sound file basename.
	 * @param volume
	 *     Volume level between 0.0 and 1.0.
	 * @return {ui.SoundManager.Sound}
	 *   The new sound instance or `undefined`.
	 */
	playGlobalizedMusic(musicName: string, volume=1.0): Sound|undefined {
		// load into cache so playEffect doesn't look in "data/sounds"
		if (!this.cache[musicName]) {
			this.load(musicName, stendhal.paths.music + "/" + musicName + ".ogg");
		}
		return this.playGlobalizedLoop(musicName, this.getLayerIndex("music"), volume);
	}

	/**
	 * Loops a sound with uniform volume & stops previous instance.
	 *
	 * @param musicName {string}
	 *   Sound file basename (can be `undefined` or `null`).
	 * @param volume {number}
	 *   Volume level between 0.0 & 1.0.
	 * @return {ui.SoundManager.Sound}
	 *   The new sound instance or `undefined`.
	 */
	playSingleGlobalizedMusic(musicName: string, volume=1.0) {
		// DEBUG:
		console.log("playing global music: " + musicName
				+ "\nold: " + (this.globalMusic ? this.globalMusic.basename : this.globalMusic));

		if (this.globalMusic && musicName === this.globalMusic.basename) {
			// don't stop & restart music when changing maps if the same
			return;
		}
		if (this.globalMusic) {
			this.stop("music", this.globalMusic);
			if (!musicName) {
				// just stop if name not provided
				this.globalMusic = undefined;
				return;
			}
		}
		if (musicName) {
			this.globalMusic = this.playGlobalizedMusic(musicName, volume);
		}
	}

	/**
	 * Stops a sound & removes it from active group.
	 *
	 * @param layer {number|string}
	 *   Channel index or name sound is playing on.
	 * @param sound {ui.SoundManager.Sound|string}
	 *   The sound or name of sound to be stopped.
	 * @return {boolean}
	 *   `true` if succeeded.
	 */
	stop(layer: number|string, sound: Sound|string): boolean {
		// DEBUG:
		const lname = layer;

		if (typeof(layer) === "string") {
			layer = this.getLayerIndex(layer);
		}

		// DEBUG:
		console.log("(SoundManager.stop) layer index: " + layer + " (" + lname + ")");

		if (layer < 0 || layer >= this.layers.length) {
			console.error("cannot stop sound on non-existent layer: " + layer);
			return false;
		}
		let soundName = "unknown";
		if (typeof(sound) === "string") {
			soundName = sound;
			sound = this.getActiveByName(soundName)!;
		}
		if (!sound) {
			console.error("sound \"" + soundName + "\" not playing");
			return false;
		}

		const layerName = this.layers[layer];
		const group = this.active[layerName];
		// use this value to avoid error "Argument of type 'string | Sound' is not assignable to parameter of type 'Sound'"
		const sSound = sound as Sound;
		const idx = group.indexOf(sSound);
		if (sound && idx > -1) {
			sSound.pause();
			sSound.currentTime = 0;
			if (sSound.onended) {
				sSound.onended(new Event("stopsound"));
			}
		}
		return this.active[layerName].indexOf(sSound) < 0;
	}

	/**
	 * Stops all currently playing sounds.
	 *
	 * @param includeGui
	 *     If <code>true</code>, sounds on the gui layer will stopped
	 *     as well.
	 * @return
	 *     <code>true</code> if all sounds were aborted or paused.
	 */
	stopAll(includeGui=false): boolean {
		let stopped = true;
		for (const layerName of this.layers) {
			if (layerName === "gui" && !includeGui) {
				continue;
			}
			const curLayer = this.active[layerName];
			// XXX: just iterating over indexes doesn't remove all sounds. async issue?
			while (curLayer.length > 0) {
				this.stop(layerName, curLayer[0]);
			}
			stopped = stopped && this.active[layerName].length == 0;
		}
		return stopped;
	}

	/**
	 * Mutes all currently playing sounds.
	 *
	 * @return
	 *     <code>true</code> if all sounds were muted.
	 */
	muteAll(): boolean {
		let muted = true;
		for (const layerName of this.layers) {
			for (const snd of this.active[layerName]) {
				snd.muted = true;
				muted = muted && snd.muted;
			}
		}
		return muted;
	}

	/**
	 * Unmutes all currently playing sounds.
	 *
	 * @return
	 *     <code>true</code> if all sounds were unmuted.
	 */
	unmuteAll(): boolean {
		let unmuted = true;
		for (const layerName of this.layers) {
			for (const snd of this.active[layerName]) {
				snd.muted = false;
				unmuted = unmuted && !snd.paused && !snd.muted;
			}
		}

		return unmuted;
	}

	/**
	 * Adjusts volume level relative to distance.
	 *
	 * FIXME: hearing distance is slightly further than in Java client
	 *        See: games.stendhal.client.sound.facade.Audible*Area
	 *
	 * @param layername
	 *    String name of layer this sound is played on.
	 * @param snd
	 *    The sound to be adjusted.
	 * @param radius
	 *     Radius at which sound can be heard.
	 * @param sx
	 *     X coordinate of sound entity.
	 * @param sy
	 *     Y coordinate of sound entity.
	 * @param ex
	 *     X coordinate of listening entity.
	 * @param ey
	 *     Y coordinate of listening entity.
	 */
	adjustForDistance(layername: string, snd: Sound, radius: number,
			sx: number, sy: number, ex: number, ey: number) {
		const xdist = ex - sx;
		const ydist = ey - sy;
		const dist2 = xdist * xdist + ydist * ydist;
		const rad2 = radius * radius;
		if (dist2 > rad2) {
			// outside the specified radius
			snd.volume = 0.0;
		} else {
			const maxvol = this.getAdjustedVolume(layername, snd.basevolume);
			// The sound api does not guarantee anything about how the volume
			// works, so it does not matter much how we scale it.
			snd.volume = this.normVolume(Math.min(rad2 / (dist2 * 20), maxvol));
		}
	}

	/**
	 * Normalizes volume level.
	 *
	 * @param vol
	 *     The input volume.
	 * @return
	 *     Level between 0 and 1.
	 */
	private normVolume(vol: number): number {
		return vol < 0 ? 0 : vol > 1 ? 1 : vol;
	}

	/**
	 * Calculates actual volume against layer volume levels.
	 *
	 * @param layername
	 *     String identifier of layer sound is playing on.
	 * @param basevol
	 *     The sounds base volume level.
	 * @return
	 *     Volume level adjusted with "master" & associated layer.
	 */
	private getAdjustedVolume(layername: string, basevol: number): number {
		let actualvol = basevol * stendhal.config.getFloat("sound.master.volume");
		const lvol = stendhal.config.getFloat("sound." + layername + ".volume");
		if (typeof(lvol) !== "number") {
			console.warn("cannot adjust volume for layer \"" + layername + "\"");
			return actualvol;
		}
		return actualvol * lvol;
	}

	/**
	 * Applies the adjusted volume level to a sound.
	 *
	 * @param layername
	 *     String identifier of layer sound is playing on.
	 * @param snd
	 *     The sound to be adjusted.
	 */
	private applyAdjustedVolume(layername: string, snd: Sound) {
		snd.volume = this.normVolume(this.getAdjustedVolume(layername, snd.basevolume));
	}

	/**
	 * Sets layer volume level.
	 *
	 * TODO: return numeric value for determining what went wrong
	 *
	 * @param layername
	 *     Name of layer being adjusted.
	 * @param vol
	 *     Volume level.
	 * @return
	 *     <code>true</code> if volume level was set.
	 */
	setVolume(layername: string, vol: number): boolean {
		const oldvol = stendhal.config.getFloat("sound." + layername + ".volume");
		if (typeof(oldvol) === "undefined" || oldvol === "") {
			return false;
		}

		stendhal.config.set("sound." + layername + ".volume", this.normVolume(vol));

		const layerset = layername === "master" ? this.layers : [layername];
		for (const l of layerset) {
			const layersounds = this.active[l];
			if (typeof(layersounds) === "undefined") {
				continue;
			}
			for (const snd of layersounds) {
				if (typeof(snd.radius) === "number"
						&& typeof(snd.x) === "number"
						&& typeof(snd.y) === "number") {
					this.adjustForDistance(layername, snd, snd.radius,
							snd.x, snd.y, marauroa.me["_x"], marauroa.me["_y"]);
				} else {
					this.applyAdjustedVolume(layername, snd);
				}
			}
		}
		return true;
	}

	/**
	 * Retrieves layer volume level.
	 *
	 * @param layername
	 *     Layer string identifier.
	 * @return
	 *     Current volume level of layer.
	 */
	getVolume(layername="master"): number {
		let vol = stendhal.config.getFloat("sound." + layername + ".volume");
		if (typeof(vol) === "undefined" || isNaN(vol) || !isFinite(vol)) {
			console.warn("could not get volume for channel \"" + layername + "\"");
			return 1;
		}
		return this.normVolume(vol);
	}

	/**
	 * Toggles muted state of sound system.
	 */
	toggleSound() {
		const enabled = !stendhal.config.getBoolean("sound");
		stendhal.config.set("sound", enabled);

		if (enabled) {
			if (!this.unmuteAll()) {
				let errmsg = "Failed to unmute sounds:";
				for (const snd of this.getActive()) {
					if (snd && snd.src && snd.muted) {
						errmsg += "\n- " + snd.src;
					}
				}
				console.warn(errmsg);
			}
		} else {
			if (!this.muteAll()) {
				let errmsg = "Failed to mute sounds:";
				for (const snd of this.getActive()) {
					if (snd && snd.src && !snd.muted) {
						errmsg += "\n- " + snd.src;
					}
				}
				console.warn(errmsg);
			}
		}

		// notify client
		ui.onSoundUpdate();
	}

	/**
	 * Called at startup to pre-cache certain sounds.
	 */
	startupCache() {
		// login sound
		this.load("ui/login",
				stendhal.paths.sounds + "/ui/login.ogg", true);
	}
}
