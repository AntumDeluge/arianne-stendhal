/*
 * @(#) games/stendhal/client/gui/j2d/entity/Entity2DView.java
 *
 * $Id$
 */

package games.stendhal.client.gui.j2d.entity;

//
//

import games.stendhal.client.GameScreen;
import games.stendhal.client.StendhalUI;
import games.stendhal.client.stendhal;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.EntityChangeListener;
import games.stendhal.client.entity.EntityView;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.entity.User;
import games.stendhal.client.sprite.AnimatedSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.game.RPAction;

/**
 * The 2D view of an entity.
 */
public abstract class Entity2DView implements EntityView, EntityChangeListener {
	/**
	 * The entity this view is for
	 */
	protected Entity	entity;

	/**
	 * The entity image (or current one at least).
	 */
	private Sprite		sprite;

	/**
	 * The entity drawing composite.
	 */
	protected Composite	entityComposite;

	/**
	 * Whether this view is contained.
	 */
	private boolean		contained;

	/**
	 * Model values affecting animation.
	 */
	protected boolean	animatedChanged;

	/**
	 * Some model value changed.
	 */
	private boolean		changed;

	/**
	 * Model values affecting visual representation changed.
	 */
	protected boolean	representationChanged;

	/**
	 * The game screen this is for.
	 */
	protected GameScreen	screen;

	/**
	 * The visibility value changed.
	 */
	protected boolean	visibilityChanged;


	/**
	 * Create a 2D view of an entity.
	 *
	 * @param	entity		The entity to render.
	 */
	public Entity2DView(final Entity entity) {
		this.entity = entity;

		// TODO: Pass this in
		screen = GameScreen.get();

		entityComposite = AlphaComposite.SrcOver;
		contained = false;
		animatedChanged = false;
		changed = true;
		visibilityChanged = true;
		representationChanged = true;

		entity.addChangeListener(this);
	}


	//
	// Entity2DView
	//

	/**
	 * Build a list of entity specific actions.
	 * <strong>NOTE: The first entry should be the default.</strong>
	 *
	 * @param	list		The list to populate.
	 */
	protected void buildActions(final List<String> list) {
		list.add(ActionType.LOOK.getRepresentation());
	}


	/**
	 * Rebuild the representation using the base entity.
	 */
	protected void buildRepresentation() {
		setSprite(SpriteStore.get().getSprite(translate(entity.getType())));
	}


	/**
	 * Public method to initially create representation.
	 * TODO: Get rid of need for this.
	 */
	public void createRepresentation() {
		buildRepresentation();
	}


	/**
	 * Mark this as changed. This will force the <code>update()</code>
	 * method to be called.
	 */
	protected void markChanged() {
		changed = true;
	}


	/**
	 * Draw the entity.
	 *
	 * @param	g2d		The graphics to drawn on.
	 */
	public void draw(final Graphics2D g2d) {
		/*
		 * Handle entity changes
		 */
		if(changed) {
			update();
			changed = false;
		}

		Rectangle r = getArea();

		if(isContained()) {
			r.setLocation(0, 0);
		} else {
			r.x -= screen.getScreenViewX();
			r.y -= screen.getScreenViewY();

			if(!screen.isInScreen(r)) {
				return;
			}
		}

		Composite oldComposite = g2d.getComposite();

		try {
			g2d.setComposite(entityComposite);
			draw(g2d, r.x, r.y, r.width, r.height);
		} finally {
			g2d.setComposite(oldComposite);
		}

		drawEffect(g2d, r.x, r.y, r.width, r.height);
	}


	/**
	 * Draw the entity.
	 *
	 * @param	g2d		The graphics context.
	 * @param	x		The drawn X coordinate.
	 * @param	y		The drawn Y coordinate.
	 * @param	width		The drawn entity width.
	 * @param	height		The drawn entity height.
	 */
	protected void draw(final Graphics2D g2d, final int x, final int y, final int width, final int height) {
		drawEntity(g2d, x, y, width, height);

		if (stendhal.SHOW_COLLISION_DETECTION) {
			g2d.setColor(Color.blue);
			g2d.drawRect(x, y, width, height);

			g2d.setColor(Color.green);
			g2d.draw(screen.convertWorldToScreenView(entity.getArea()));
		}
	}


	/**
	 * Draw the effect part. This is drawn independent of the visibility
	 * setting.
	 *
	 * @param	g2d		The graphics context.
	 * @param	x		The drawn X coordinate.
	 * @param	y		The drawn Y coordinate.
	 * @param	width		The drawn entity width.
	 * @param	height		The drawn entity height.
	 */
	protected void drawEffect(final Graphics2D g2d, final int x, final int y, final int width, final int height) {
	}


