/**
 * Created By: cfloersch
 * Date: 1/10/14
 * Copyright 2013 XpertSoftware
 */
package xpertss.function;

import xpertss.lang.Classes;
import xpertss.lang.Objects;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Static utility methods pertaining to Function instances.
 *
 * @see Function
 * @see BinaryFunction
 */
public final class Functions {

   private Functions() { }


   /**
    * A function that returns {@link Object#toString} of its argument. Note that this
    * function does not support {@code null} inputs and will throw a {@link
    * NullPointerException} when applied to {@code null}.
    */
   @SuppressWarnings("unchecked")
   public static <E> Function<E,String> string()
   {
      return Objects.cast(ToStringFunction.INSTANCE);
   }

   private enum ToStringFunction implements Function<Object,String> {
      INSTANCE;
      @Override public String apply(Object input)
      {
         return input.toString();
      }
   }






   /**
    * Returns a function which performs key-to-value lookup on {@code map}.
    * <p/>
    * The difference between a map and a function is that a map is defined on a set of
    * keys, while a function is defined on a type.  The function built by this method
    * returns {@code null} for all its arguments that do not belong to the map's keyset.
    *
    * @param map Source map
    * @return Function that returns map.get(a) for each a
    * @throws NullPointerException If map is {@code null}
    */
   public static <A,B> Function<A,B> forMap(Map<A,B> map)
   {
      final Map<A,B> delegate = Objects.notNull(map);
      return new Function<A,B>() {
         public B apply(A a)
         {
            return delegate.get(a);
         }
      };
   }





   /**
    * Returns a composition {@code outer<sup>0</sup>inner : A->C} of two functions, {@code
    * inner: A->B} and {@code outer: B->C}. Composition is defined as a function h such that
    * h(x) = outer(inner(x)) for each x.
    *
    * @see <a href="//en.wikipedia.org/wiki/Function_composition">function composition</a>
    *
    * @param inner the inner function
    * @param outer the outer function
    * @return Function&lt;A->C> composition of inner and outer
    */
   public static <A,B,C> Function<A,C> compose(Function<A,? extends B> inner, Function<B,C> outer)
   {
      final Function<B,C> funcG = Objects.notNull(outer);
      final Function<A,? extends B> funcF = Objects.notNull(inner);
      return new Function<A,C> () {
         public C apply(A a)
         {
            return funcG.apply(funcF.apply(a));
         }
      };
   }







   /**
    * Returns a composition {@code outer<sup>0</sup>inner : T,U->V} of two functions, {@code
    * inner: T,U->R} and {@code outer: R->V}. Composition is defined as a function h such that
    * h(x,y) = outer(inner(x,y)) for each x and y pair.
    *
    * @see <a href="//en.wikipedia.org/wiki/Function_composition">function composition</a>
    *
    * @param inner the inner function
    * @param outer the outer function
    * @return Function&lt;T,U->V> composition of inner and outer
    */
   public static <T, R, U, V> BinaryFunction<T, U, V> compose(BinaryFunction<T, U, R> inner, Function<? super R, ? extends V> outer)
   {
      final BinaryFunction<T,U,R> binary = Objects.notNull(inner);
      final Function<? super R, ? extends V> func = Objects.notNull(outer);
      return new BinaryFunction<T, U, V>() {
         @Override
         public V apply(T first, U second) {
            return func.apply(binary.apply(first, second));
         }
      };
   }







   private static final Class[] uniLockers = { SyncSafeFunction.class, LockSafeFunction.class };
   private static final Class[] biLockers = { SyncSafeBinaryFunction.class, LockSafeBinaryFunction.class };

   /**
    * Returns a function which guarantees that the delegate's {@link Function#apply(Object)}
    * method will be called by only a single thread at a time, making it thread-safe. This
    * implementation will synchronizes on the delegate before calling it.
    * <p/>
    * Using traditional synchronization is suitable where very little contention exists. As
    * lock contention goes up it scales poorly.
    *
    * @throws NullPointerException if the specified {@code delegate} is {@code null}
    * @see #lock(Function)
    */
   public static <F,T> Function<F,T> synchronize(Function<F,T> delegate)
   {
      if(Classes.isInstanceOf(delegate, uniLockers)) return delegate;
      return new SyncSafeFunction<>(delegate);
   }

