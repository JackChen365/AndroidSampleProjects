package com.okay.android.view;

import android.graphics.Canvas;
import android.graphics.Rect;

import static android.view.WindowManager.LayoutParams.MEMORY_TYPE_NORMAL;
import static android.view.WindowManager.LayoutParams.MEMORY_TYPE_HARDWARE;
import static android.view.WindowManager.LayoutParams.MEMORY_TYPE_GPU;
import static android.view.WindowManager.LayoutParams.MEMORY_TYPE_PUSH_BUFFERS;

/**
 * Abstract interface to someone holding a display surface.  Allows you to
 * control the surface size and format, edit the pixels in the surface, and
 * monitor changes to the surface.  This interface is typically available
 * through the {@link android.view.SurfaceView} class.
 * 
 * <p>When using this interface from a thread different than the one running
 * its {@link android.view.SurfaceView}, you will want to carefully read the
 * {@link #lockCanvas} and {@link Callback#surfaceCreated Callback.surfaceCreated}.
 */
public interface SurfaceHolder {
    /**
     * Surface type.
     * 
     * @see #SURFACE_TYPE_NORMAL
     * @see #SURFACE_TYPE_HARDWARE
     * @see #SURFACE_TYPE_GPU
     * @see #SURFACE_TYPE_PUSH_BUFFERS
     */
    
    /** Surface type: creates a regular surface, usually in main, non
     * contiguous, cached/buffered RAM. */
    public static final int SURFACE_TYPE_NORMAL = MEMORY_TYPE_NORMAL;
    /** Surface type: creates a suited to be used with DMA engines and
     * hardware accelerators. */
    public static final int SURFACE_TYPE_HARDWARE = MEMORY_TYPE_HARDWARE;
    /** Surface type: creates a surface suited to be used with the GPU */
    public static final int SURFACE_TYPE_GPU = MEMORY_TYPE_GPU;
    /** Surface type: creates a "push" surface, that is a surface that 
     * doesn't owns its buffers. With such a surface lockCanvas will fail. */
    public static final int SURFACE_TYPE_PUSH_BUFFERS = MEMORY_TYPE_PUSH_BUFFERS;

    /**
     * Exception that is thrown from {@link #lockCanvas} when called on a Surface
     * whose is SURFACE_TYPE_PUSH_BUFFERS.
     */
    public static class BadSurfaceTypeException extends RuntimeException {
        public BadSurfaceTypeException() {
        }

        public BadSurfaceTypeException(String name) {
            super(name);
        }
    }

    /**
     * A client may implement this interface to receive information about
     * changes to the surface.  When used with a {@link android.view.SurfaceView}, the
     * Surface being held is only available between calls to
     * {@link #surfaceCreated(SurfaceHolder)} and
     * {@link #surfaceDestroyed(SurfaceHolder).  The Callback is set with
     * {@link SurfaceHolder#addCallback SurfaceHolder.addCallback} method.
     */
    public interface Callback {
        /**
         * This is called immediately after the surface is first created.
         * Implementations of this should start up whatever rendering code
         * they desire.  Note that only one thread can ever draw into
         * a {@link android.view.Surface}, so you should not draw into the Surface here
         * if your normal rendering will be in another thread.
         * 
         * @param holder The SurfaceHolder whose surface is being created.
         */
        public void surfaceCreated(SurfaceHolder holder);

        /**
         * This is called immediately after any structural changes (format or
         * size) have been made to the surface.  You should at this point update
         * the imagery in the surface.  This method is always called at least
         * once, after {@link #surfaceCreated}.
         * 
         * @param holder The SurfaceHolder whose surface has changed.
         * @param format The new PixelFormat of the surface.
         * @param width The new width of the surface.
         * @param height The new height of the surface.
         */
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height);

