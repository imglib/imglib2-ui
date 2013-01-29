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

package net.imglib2.ui.swing;
/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2013 Stephan Preibisch, Tobias Pietzsch, Barry DeZonia,
 * Stephan Saalfeld, Albert Cardona, Curtis Rueden, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Lee Kamentsky, Larry Lindsey, Grant Harris,
 * Mark Hiner, Aivar Grislis, Martin Horn, Nick Perry, Michael Zinsmaier,
 * Steffen Jaensch, Jan Funke, Mark Longair, and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.ColorModel;

import javax.swing.JComponent;
import javax.swing.JFrame;

import net.imglib2.display.ARGBScreenImage;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.AbstractInteractiveDisplay2D;
import net.imglib2.ui.ScreenImageRenderer;
import net.imglib2.ui.TransformEventHandler2D;
import net.imglib2.ui.TransformListener2D;

public class SwingInteractiveDisplay2D extends AbstractInteractiveDisplay2D implements TransformListener2D
{
	/**
	 * Used to render the image for on-screen display.
	 */
	protected ARGBScreenImage screenImage;

	/**
	 * Transformation from {@link #sourceInterval} to {@link #screenImage}.
	 */
	final protected AffineTransform2D sourceToScreen;

	/**
	 * Mouse/Keyboard handler to manipulate {@link #sourceToScreen} transformation.
	 */
	final protected TransformEventHandler2D handler;

	final protected JFrame frame;

	final protected Viewer2DCanvas canvas;

	final protected ScreenImageRenderer renderer;

	final protected TransformListener2D renderTransformListener;

	public SwingInteractiveDisplay2D( final int width, final int height, final ScreenImageRenderer renderer, final TransformListener2D renderTransformListener )
	{
		this.screenImage = new ARGBScreenImage( width, height );
		sourceToScreen = new AffineTransform2D();
		this.renderer = renderer;
		renderer.screenImageChanged( screenImage );
		this.renderTransformListener = renderTransformListener;

		handler = new TransformEventHandler2D( this );
		handler.setWindowCenter( width / 2, height / 2 );

		canvas = new Viewer2DCanvas( width, height );
		canvas.addComponentListener( new ComponentListener()
		{
			@Override
			public void componentShown( final ComponentEvent e ) {}

			@Override
			public void componentMoved( final ComponentEvent e ) {}

			@Override
			public void componentHidden( final ComponentEvent e ) {}

			@Override
			public void componentResized( final ComponentEvent e )
			{
				final int oldW = ( int ) screenImage.dimension( 0 );
				final int oldH = ( int ) screenImage.dimension( 1 );
				final int w = canvas.getWidth();
				final int h = canvas.getHeight();
				synchronized( sourceToScreen )
				{
					sourceToScreen.set( handler.getTransform() );
					sourceToScreen.set( sourceToScreen.get( 0, 2 ) - oldW/2, 0, 2 );
					sourceToScreen.set( sourceToScreen.get( 1, 2 ) - oldH/2, 1, 2 );
					sourceToScreen.scale( ( double ) w / oldW );
					sourceToScreen.set( sourceToScreen.get( 0, 2 ) + w/2, 0, 2 );
					sourceToScreen.set( sourceToScreen.get( 1, 2 ) + h/2, 1, 2 );
					handler.setTransform( sourceToScreen );
					handler.setWindowCenter( w / 2, h / 2 );
					renderTransformListener.transformChanged( sourceToScreen );
				}
				requestRepaint();
			}
		} );

		final GraphicsConfiguration gc = getSuitableGraphicsConfiguration( ARGBScreenImage.ARGB_COLOR_MODEL );
		frame = new JFrame( "ImgLib2", gc );
		frame.getRootPane().setDoubleBuffered( true );
		frame.getContentPane().add( canvas );
		frame.pack();
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setVisible( true );

		addHandler( handler );
	}

	/**
	 * Add new event handler.
	 */
	@Override
	public void addHandler( final Object h )
	{
		if ( KeyListener.class.isInstance( h ) )
			frame.addKeyListener( ( KeyListener ) h );

		if ( MouseMotionListener.class.isInstance( h ) )
			canvas.addMouseMotionListener( ( MouseMotionListener ) h );

		if ( MouseListener.class.isInstance( h ) )
			canvas.addMouseListener( ( MouseListener ) h );

		if ( MouseWheelListener.class.isInstance( h ) )
			canvas.addMouseWheelListener( ( MouseWheelListener ) h );
	}

	final protected class Viewer2DCanvas extends JComponent
	{
		private static final long serialVersionUID = -8868693317975626367L;

		public Viewer2DCanvas( final int w, final int h )
		{
			super();
			setPreferredSize( new Dimension( w, h ) );
		}

		@Override
		public void paintComponent( final Graphics g )
		{
			g.drawImage( screenImage.image(), 0, 0, getWidth(), getHeight(), null );
			renderer.drawOverlays( g );
		}
	}

	protected static GraphicsConfiguration getSuitableGraphicsConfiguration( final ColorModel colorModel )
	{
		final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final GraphicsConfiguration defaultGc = device.getDefaultConfiguration();
		if ( defaultGc.getColorModel( Transparency.TRANSLUCENT ).equals( colorModel ) )
			return defaultGc;

		for ( final GraphicsConfiguration gc : device.getConfigurations() )
			if ( gc.getColorModel( Transparency.TRANSLUCENT ).equals( colorModel ) )
				return gc;

		return defaultGc;
	}

	@Override
	public boolean paint()
	{
		final int w = canvas.getWidth();
		final int h = canvas.getHeight();
		if ( screenImage.dimension( 0 ) != w || screenImage.dimension( 1 ) != h )
		{
			screenImage = new ARGBScreenImage( w, h );
			renderer.screenImageChanged( screenImage );
		}
		final boolean valid = renderer.drawScreenImage();
		canvas.repaint();
		return valid;
	}

	@Override
	public void transformChanged( final AffineTransform2D transform )
	{
		synchronized( sourceToScreen )
		{
			sourceToScreen.set( transform );
		}
		renderTransformListener.transformChanged( transform );
		requestRepaint();
	}

	@Override
	public void setViewerTransform( final AffineTransform2D transform )
	{
		handler.setTransform( transform );
		transformChanged( transform );
	}

	@Override
	public AffineTransform2D getViewerTransform()
	{
		return sourceToScreen;
	}
}
