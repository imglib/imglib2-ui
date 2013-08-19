package net.imglib2.ui;


/**
 * Change a transformation in response to user input (mouse events, key events,
 * etc.). Report to a {@link TransformListener} when the transformation changes.
 * The {@link TransformEventHandler} receives notifications about changes of the
 * canvas size (it may react for example by changing the scale of the
 * transformation accordingly).
 *
 * @param <A>
 *            type of transformation.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface TransformEventHandler< A >
{
	/**
	 * Get (a copy of) the current source-to-screen transform.
	 *
	 * @return current transform.
	 */
	public A getTransform();

	/**
	 * Set the current source-to-screen transform.
	 */
	public void setTransform( final A transform );

	/**
	 * This is called, when the screen size of the canvas (the component
	 * displaying the image and generating mouse events) changes. This can be
	 * used to determine screen coordinates to keep fixed while zooming or
	 * rotating with the keyboard, e.g., set these to
	 * <em>(width/2, height/2)</em>. It can also be used to update the current
	 * source-to-screen transform, e.g., to change the zoom along with the
	 * canvas size.
	 *
	 * @param width
	 *            the new canvas width.
	 * @param height
	 *            the new canvas height.
	 * @param updateTransform
	 *            whether the current source-to-screen transform should be
	 *            updated. This will be <code>false</code> for the initial
	 *            update of a new {@link TransformEventHandler} and
	 *            <code>true</code> on subsequent calls. If <code>true</code>,
	 *            an update to its {@link TransformListener} should be
	 *            triggered.
	 */
	public void setCanvasSize( final int width, final int height, final boolean updateTransform );

	/**
	 * Set the {@link TransformListener} who will receive updated
	 * transformations.
	 *
	 * @param transformListener
	 *            will receive transformation updates.
	 */
	public void setTransformListener( TransformListener< A > transformListener );

	/**
	 * Get description of how mouse and keyboard actions map to transformations.
	 */
	public String getHelpString();
}
