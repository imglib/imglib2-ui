/**
 * Copyright (c) 2011, Stephan Saalfeld
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.  Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.  Neither the name of the imglib project nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.imglib2.ui;

import net.imglib2.converter.Converter;
import net.imglib2.display.AbstractLinearRange;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * 
 *
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 */
public class CompositeRGBConverter< R extends RealType< R > > 
extends AbstractLinearRange implements Converter< R, ARGBType >
{
	final protected int band;
	
	public CompositeRGBConverter()
	{
		super();
		band = 0;
	}
	
	public CompositeRGBConverter( final double min, final double max, final int band )
	{
		super( min, max );
		this.band = band;
	}
	
	@Override
	public void convert( final R input, final ARGBType output )
	{
		final double a = input.getRealDouble();
		final int b = Math.min( 255, roundPositive( Math.max( 0, ( ( a - min ) / scale * 255.0 ) ) ) );
		
		int shift = 8 * band;
		final int argb = (b  >> shift) & 0xff;

		//	red(value >> 16) & 0xff;
		//	green(value >> 8) & 0xff;
		//	blue value & 0xff;
	
		//final int argb = 0xff000000 | ( ( ( b << 8 ) | b ) << 8 ) | b;
		
		//output.set( argb );
		
		output.add(new ARGBType(argb));
	}
}
