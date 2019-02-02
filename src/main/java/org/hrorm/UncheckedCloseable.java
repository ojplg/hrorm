package org.hrorm;

/**
 * Credit: https://stackoverflow.com/questions/32209248
 * Basically, when streaming we want to tie AutoCloseables to the lifecycle of the Stream.
 * This allows us to close() ResultSet and PreparedStatement with Stream().onClose().
  */

interface UncheckedCloseable extends Runnable, AutoCloseable {
    default void run() {
        try { close(); } catch(Exception ex) { throw new RuntimeException(ex); }
    }
    static UncheckedCloseable wrap(AutoCloseable c) {
        return c::close;
    }
    default UncheckedCloseable nest(AutoCloseable c) {
        return ()->{ try(UncheckedCloseable c1=this) { c.close(); } };
    }
}