	/**
	 * Draw the base entity part.
	 *
	 * @param	g2d		The graphics context.
	 * @param	x		The drawn X coordinate.
	 * @param	y		The drawn Y coordinate.
	 * @param	width		The drawn entity width.
	 * @param	height		The drawn entity height.
	 */
	protected void drawEntity(final Graphics2D g2d, final int x, final int y, final int width, final int height) {
		getSprite().draw(g2d, x, y);
	}


	/**
	 * Draw the top layer parts of an entity. This will be on down after
	 * all other game layers are rendered.
	 *
	 * @param	g2d		The graphics to drawn on.
	 */
	public void drawTop(final Graphics2D g2d) {
		Rectangle r = getArea();

		if(isContained()) {
			r.setLocation(0, 0);
		} else {
			r.x -= screen.getScreenViewX();
			r.y -= screen.getScreenViewY();

			if(!screen.isInScreen(r)) {
				return;
			}
		}

		Composite oldComposite = g2d.getComposite();

		try {
			g2d.setComposite(entityComposite);
			drawTop(g2d, r.x, r.y, r.width, r.height);
		} finally {
			g2d.setComposite(oldComposite);
		}
	}


	/**
	 * Draw the entity.
	 *
	 * @param	g2d		The graphics context.
	 * @param	x		The drawn X coordinate.
	 * @param	y		The drawn Y coordinate.
	 * @param	width		The drawn entity width.
	 * @param	height		The drawn entity height.
	 */
	protected void drawTop(final Graphics2D g2d, final int x, final int y, final int width, final int height) {
	}


	/**
	 * Get the screen area this is drawn in.
	 * NOTE: This only covers the area for the main sprite.
	 *
	 * @return	The area this draws in.
	 */
	public Rectangle getArea() {
		return new Rectangle(
			screen.convertWorldToScreen(getX()) + getXOffset(),
			screen.convertWorldToScreen(getY()) + getYOffset(),
			getWidth(), getHeight());
	}


	/**
	 * Get the class resource sub-path. The is the base sprite image name,
	 * relative to <code>translate()</code>.
	 *
	 * @return	The resource path.
	 */
	protected String getClassResourcePath() {
		String rpath = entity.getEntityClass();

		if(rpath != null) {
			String subclass = entity.getEntitySubClass();

			if(subclass != null) {
				rpath += "/" + subclass;
			}
		}

		return rpath;
	}