        /**
         * This is called immediately before a surface is being destroyed. After
         * returning from this call, you should no longer try to access this
         * surface.  If you have a rendering thread that directly accesses
         * the surface, you must ensure that thread is no longer touching the 
         * Surface before returning from this function.
         * 
         * @param holder The SurfaceHolder whose surface is being destroyed.
         */
        public void surfaceDestroyed(SurfaceHolder holder);
    }

    /**
     * Add a Callback interface for this holder.  There can several Callback
     * interfaces associated to a holder.
     * 
     * @param callback The new Callback interface.
     */
    public void addCallback(Callback callback);

    /**
     * Removes a previously added Callback interface from this holder.
     * 
     * @param callback The Callback interface to remove.
     */
    public void removeCallback(Callback callback);

    /**
     * Use this method to find out if the surface is in the process of being
     * created from Callback methods. This is intended to be used with
     * {@link Callback#surfaceChanged}.
     * 
     * @return true if the surface is in the process of being created.
     */
    public boolean isCreating();
    
    /**
     * Sets the surface's type. Surfaces intended to be used with OpenGL ES
     * should be of SURFACE_TYPE_GPU, surfaces accessed by DMA engines and
     * hardware accelerators should be of type SURFACE_TYPE_HARDWARE.
     * Failing to set the surface's type appropriately could result in 
     * degraded performance or failure. 
     * 
     * @param type The surface's memory type.
     */
    public void setType(int type);

    /**
     * Make the surface a fixed size.  It will never change from this size.
     * When working with a {link SurfaceView}, this must be called from the
     * same thread running the SurfaceView's window.
     * 
     * @param width The surface's width.
     * @param height The surface's height.
     */
    public void setFixedSize(int width, int height);

    /**
     * Allow the surface to resized based on layout of its container (this is
     * the default).  When this is enabled, you should monitor
     * {@link Callback#surfaceChanged} for changes to the size of the surface.
     * When working with a {link SurfaceView}, this must be called from the
     * same thread running the SurfaceView's window.
     */
    public void setSizeFromLayout();

    /**
     * Set the desired PixelFormat of the surface.  The default is OPAQUE.
     * When working with a {link SurfaceView}, this must be called from the
     * same thread running the SurfaceView's window.
     * 
     * @param format A constant from PixelFormat.
     * 
     * @see android.graphics.PixelFormat
     */
    public void setFormat(int format);

    /**
     * Enable or disable option to keep the screen turned on while this
     * surface is displayed.  The default is false, allowing it to turn off.
     * Enabling the option effectivelty.
     * This is safe to call from any thread.
     * 
     * @param screenOn Supply to true to force the screen to stay on, false
     * to allow it to turn off.
     */
    public void setKeepScreenOn(boolean screenOn);
    
    /**
     * Start editing the pixels in the surface.  The returned Canvas can be used
     * to draw into the surface's bitmap.  A null is returned if the surface has
     * not been created or otherwise can not be edited.  You will usually need
     * to implement {@link Callback#surfaceCreated Callback.surfaceCreated}
     * to find out when the Surface is available for use.
     * 
     * <p>The content of the Surface is never preserved between unlockCanvas() and
     * lockCanvas(), for this reason, every pixel within the Surface area
     * must be written. The only exception to this rule is when a dirty
     * rectangle is specified, in which case, non dirty pixels will be
     * preserved.
     * 
     * <p>If you call this repeatedly when the Surface is not ready (before
     * {@link Callback#surfaceCreated Callback.surfaceCreated} or after
     * {@link Callback#surfaceDestroyed Callback.surfaceDestroyed}), your calls
     * will be throttled to a slow rate in order to avoid consuming CPU.
     * 
     * <p>If null is not returned, this function internally holds a lock until
     * the corresponding {@link #unlockCanvasAndPost} call, preventing
     * {@link android.view.SurfaceView} from creating, destroying, or modifying the surface
     * while it is being drawn.  This can be more convenience than accessing
     * the Surface directly, as you do not need to do special synchronization
     * with a drawing thread in {@link Callback#surfaceDestroyed
     * Callback.surfaceDestroyed}.
     * 
     * @return Canvas Use to draw into the surface.
     */
    public Canvas lockCanvas();

    
    /**
     * Just like {@link #lockCanvas()} but allows to specify a dirty rectangle.
     * Every
     * pixel within that rectangle must be written; however pixels outside
     * the dirty rectangle will be preserved by the next call to lockCanvas().
     * 
     * @see android.view.SurfaceHolder#lockCanvas
     * 
     * @param dirty Area of the Surface that will be modified.
     * @return Canvas Use to draw into the surface.
     */
    public Canvas lockCanvas(Rect dirty);

    /**
     * Finish editing pixels in the surface.  After this call, the surface's
     * current pixels will be shown on the screen, but its content is lost,
     * in particular there is no guarantee that the content of the Surface
     * will remain unchanged when lockCanvas() is called again.
     * 
     * @see #lockCanvas()
     *
     * @param canvas The Canvas previously returned by lockCanvas().
     */
    public void unlockCanvasAndPost(Canvas canvas);

    /**
     * Retrieve the current size of the surface.  Note: do not modify the
     * returned Rect.  This is only safe to call from the thread of
     * {@link android.view.SurfaceView}'s window, or while inside of
     * {@link #lockCanvas()}.
     * 
     * @return Rect The surface's dimensions.  The left and top are always 0.
     */
    public Rect getSurfaceFrame();

    /**
     * Direct access to the surface object.  The Surface may not always be
     * available -- for example when using a {@link SurfaceView} the holder's
     * Surface is not created until the view has been attached to the window
     * manager and performed a layout in order to determine the dimensions
     * and screen position of the Surface.    You will thus usually need
     * to implement {@link Callback#surfaceCreated Callback.surfaceCreated}
     * to find out when the Surface is available for use.
     * 
     * <p>Note that if you directly access the Surface from another thread,
     * it is critical that you correctly implement
     * {@link Callback#surfaceCreated Callback.surfaceCreated} and
     * {@link Callback#surfaceDestroyed Callback.surfaceDestroyed} to ensure
     * that thread only accesses the Surface while it is valid, and that the
     * Surface does not get destroyed while the thread is using it.
     * 
     * <p>This method is intended to be used by frameworks which often need
     * direct access to the Surface object (usually to pass it to native code).
     * When designing APIs always use SurfaceHolder to pass surfaces around
     * as opposed to the Surface object itself. A rule of thumb is that
     * application code should never have to call this method.
     * 
     * @return Surface The surface.
     */
    public Surface getSurface();
}
