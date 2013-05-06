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
package net.imglib2.ui.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import javax.swing.JComponent;
import javax.swing.JFrame;

import net.imglib2.Interval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.AbstractInteractiveDisplay3D;
import net.imglib2.ui.BoxOverlay;
import net.imglib2.ui.ScreenImageRenderer;
import net.imglib2.ui.TransformEventHandler3D;
import net.imglib2.ui.TransformListener3D;
import net.imglib2.util.Intervals;

public class SwingInteractiveDisplay3D extends AbstractInteractiveDisplay3D implements TransformListener3D
{
	/**
	 * The size of the {@link #source}. This is used for displaying the
	 * navigation wire-frame cube.
	 */
	final protected Interval sourceInterval;

	/**
	 * Additional transformation to apply to {@link #sourceInterval}
	 * when displaying navigation wire-frame cube. This is useful
	 * for pre-scaling when showing anisotropic data, for example.
	 */
	final protected AffineTransform3D sourceTransform;

	/**
	 * Used to render the image for on-screen display.
	 */
	protected ARGBScreenImage screenImage;

	/**
	 * The transformation interactively set by the user.
	 */
	final protected AffineTransform3D viewerTransform;

	/**
	 * Transformation from {@link #source} to {@link #screenImage}. This is a
	 * concatenation of {@link #sourceTransform} and the interactive
	 * {@link #viewerTransform transform} set by the user.
	 */
	final protected AffineTransform3D sourceToScreen;

	/**
	 * Navigation wire-frame cube.
	 */
	final protected BoxOverlay box;

	/**
	 * Screen interval in which to display navigation wire-frame cube.
	 */
	final protected Interval boxInterval;

	/**
	 * Mouse/Keyboard handler to manipulate {@link #viewerTransform} transformation.
	 */
	final protected TransformEventHandler3D handler;

	final protected JFrame frame;

	final protected Viewer3DCanvas canvas;

	final protected ScreenImageRenderer renderer;

	final protected TransformListener3D renderTransformListener;

	/**
	 * The {@link BufferedImage} that is actually drawn on the canvas. Depending
	 * on {@link #discardAlpha} this is either the {@link BufferedImage}
	 * obtained from {@link #screenImage}, or {@link #screenImage}s buffer
	 * re-wrapped using a RGB color model.
	 */
	protected BufferedImage bufferedImage;

	/**
	 * Whether to discard the {@link #screenImage} alpha components when drawing.
	 */
	final protected boolean discardAlpha = true;

	/**
	 *
	 * @param width
	 *            window width.
	 * @param height
	 *            window height.
	 * @param sourceInterval
	 *            The size of the source. This is used for displaying the
	 *            navigation wire-frame cube.
	 * @param sourceTransform
	 *            Additional transformation to apply to {@link #sourceInterval}
	 *            when displaying navigation wire-frame cube. This is useful for
	 *            pre-scaling when showing anisotropic data, for example.
	 * @param renderer
	 *            is called to render into a {@link ARGBScreenImage}.
	 * @param renderTransformListener
	 *            is notified when the viewer transformation is changed.
	 */
	public SwingInteractiveDisplay3D( final int width, final int height, final Interval sourceInterval, final AffineTransform3D sourceTransform, final ScreenImageRenderer renderer, final TransformListener3D renderTransformListener )
	{
		this.sourceInterval = sourceInterval;
		this.sourceTransform = sourceTransform;
		this.screenImage = new ARGBScreenImage( width, height );
		this.bufferedImage = getBufferedImage( screenImage );
		viewerTransform = new AffineTransform3D();
		sourceToScreen = new AffineTransform3D();
		this.renderer = renderer;
		renderer.screenImageChanged( screenImage );
		this.renderTransformListener = renderTransformListener;

		boxInterval = Intervals.createMinSize( 10, 10, 80, 60 );
		box = new BoxOverlay();

		handler = new TransformEventHandler3D( this );
		handler.setWindowCenter( width / 2, height / 2 );

		canvas = new Viewer3DCanvas( width, height );
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
				synchronized( viewerTransform )
				{
					viewerTransform.set( handler.getTransform() );
					viewerTransform.set( viewerTransform.get( 0, 3 ) - oldW/2, 0, 3 );
					viewerTransform.set( viewerTransform.get( 1, 3 ) - oldH/2, 1, 3 );
					viewerTransform.scale( ( double ) w / oldW );
					viewerTransform.set( viewerTransform.get( 0, 3 ) + w/2, 0, 3 );
					viewerTransform.set( viewerTransform.get( 1, 3 ) + h/2, 1, 3 );
					handler.setTransform( viewerTransform );
					handler.setWindowCenter( w / 2, h / 2 );
					renderTransformListener.transformChanged( viewerTransform );
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

	static final private ColorModel RGB_COLOR_MODEL = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

	private BufferedImage getBufferedImage( final ARGBScreenImage img )
	{
		if ( discardAlpha )
		{
			final BufferedImage si = img.image();
			final SampleModel sampleModel = RGB_COLOR_MODEL.createCompatibleWritableRaster( 1, 1 ).getSampleModel().createCompatibleSampleModel( si.getWidth(), si.getHeight() );
			final DataBuffer dataBuffer = si.getRaster().getDataBuffer();
			final WritableRaster rgbRaster = Raster.createWritableRaster( sampleModel, dataBuffer, null );
			return new BufferedImage( RGB_COLOR_MODEL, rgbRaster, false, null );
		}
		else
			return img.image();
	}

	final protected class Viewer3DCanvas extends JComponent
	{
		private static final long serialVersionUID = 2040463400821436135L;

		public Viewer3DCanvas( final int w, final int h )
		{
			super();
			setPreferredSize( new Dimension( w, h ) );
		}

		@Override
		public void paintComponent( final Graphics g )
		{
			g.drawImage( bufferedImage, 0, 0, getWidth(), getHeight(), null );
			synchronized( viewerTransform )
			{
				sourceToScreen.set( viewerTransform );
			}
			sourceToScreen.concatenate( sourceTransform );
			box.paint( ( Graphics2D ) g, sourceInterval, screenImage, sourceToScreen, boxInterval );
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
			bufferedImage = getBufferedImage( screenImage );
			renderer.screenImageChanged( screenImage );
		}
		final boolean valid = renderer.drawScreenImage();
		canvas.repaint();
		return valid;
	}

	@Override
	public void transformChanged( final AffineTransform3D transform )
	{
		synchronized( viewerTransform )
		{
			viewerTransform.set( transform );
		}
		renderTransformListener.transformChanged( transform );
		requestRepaint();
	}

	@Override
	public void setViewerTransform( final AffineTransform3D transform )
	{
		handler.setTransform( transform );
		transformChanged( transform );
	}

	@Override
	public AffineTransform3D getViewerTransform()
	{
		return viewerTransform;
	}
}