	/**
	 * Get the drawing composite.
	 *
	 * @return	The drawing composite.
	 */
	protected AlphaComposite getComposite() {
		int visibility = getVisibility();

		if(visibility == 100) {
			return AlphaComposite.SrcOver;
		} else if(visibility == 0) {
			return AlphaComposite.Dst;
		} else {
			return AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, visibility / 100.0f);
		}
	}


	/**
	 * Get the height.
	 *
	 * @return	The height (in pixels).
	 */
	public int getHeight() {
		return GameScreen.SIZE_UNIT_PIXELS;
	}


	/**
	 * Get the sprite image for this entity.
	 *
	 * @return	The image representation.
	 */
	public Sprite getSprite() {
		return sprite;
	}


	/**
	 * Get the entity's visibility.
	 *
	 * @return	The visibility value (0-100).
	 */
	protected int getVisibility() {
		return entity.getVisibility();
	}


	/**
	 * Get the width.
	 *
	 * @return	The width (in pixels).
	 */
	public int getWidth() {
		return GameScreen.SIZE_UNIT_PIXELS;
	}


	/**
	 * Get the entity's X coordinate.
	 *
	 * @return	The X coordinate.
	 */
	protected double getX() {
		return entity.getX();
	}


	/**
	 * Get the X offset alignment adjustment.
	 *
	 * @return	The X offset (in pixels).
	 */
	protected int getXOffset() {
		return 0;
	}


	/**
	 * Get the entity's Y coordinate.
	 *
	 * @return	The Y coordinate.
	 */
	protected double getY() {
		return entity.getY();
	}


	/**
	 * Get the Y offset alignment adjustment.
	 *
	 * @return	The Y offset (in pixels).
	 */
	protected int getYOffset() {
		return 0;
	}


	/**
	 * Determines on top of which other entities this entity should be
	 * drawn. Entities with a high Z index will be drawn on top of ones
	 * with a lower Z index.
	 * 
	 * Also, players can only interact with the topmost entity.
	 * 
	 * @return	The drawing index.
	 */
	public int getZIndex() {
		return 10000;
	}


	/**
	 * Determine if this view is currently animatable.
	 *
	 * @return	<code>true</code> if animating enabled.
	 */
	protected boolean isAnimating() {
		// Allow sprites to animate by default
		return true;
	}


	/**
	 * Determine if this view is contained, and should render in a
	 * compressed (it's defined) area without clipping anything important.
	 *
	 * @return	<code>true</code> if contained.
	 */
	public boolean isContained() {
		return contained;
	}


	/**
	 * Reorder the actions list (if needed). Please use as last resort.
	 *
	 * @param	list		The list to reorder.
	 */
	protected void reorderActions(final List<String> list) {
	}


	/**
	 * Set the sprite's animation state (if applicable).
	 *
	 * @param	sprite		The sprite.
	 */
	protected void setAnimation(final Sprite sprite) {
		if(sprite instanceof AnimatedSprite) {
			AnimatedSprite asprite = (AnimatedSprite) sprite;

			if(isAnimating()) {
				asprite.start();
			} else {
				asprite.stop();
				asprite.reset();
			}
		}
	}


	/**
	 * Set whether this view is contained, and should render in a
	 * compressed (it's defined) area without clipping anything important.
	 *
	 * @param	contained	<code>true</code> if contained.
	 */
	public void setContained(boolean contained) {
		this.contained = contained;
	}


	/**
	 * Set the content inspector for this entity (if needed).
	 *
	 * @param	inspector	The inspector.
	 */
	public void setInspector(final Inspector inspector) {
	}


	/**
	 * Set the sprite.
	 *
	 * @param	sprite		The sprite.
	 */
	protected void setSprite(final Sprite sprite) {
		setAnimation(sprite);
		animatedChanged = false;

		this.sprite = sprite;
	}


	/**
	 * Translate a resource name into it's sprite image path.
	 *
	 * @param	name		The resource name.
	 *
	 * @return	The full resource name.
	 */
	protected String translate(final String name) {
		return "data/sprites/" + name + ".png";
	}


	/**
	 * Handle updates.
	 */
	protected void update() {
		if(representationChanged) {
			buildRepresentation();
			representationChanged = false;
		}

		if(visibilityChanged) {
			entityComposite = getComposite();
			visibilityChanged = false;
		}

		if(animatedChanged) {
			setAnimation(getSprite());
			animatedChanged = false;
		}
	}


	//
	// EntityChangeListener
	//

	/**
	 * An entity was changed.
	 *
	 * @param	entity		The entity that was changed.
	 * @param	property	The property identifier.
	 */
	public void entityChanged(final Entity entity, final Object property)
	{
		changed = true;

		if(property == Entity.PROP_ANIMATED) {
			animatedChanged = true;
		} else if(property == Entity.PROP_TYPE) {
			representationChanged = true;
		} else if(property == Entity.PROP_VISIBILITY) {
			visibilityChanged = true;
		}
	}


	//
	// EntityView
	//

	/**
	 * Get the list of actions.
	 *
	 * @return	The list of actions.
	 */
	public final String [] getActions() {
		List<String> list = new ArrayList<String>();

		buildActions(list);

		/*
		 * Special admin options
		 */
		if (User.isAdmin()) {
			list.add(ActionType.ADMIN_INSPECT.getRepresentation());
			list.add(ActionType.ADMIN_DESTROY.getRepresentation());
			list.add(ActionType.ADMIN_ALTER.getRepresentation());
		}

		reorderActions(list);

		return list.toArray(new String[list.size()]);
	}


	/**
	 * Get the view's entity.
	 *
	 * @return	The view's entity.
	 */
	public Entity getEntity() {
		return entity;
	}


	/**
	 * Determine if this entity can be moved (e.g. via dragging).
	 *
	 * @return	<code>true</code> if the entity is movable.
	 */
	public boolean isMovable() {
		return false;
	}


	/**
	 * Perform the default action.
	 */
	public void onAction() {
		onAction(ActionType.LOOK);
	}


	/**
	 * Perform an action.
	 *
	 * @param	at		The action.
	 */
	public void onAction(final ActionType at) {
		int id = getEntity().getID().getObjectID();
		RPAction rpaction;

		switch (at) {
			case LOOK:
				rpaction = new RPAction();
				rpaction.put("type", at.toString());
				getEntity().fillTargetInfo(rpaction);

				at.send(rpaction);
				break;

			case ADMIN_INSPECT:
				rpaction = new RPAction();

				rpaction.put("type", at.toString());
				//fillTargetInfo(rpaction);
				rpaction.put("targetid", id);

				at.send(rpaction);
				break;

			case ADMIN_DESTROY:
				rpaction = new RPAction();
				rpaction.put("type", at.toString());

				rpaction.put("targetid", id);

				at.send(rpaction);
				break;

			case ADMIN_ALTER:
				StendhalUI.get().setChatLine("/alter #" + id + " ");
				break;

			default:
				Log4J.getLogger(Entity2DView.class).error("Action not processed: " + at);
				break;
		}
	}


	/**
	 * Release any view resources. This view should not be used after
	 * this is called.
	 */
	public void release()
	{
		entity.removeChangeListener(this);
		entity = null;
	}
}
