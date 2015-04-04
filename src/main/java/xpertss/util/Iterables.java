package xpertss.util;

import xpertss.function.Consumer;
import xpertss.function.Predicate;
import xpertss.lang.Objects;

import java.util.Comparator;
import java.util.Iterator;

/**
 * This class contains static utility methods that operate on objects of type
 * {@code Iterable}.
 */
public final class Iterables {

   private Iterables() { }

   /**
    * Returns an ordering that orders two {@link Iterable}s lexicographically. That
    * is, it orders them, using {@link Comparable#compareTo(Object)}, the first pair
    * of values that follow any common prefix, or when one iterable is a prefix of
    * the other, treats the shorter iterable as the lesser.
    * <p/>
    * For example, {@code [] < [1] < [1, 1] < [1, 2] < [2]}.
    * <p/>
    * This ordering does not support {@code null} elements.
    *
    * @see <a href="http://en.wikipedia.org/wiki/Lexicographical_order">
    *     Lexicographical order article at Wikipedia</a>
    */
   public static <T extends Comparable<? super T>> Ordering<Iterable<T>> ordering()
   {
      return Objects.cast(LexicographicalOrdering.INSTANCE);
   }

   private static class LexicographicalOrdering extends Ordering<Iterable<Comparable>> {
      private static final Ordering<Iterable<Comparable>> INSTANCE = new LexicographicalOrdering();

      @Override public int compare(Iterable<Comparable> leftIterable, Iterable<Comparable> rightIterable)
      {
         Iterator<Comparable> left = leftIterable.iterator();
         Iterator<Comparable> right = rightIterable.iterator();
         while (left.hasNext()) {
            if (!right.hasNext()) return 1; // because it's longer
            @SuppressWarnings("unchecked") int result = left.next().compareTo(right.next());
            if (result != 0) return result;
         }
         if (right.hasNext()) return -1; // because it's longer
         return 0;
      }
   }

   /**
    * Returns an ordering that orders two {@link Iterable}s according to the rules
    * of the given {@link Comparator}.
    * <p/>
    * It orders two {@link Iterable}s using {@link Comparator#compare(Object,Object)}
    * on each element. If the two iterables share a common prefix then the iterable
    * with fewer elements is considered to be less than one with more elements.
    *
    * @param comparator The comparator to apply to the iterable's elements
    * @throws NullPointerException If the comparator is {@code null}
    */
   public static <T> Ordering<Iterable<T>> ordering(Comparator<T> comparator)
   {
      final Ordering<T> ordering = Ordering.from(Objects.notNull(comparator));
      return new Ordering<Iterable<T>>() {
         @Override public int compare(Iterable<T> leftIterable, Iterable<T> rightIterable)
         {
            Iterator<T> left = leftIterable.iterator();
            Iterator<T> right = rightIterable.iterator();
            while (left.hasNext()) {
               if (!right.hasNext()) return 1; // because it's longer
               int result = ordering.compare(left.next(), right.next());
               if (result != 0) return result;
            }
            if (right.hasNext()) return -1; // because it's longer
            return 0;
         }
      };
   }

   /**
    * Performs the given action for each element of the {@link Iterable} until
    * all elements have been processed or the action throws an exception. Unless
    * otherwise specified by the implementing class, actions are performed in the
    * order of iteration (if an iteration order is specified). Exceptions thrown
    * by the action are relayed to the caller.
    *
    * @param items The iterable to iterate
    * @param action The action to be performed for each element
    * @throws NullPointerException If either items or action are {@code null}
    */
   public static <T> void forEach(Iterable<T> items, Consumer<? super T> action)
   {
      for(T item : items) action.apply(item);
   }




   /**
    * Returns {@code true} if one or more elements in {@code iterable} satisfy
    * the predicate.
    *
    * @throws NullPointerException If iterable or predicate are {@code null}
    */
   public static <T> boolean any(Iterable<T> iterable, Predicate<? super T> predicate)
   {
      Objects.notNull(predicate, "predicate");
      for(T item : Objects.notNull(iterable, "iterable")) {
         if(predicate.apply(item)) return true;
      }
      return false;
   }

   /**
    * Returns {@code true} if every element in {@code iterable} satisfies the
    * predicate. If {@code iterable} is empty, {@code true} is returned.
    *
    * @throws NullPointerException If iterable or predicate are {@code null}
    */
   public static <T> boolean all(Iterable<T> iterable, Predicate<? super T> predicate)
   {
      Objects.notNull(predicate, "predicate");
      for(T item : Objects.notNull(iterable, "iterable")) {
         if(!predicate.apply(item)) return false;
      }
      return true;
   }

   /**
    * Returns {@code true} if none of the elements in {@code iterable} satisfy
    * the predicate. If {@code iterable} is empty, {@code true} is returned.
    *
    * @throws NullPointerException If iterable or predicate are {@code null}
    */
   public static <T> boolean none(Iterable<T> iterable, Predicate<? super T> predicate)
   {
      Objects.notNull(predicate, "predicate");
      for(T item : Objects.notNull(iterable, "iterable")) {
         if(predicate.apply(item)) return false;
      }
      return true;
   }

}