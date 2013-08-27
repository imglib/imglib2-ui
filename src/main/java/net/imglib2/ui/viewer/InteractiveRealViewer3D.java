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

import net.imglib2.Interval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.AffineTransformType3D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.TransformEventHandler3D;
import net.imglib2.ui.overlay.BoxOverlayRenderer;
import net.imglib2.ui.util.Defaults;
import net.imglib2.ui.util.FinalSource;

/**
 * Interactive viewer for a 3D {@link RealRandomAccessible}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class InteractiveRealViewer3D< T > extends InteractiveRealViewer< AffineTransform3D, InteractiveDisplayCanvasComponent< AffineTransform3D > >
{
	/**
	 * Create an interactive viewer for a 3D {@link RealRandomAccessible}.
	 *
	 * @param width
	 *            window width.
	 * @param height
	 *            window height.
	 * @param source
	 *            The source image to display. It is assumed that the source is
	 *            extended to infinity.
	 * @param sourceInterval
	 *            The size of the source in source local coordinates. This is
	 *            used for displaying a navigation wire-frame cube.
	 * @param sourceTransform
	 *            Transformation from source to global coordinates. This is
	 *            useful for pre-scaling when showing anisotropic data, for
	 *            example.
	 * @param converter
	 *            Converter from the source type to argb for rendering the
	 *            source.
	 */
	public InteractiveRealViewer3D( final int width, final int height, final RealRandomAccessible< T > source, final Interval sourceInterval, final AffineTransform3D sourceTransform, final Converter< ? super T, ARGBType > converter )
	{
		super( AffineTransformType3D.instance,
				new InteractiveDisplayCanvasComponent< AffineTransform3D >( width, height, TransformEventHandler3D.factory() ),
				Defaults.rendererFactory( AffineTransformType3D.instance, new FinalSource< T, AffineTransform3D >( source, sourceTransform, converter ) ) );

		final BoxOverlayRenderer box = new BoxOverlayRenderer( width, height );
		box.setSource( sourceInterval, sourceTransform );
		display.addTransformListener( box );
		display.addOverlayRenderer( box );
	}

	public InteractiveRealViewer3D( final int width, final int height, final RealRandomAccessible< T > source, final Interval sourceInterval, final Converter< ? super T, ARGBType > converter )
	{
		this( width, height, source, sourceInterval, new AffineTransform3D(), converter );
	}
}
