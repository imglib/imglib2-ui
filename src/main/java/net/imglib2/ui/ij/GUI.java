/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2012 Stephan Preibisch, Stephan Saalfeld, Tobias
 * Pietzsch, Albert Cardona, Barry DeZonia, Curtis Rueden, Lee Kamentsky, Larry
 * Lindsey, Johannes Schindelin, Christian Dietz, Grant Harris, Jean-Yves
 * Tinevez, Steffen Jaensch, Mark Longair, Nick Perry, and Jan Funke.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package net.imglib2.ui.ij;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;

import java.awt.Canvas;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Register mouse and key listeners. Backup and restore old listeners.
 *
 * @author Stephan Saalfeld
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class GUI
{
	/**
	 * Backup and clear current key and mouse listeners.
	 *
	 * @param imp
	 *            the ImagePlus in which to install the GUI.
	 */
	public GUI( final ImagePlus imp )
	{
		window = imp.getWindow();
		canvas = imp.getCanvas();
		ij = IJ.getInstance();
		backup = backupGui();
		clearGui();
		handlers = new ArrayList< Object >();
	}

	/**
	 * Add new event handler.
	 */
	public void addHandler( final Object handler )
	{
		handlers.add( handler );

		if ( KeyListener.class.isInstance( handler ) )
		{
			canvas.addKeyListener( ( KeyListener ) handler );
			window.addKeyListener( ( KeyListener ) handler );
			if ( ij != null )
				ij.addKeyListener( ( KeyListener ) handler );
		}

		if ( MouseMotionListener.class.isInstance( handler ) )
			canvas.addMouseMotionListener( ( MouseMotionListener ) handler );

		if ( MouseListener.class.isInstance( handler ) )
			canvas.addMouseListener( ( MouseListener ) handler );

		if ( MouseWheelListener.class.isInstance( handler ) )
			canvas.addMouseWheelListener( ( MouseWheelListener ) handler );
	}

	/**
	 * Add new event handlers.
	 */
	public void addHandlers( final Collection< Object > newHandlers )
	{
		for ( final Object h : newHandlers )
			addHandler( h );
	}

	/**
	 * Restore the previously active Event handlers.
	 */
	public void restoreGui()
	{
		restoreGui( backup );
	}

	/**
	 * Stores current mouse and keyboard listeners.
	 */
	protected class State
	{
		final KeyListener[] windowKeyListeners;

		final KeyListener[] canvasKeyListeners;

		final KeyListener[] ijKeyListeners;

		final MouseListener[] canvasMouseListeners;

		final MouseMotionListener[] canvasMouseMotionListeners;

		final MouseWheelListener[] canvasMouseWheelListeners;

		public State()
		{
			canvasKeyListeners = canvas.getKeyListeners();
			windowKeyListeners = window.getKeyListeners();
			ijKeyListeners = ( ij == null ) ? null : ij.getKeyListeners();
			canvasMouseListeners = canvas.getMouseListeners();
			canvasMouseMotionListeners = canvas.getMouseMotionListeners();
			canvasMouseWheelListeners = canvas.getMouseWheelListeners();
		}
	}

	final protected ImageWindow window;

	final protected Canvas canvas;

	final protected ImageJ ij;

	final protected State backup;

	final protected ArrayList< Object > handlers;

	/**
	 * Restore the event handlers from a {@link State}.
	 *
	 * @param state
	 *            the state to restore.
	 */
	protected void restoreGui( final State state )
	{
		clearGui();
		for ( final KeyListener l : state.canvasKeyListeners )
			canvas.addKeyListener( l );
		for ( final KeyListener l : state.windowKeyListeners )
			window.addKeyListener( l );
		if ( ij != null )
			for ( final KeyListener l : state.ijKeyListeners )
				ij.addKeyListener( l );
		for ( final MouseListener l : state.canvasMouseListeners )
			canvas.addMouseListener( l );
		for ( final MouseMotionListener l : state.canvasMouseMotionListeners )
			canvas.addMouseMotionListener( l );
		for ( final MouseWheelListener l : state.canvasMouseWheelListeners )
			canvas.addMouseWheelListener( l );
	}

	/**
	 * Backup active event handlers for restore.
	 */
	protected State backupGui()
	{
		return new State();
	}

	/**
	 * Remove all event handlers.
	 */
	protected void clearGui()
	{
		KeyListener[] keyListeners = canvas.getKeyListeners();
		for ( final KeyListener l : keyListeners )
			canvas.removeKeyListener( l );

		keyListeners = window.getKeyListeners();
		for ( final KeyListener l : keyListeners )
			window.removeKeyListener( l );

		if ( ij != null )
		{
			keyListeners = ij.getKeyListeners();
			for ( final KeyListener l : keyListeners )
				ij.removeKeyListener( l );
		}

		final MouseListener[] mouseListeners = canvas.getMouseListeners();
		for ( final MouseListener l : mouseListeners )
			canvas.removeMouseListener( l );

		final MouseMotionListener[] mouseMotionListeners = canvas.getMouseMotionListeners();
		for ( final MouseMotionListener l : mouseMotionListeners )
			canvas.removeMouseMotionListener( l );

		final MouseWheelListener[] mouseWheelListeners = window.getMouseWheelListeners();
		for ( final MouseWheelListener l : mouseWheelListeners )
			canvas.removeMouseWheelListener( l );
	}
}
