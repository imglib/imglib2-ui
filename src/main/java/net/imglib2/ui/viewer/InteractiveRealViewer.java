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
package net.imglib2.ui.viewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;

import net.imglib2.concatenate.Concatenable;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineSet;
import net.imglib2.ui.AffineTransformType;
import net.imglib2.ui.InteractiveDisplayCanvas;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.RenderSource;
import net.imglib2.ui.Renderer;
import net.imglib2.ui.RendererFactory;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.overlay.BufferedImageOverlayRenderer;
import net.imglib2.ui.util.GuiUtil;

/**
 * Simple interactive viewer window. It creates a JFrame with the given
 * {@link InteractiveDisplayCanvas canvas}, and sets up transformation handling
 * and painting of a given {@link RenderSource source}.
 * <p>
 * It implements {@link PainterThread.Paintable} to handle {@link #paint()
 * repainting} through a {@link PainterThread}. It implements
 * {@link TransformListener} to be notified about viewer transformation changes
 * made by the user.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 *
 * @param <T>
 *            pixel type
 * @param <A>
 *            transform type
 * @param <C>
 *            canvas component type
 */
public class InteractiveRealViewer< T, A extends AffineSet & AffineGet & Concatenable< AffineGet >, C extends JComponent & InteractiveDisplayCanvas< A > > implements TransformListener< A >, PainterThread.Paintable
{
	final protected AffineTransformType< A > transformType;

	/**
	 * Transformation set by the interactive viewer.
	 */
	final protected A viewerTransform;

	/**
	 * Canvas used for displaying the rendered {@link #screenImages screen image}.
	 */
	final protected C display;

	/**
	 * Thread that triggers repainting of the display.
	 */
	final protected PainterThread painterThread;

	final protected Renderer< A > imageRenderer;

	final protected JFrame frame;

	final protected RenderSource< T, A > source;

	/**
	 * Create an interactive viewer window displaying a given
	 * {@link RenderSource <code>source</code>} in the given
	 * <code>interactiveDisplayCanvas</code>.
	 * <p>
	 * A {@link Renderer} is created that paints to a
	 * {@link BufferedImageOverlayRenderer} render target which is displayed on
	 * the canvas as an {@link OverlayRenderer}. A {@link PainterThread} is
	 * created which queues repainting requests from the renderer and
	 * interactive canvas, and triggers {@link #paint() repainting} of the
	 * viewer.
	 *
	 * @param transformType
	 * @param interactiveDisplayCanvas
	 *            the canvas {@link JComponent} which will show the rendered images.
	 * @param source
	 *            the source data to render.
	 * @param rendererFactory
	 *            is used to create a {@link Renderer} for the source.
	 */
	public InteractiveRealViewer( final AffineTransformType< A > transformType, final C interactiveDisplayCanvas, final RenderSource< T, A > source, final RendererFactory rendererFactory )
	{
		this.transformType = transformType;
		painterThread = new PainterThread( this );
		viewerTransform = transformType.createTransform();
		display = interactiveDisplayCanvas;
		display.addTransformListener( this );

		final BufferedImageOverlayRenderer target = new BufferedImageOverlayRenderer();
		imageRenderer = rendererFactory.create( transformType, target, painterThread );
		display.addOverlayRenderer( target );

//		final GraphicsConfiguration gc = GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.ARGB_COLOR_MODEL );
		final GraphicsConfiguration gc = GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL );
		frame = new JFrame( "ImgLib2", gc );
		frame.getRootPane().setDoubleBuffered( true );
		final Container content = frame.getContentPane();
		content.add( display, BorderLayout.CENTER );
		frame.pack();
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				painterThread.interrupt();
			}
		} );
		frame.setVisible( true );

		this.source = source;

		painterThread.start();

	}

	/**
	 * Render the source using the current viewer transformation and
	 */
	@Override
	public void paint()
	{
		imageRenderer.paint( source, viewerTransform );
		display.repaint();
	}


	@Override
	public void transformChanged( final A transform )
	{
		transformType.set( viewerTransform, transform );
		requestRepaint();
	}

	/**
	 * Get the canvas component used for painting
	 *
	 * @return the canvas component used for painting.
	 */
	public C getDisplayCanvas()
	{
		return display;
	}

	/**
	 * Request a repaint of the display.
	 * Calls {@link Renderer#requestRepaint()}.
	 */
	public void requestRepaint()
	{
		imageRenderer.requestRepaint();
	}
}
