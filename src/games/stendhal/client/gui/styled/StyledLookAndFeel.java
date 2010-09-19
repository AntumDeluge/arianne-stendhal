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
package games.stendhal.client.gui.styled;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Logger;

public class StyledLookAndFeel extends MetalLookAndFeel {
	private static final String pkg = "games.stendhal.client.gui.styled.";
	
	private final Style style;
	
	public StyledLookAndFeel(Style style) {
		super();
		this.style = style;
	}
	
	@Override
	protected void initClassDefaults(UIDefaults table) {
		super.initClassDefaults(table);

		Object[] uiDefaults = {
			// Provide access to the style for the components
			"StendhalStyle", style,
			// The component UIs
			"ButtonUI", pkg + "StyledButtonUI",
			"CheckBoxUI", pkg + "StyledCheckBoxUI",
			"ComboBoxUI", pkg + "StyledComboBoxUI",
			"LabelUI", pkg + "StyledLabelUI",
			"MenuItemUI", pkg + "StyledMenuItemUI",
			"OptionPaneUI", pkg + "StyledOptionPaneUI",
			"PanelUI", pkg + "StyledPanelUI",
			"PasswordFieldUI", pkg + "StyledPasswordFieldUI",
			"PopupMenuUI", pkg + "StyledPopupMenuUI",
			"ProgressBarUI", pkg + "StyledProgressBarUI",
			"SeparatorUI", pkg + "StyledSeparatorUI",
			"ScrollBarUI", pkg + "StyledScrollBarUI",
			"ScrollPaneUI", pkg + "StyledScrollPaneUI",
			"SliderUI", pkg + "StyledSliderUI",
			"SplitPaneUI", pkg + "StyledSplitPaneUI",
			"TextFieldUI", pkg + "StyledTextFieldUI",
			"ToolTipUI", pkg + "StyledToolTipUI",
		};
		
		table.putDefaults(uiDefaults);
	}
	
	@Override
	protected void initComponentDefaults(UIDefaults table) {
		super.initComponentDefaults(table);
		restoreSystemKeymaps(table);
	}
	
	/**
	 * A hack to work around swing hard coding key bindings, and providing
	 * no reasonable way to access the native ones. Add new components and
	 * key bindings here when mac users report them.
	 * 
	 * @param table A mess of all UI defaults crammed in one hash map
	 */
	private void restoreSystemKeymaps(UIDefaults table) {
		/*
		 * Input mappings that need to be tuned. the list is not exhaustive and
		 * will likely need more entries when new components start to be used.
		 */
		String[] keys = { "EditorPane.focusInputMap",
				"FormattedTextField.focusInputMap",
				"PasswordField.focusInputMap",
				"TextArea.focusInputMap",
				"TextField.focusInputMap",
				"TextPane.focusInputMap" };
		
		// Native modifier key. Ctrl for others, cmd on mac
		int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		for (String key : keys) {
			Object value = table.get(key);
			if (value instanceof InputMapUIResource) {
				InputMapUIResource map = (InputMapUIResource) value;
				
				// CUT
				remapKey(map, KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, modifier);
				
				// COPY
				remapKey(map, KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, modifier);
				
				// PASTE
				remapKey(map, KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK, modifier);
			} else {
				Logger.getLogger(StyledLookAndFeel.class).error("Can not modify resource: " + key);
			}
		}
	}
	
	/**
	 * Remap a swing default key binding to a native one, if needed.
	 * 
	 * @param map keymap to be modified
	 * @param key
	 * @param defaultModifier swing default modifier key for the action
	 * @param nativeModifier native modifier key for the action
	 */
	private void remapKey(InputMapUIResource map, int key, int defaultModifier, 
			int nativeModifier) {
		KeyStroke defaultKey = KeyStroke.getKeyStroke(key, defaultModifier);
		Object action = map.get(defaultKey);
		
		KeyStroke nativeKey = KeyStroke.getKeyStroke(key, nativeModifier);
		if (!nativeKey.equals(defaultKey)) {
			map.remove(defaultKey);
			map.put(nativeKey, action);
		}
	}
	
	@Override
	public boolean isSupportedLookAndFeel() {
		// supported everywhere
		return true;
	}
	
	@Override
	public boolean isNativeLookAndFeel() {
		return false;
	}
	
	@Override
	public String getDescription() {
		return "Stendhal pixmap look and feel";
	}
	
	@Override
	public String getID() {
		return "Stendhal";
	}
	
	@Override
	public String getName() {
		return "Stendhal";
	}
}
