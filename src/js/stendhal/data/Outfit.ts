/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { OutfitStore } from "./OutfitStore";

import { Pair } from "../util/Pair";


/**
 * Represents an entity's sprite layers.
 */
export class Outfit {

	/** Sprite layers. */
	private readonly layers: {[name: string]: number};
	/** Layer coloring values. */
	private coloring?: {[name: string]: number};


	/**
	 * Creates a new outfit.
	 *
	 * @param layers
	 *   Sprite layers.
	 */
	constructor(layers: {[layer: string]: number}={}) {
		this.layers = layers;
	}

	/**
	 * Sets a layer's sprite index.
	 *
	 * @param name
	 *   Layer name.
	 * @param index
	 *   Sprite image index.
	 */
	public setLayer(name: string, index: number) {
		this.layers[name] = index;
	}

	/**
	 * Unsets a layer's sprite index.
	 *
	 * @param name
	 *   Layer name.
	 */
	public unsetLayer(name: string) {
		delete this.layers[name];
	}

	/**
	 * Retrieves the index value of a specified layer.
	 *
	 * @param name
	 *   Layer name.
	 * @return
	 *   Sprite image index.
	 */
	public getLayerIndex(name: string): number|undefined {
		return this.layers[name];
	}

	/**
	 * Retrieves sorted layer info.
	 *
	 * @return {util.Pair.Pair<string, number>[]}
	 *   Layers info.
	 */
	public getLayers(): Pair<string, number>[] {
		const layers: Pair<string, number>[] = [];
		// only include valid layers
		for (const name of ["detail-rear", ...OutfitStore.get().getLayerNames()]) {
			const index = this.layers[name];
			if (index != undefined) {
				layers.push(new Pair(name, index));
			}
		}
		return layers;
	}

	/**
	 * Sets coloring info for this outfit.
	 *
	 * @param coloring {object}
	 *   Color values indexed by layer name.
	 */
	public setColoring(coloring: {[name: string]: number}) {
		this.coloring = coloring;
	}

	/**
	 * Retrieves coloring info for this outfit.
	 *
	 * @return {object}
	 *   Color values indexed by layer name or `undefined`.
	 */
	public getColoring(): {[name: string]: number}|undefined {
		return this.coloring;
	}

	/**
	 * Retrieves coloring for a single layer.
	 *
	 * @param name {string}
	 *   Layer name.
	 * @return {number}
	 *   Color value or `undefined`.
	 */
	public getLayerColor(name: string): number|undefined {
		return this.coloring && name in this.coloring ? this.coloring[name] : undefined;
	}

	/**
	 * Retrieves signature identifying this outfit.
	 */
	public getSignature(): string {
		const lsig: string[] = [];
		const csig: string[] = [];
		for (const layer of this.getLayers()) {
			lsig.push(layer.join("="));
		}
		if (this.coloring) {
			for (const name of Object.keys(this.coloring)) {
				csig.push(name + "=" + this.coloring[name]);
			}
		}
		return "outfit(" + lsig.join(",") + ")" + (csig ? " colors(" + csig.join(",") + ")" : "");
	}

	/**
	 * Compares outfit signatures for equality.
	 *
	 * @param other {data.Outfit.Outfit}
	 *   The outfit to compare against this one.
	 */
	public equals(other: Outfit): boolean {
		return other.getSignature() === this.getSignature();
	}
}
