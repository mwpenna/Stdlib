package xpertss.time;

/**
 * A time provider that can be used to obtain clock times and a high resolution
 * nano time.
 * <p>
 * This tool enables better testing by allowing a time provider to be stubbed
 * at runtime.
 *
 * User: cfloersch
 * Date: 12/18/12
 */
public abstract class TimeProvider {

   private static final InheritableThreadLocal<TimeProvider> cache = new InheritableThreadLocal<>();
   private static final TimeProvider system = new SystemTimeProvider();


   /**
    * @return The current time in milliseconds.
    * @see System#currentTimeMillis()
    */
   public abstract long milliTime();

   /**
    * @return The current value of the nanosecond timer.
    * @see System#nanoTime()
    */
   public abstract long nanoTime();


   /**
    * Used to provide a TimeProvider stub that will be returned by the calling
    * thread and all of its child threads. This allows an alternative to the
    * system time provider to be used within the context of a unit test where
    * precise times are needed for testing.
    */
   public static void stub(TimeProvider stub)
   {
      cache.set(stub);
   }

   /**
    * Get the default time provider for the calling thread. If a stub has not
    * been defined this will return the system time provider.
    */
   public static TimeProvider get()
   {
      TimeProvider stub = cache.get();
      return (stub == null) ? system : stub;
   }


}