   private static class SyncSafeFunction<F,T> implements Function<F,T>, Serializable {
      final Function<F,T> delegate;

      SyncSafeFunction(Function<F,T> delegate)
      {
         this.delegate = Objects.notNull(delegate);
      }
      public T apply(F item)
      {
         synchronized (delegate) {
            return delegate.apply(item);
         }
      }
   }


   /**
    * Returns a binary function that guarantees that the delegate's {@link BinaryFunction
    * #apply(Object,Object)} method will be called by only a single thread at a time, making
    * it thread-safe. This implementation will synchronizes on the delegate before calling it.
    * <p/>
    * Using traditional synchronization is suitable where very little contention exists. As
    * lock contention goes up it scales poorly.
    *
    * @throws NullPointerException if the specified {@code delegate} is {@code null}
    * @see #lock(BinaryFunction)
    */
   public static <T,U,R> BinaryFunction<T,U,R> synchronize(BinaryFunction<T,U,R> delegate)
   {
      if(Classes.isInstanceOf(delegate, biLockers)) return delegate;
      return new SyncSafeBinaryFunction<>(delegate);
   }

   private static class SyncSafeBinaryFunction<T,U,R> implements BinaryFunction<T,U,R>, Serializable {
      final BinaryFunction<T,U,R> delegate;

      SyncSafeBinaryFunction(BinaryFunction<T,U,R> delegate)
      {
         this.delegate = Objects.notNull(delegate);
      }
      public R apply(T left, U right)
      {
         synchronized (delegate) {
            return delegate.apply(left, right);
         }
      }
   }


   /**
    * Returns a function which guarantees that the delegate's {@link Function#apply(Object)}
    * method will be called by only a single thread at a time, making it thread-safe.  This
    * implementation  will acquire a  lock {@link java.util.concurrent.locks.ReentrantLock}
    * before calling the delegate.
    * <p/>
    * This implementation is very similar to the synchronized variant at low contention
    * levels even though it is less consistent.  However, at higher contention levels it
    * performs slightly better.
    *
    * @throws NullPointerException if the specified {@code delegate} is {@code null}
    * @see #synchronize(Function)
    */
   public static <F,T> Function<F,T> lock(Function<F,T> delegate)
   {
      if(Classes.isInstanceOf(delegate, uniLockers)) return delegate;
      return new LockSafeFunction<>(delegate);
   }

   private static class LockSafeFunction<F,T> implements Function<F,T>, Serializable {
      final ReentrantLock lock = new ReentrantLock();
      final Function<F,T> delegate;

      LockSafeFunction(Function<F,T> delegate)
      {
         this.delegate = Objects.notNull(delegate);
      }
      public T apply(F item)
      {
         try {
            lock.lock();
            return delegate.apply(item);
         } finally {
            lock.unlock();
         }
      }
   }


   /**
    * Returns a binary function which guarantees that the delegate's {@link BinaryFunction
    * #apply(Object,Object)} method will be called by only a single thread at a time, making
    * it thread-safe.  This implementation  will acquire a lock
    * {@link java.util.concurrent.locks.ReentrantLock} before calling the delegate.
    * <p/>
    * This implementation is very similar to the synchronized variant at low contention
    * levels even though it is less consistent.  However, at higher contention levels it
    * performs slightly better.
    *
    * @throws NullPointerException if the specified {@code delegate} is {@code null}
    * @see #synchronize(BinaryFunction)
    */
   public static <T,U,R> BinaryFunction<T,U,R> lock(BinaryFunction<T,U,R> delegate)
   {
      if(Classes.isInstanceOf(delegate, biLockers)) return delegate;
      return new LockSafeBinaryFunction<>(delegate);
   }

   private static class LockSafeBinaryFunction<T,U,R> implements BinaryFunction<T,U,R>, Serializable {
      final ReentrantLock lock = new ReentrantLock();
      final BinaryFunction<T,U,R> delegate;

      LockSafeBinaryFunction(BinaryFunction<T,U,R> delegate)
      {
         this.delegate = Objects.notNull(delegate);
      }
      public R apply(T left, U right)
      {
         try {
            lock.lock();
            return delegate.apply(left, right);
         } finally {
            lock.unlock();
         }
      }
   }


}
