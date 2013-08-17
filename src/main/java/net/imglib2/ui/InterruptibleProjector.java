package net.imglib2.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.imglib2.AbstractInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.display.Projector;
import net.imglib2.ui.util.StopWatch;

/**
 * Similar to a {@link Projector}, this renders a target
 * 2D {@link RandomAccessibleInterval} by copying values from a source
 * {@link RandomAccessible}. In contrast to a {@link Projector} rendering can be
 * interrupted, in which case {@link #map(RandomAccessibleInterval, int)} will
 * return false. Also, rendering is multi-threaded and rendering time for the
 * last {@link #map(RandomAccessibleInterval, int)} can be queried.
 *
 * @param <A>
 *            pixel type of the source {@link RandomAccessible}.
 * @param <B>
 *            pixel type of the target {@link RandomAccessibleInterval}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class InterruptibleProjector< A, B > extends AbstractInterval
{
	final protected RandomAccessible< A > source;

	final protected Converter< ? super A, B > converter;

	protected long lastFrameRenderNanoTime;

	/**
	 * Create new projector with the given source and a converter from source to target pixel type.
	 */
	public InterruptibleProjector( final RandomAccessible< A > source, final Converter< ? super A, B > converter )
	{
		super( new long[ source.numDimensions() ] );
		this.source = source;
		this.converter = converter;
		lastFrameRenderNanoTime = -1;
	}

	/**
	 * Render the 2D target image by copying values from the source. Source can
	 * have more dimensions than the target Target coordinate <em>(x,y)</em> is
	 * copied from source coordinate <em>(x,y,0,...,0)</em>
	 *
	 * @param target
	 * @param numThreads
	 *            how many threads to use for rendering.
	 * @return true if rendering was completed (all target pixels written).
	 *         false if rendering was interrupted.
	 */
	public boolean map( final RandomAccessibleInterval< B > target, final int numThreads )
	{
		interrupted.set( false );

		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		min[ 0 ] = target.min( 0 );
		min[ 1 ] = target.min( 1 );
		max[ 0 ] = target.max( 0 );
		max[ 1 ] = target.max( 1 );

		final long cr = -target.dimension( 0 );

		final int width = ( int ) target.dimension( 0 );
		final int height = ( int ) target.dimension( 1 );

		final ExecutorService ex = Executors.newFixedThreadPool( numThreads );
		final int numTasks;
		if ( numThreads > 1 )
		{
			numTasks = Math.max( numThreads * 10, height );
		}
		else
			numTasks = 1;
		final double taskHeight = ( double ) height / numTasks;
		for ( int taskNum = 0; taskNum < numTasks; ++taskNum )
		{
			final long myMinY = min[ 1 ] + ( int ) ( taskNum * taskHeight );
			final long myHeight = ( (taskNum == numTasks - 1 ) ? height : ( int ) ( ( taskNum + 1 ) * taskHeight ) ) - myMinY - min[ 1 ];

			final Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					if ( interrupted.get() )
						return;

					final RandomAccess< A > sourceRandomAccess = source.randomAccess( InterruptibleProjector.this );
					final RandomAccess< B > targetRandomAccess = target.randomAccess( target );

					sourceRandomAccess.setPosition( min );
					sourceRandomAccess.setPosition( myMinY, 1 );
					targetRandomAccess.setPosition( min[ 0 ], 0 );
					targetRandomAccess.setPosition( myMinY, 1 );
					for ( int y = 0; y < myHeight; ++y )
					{
						if ( interrupted.get() )
							return;
						for ( int x = 0; x < width; ++x )
						{
							converter.convert( sourceRandomAccess.get(), targetRandomAccess.get() );
							sourceRandomAccess.fwd( 0 );
							targetRandomAccess.fwd( 0 );
						}
						sourceRandomAccess.move( cr, 0 );
						targetRandomAccess.move( cr, 0 );
						sourceRandomAccess.fwd( 1 );
						targetRandomAccess.fwd( 1 );
					}
				}
			};
			ex.execute( r );
		}
		ex.shutdown();
		try
		{
			ex.awaitTermination( 1, TimeUnit.HOURS );
		}
		catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}

		lastFrameRenderNanoTime = stopWatch.nanoTime();

		return ! interrupted.get();
	}

	protected AtomicBoolean interrupted = new AtomicBoolean();

	/**
	 * Abort {@link #map(RandomAccessibleInterval, int)} if it is currently running.
	 */
	public void cancel()
	{
		interrupted.set( true );
	}

	/**
	 * How many nano-seconds did the last
	 * {@link #map(RandomAccessibleInterval, int)} take.
	 *
	 * @return time needed for rendering the last frame, in nano-seconds.
	 */
	public long getLastFrameRenderNanoTime()
	{
		return lastFrameRenderNanoTime;
	}
}
