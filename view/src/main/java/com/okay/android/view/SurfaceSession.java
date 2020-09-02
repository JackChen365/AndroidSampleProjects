//package com.okay.android.view;
//
//
///**
// * An instance of this class represents a connection to the surface
// * flinger, in which you can create one or more Surface instances that will
// * be composited to the screen.
// * {@hide}
// */
//public class SurfaceSession {
//    /** Create a new connection with the surface flinger. */
//    public SurfaceSession() {
//        init();
//    }
//
//    /** Forcibly detach native resources associated with this object.
//     *  Unlike destroy(), after this call any surfaces that were created
//     *  from the session will no longer work. The session itself is destroyed.
//     */
//    public native void kill();
//
//    /* no user serviceable parts here ... */
//    @Override
//    protected void finalize() throws Throwable {
//        destroy();
//    }
//
//    private native void init();
//    private native void destroy();
//
//    private int mClient;
//}
//
