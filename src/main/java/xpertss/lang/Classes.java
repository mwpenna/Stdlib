package xpertss.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A set of utility methods for {@link Class <code>Class</code>es} and class names.
 */
@SuppressWarnings("UnusedDeclaration")
public final class Classes {

   private Classes() { }


   /**
    * Convert a Class into a Class[] Array. This is useful for using in Reflection
    * operations where the method signature arguments are defined as an array and
    * its such a pain in the butt to create an array of one all the time over and
    * over again. If the supplied argument is null this will return a zero length
    * array.
    */
   public static Class<?>[] toArray(Class<?> ... cls)
   {
      // This works because it is a concrete type. Had it been generic it would have failed
      return cls;
   }

   /**
    * Returns an empty array if the specified data array is {@code null}, otherwise
    * it returns the data array.

    * @param data The data array
    * @return the data array if not {@code null} or a zero length array
    */
   public static Class<?>[] emptyIfNull(Class<?>[] data)
   {
      return (data == null) ? new Class<?>[0] : data;
   }





   /**
    * This takes a Class object that is an array type and returns a String
    * representation of that array as if you saw it in a source file.
    */
   public static String getArrayClassType(Class arrayClass)
   {
      StringBuilder array = new StringBuilder();
      String type;
      for (type = arrayClass.getName(); type.startsWith("["); type = type.substring(1))
         array.append("[]");

      switch (type.charAt(0)) {
         case 66: // 'B'
            array.insert(0, "byte");
            break;

         case 73: // 'I'
            array.insert(0, "int");
            break;

         case 74: // 'J'
            array.insert(0, "long");
            break;

         case 70: // 'F'
            array.insert(0, "float");
            break;

         case 68: // 'D'
            array.insert(0, "double");
            break;

         case 67: // 'C'
            array.insert(0, "char");
            break;

         case 83: // 'S'
            array.insert(0, "short");
            break;

         case 90: // 'Z'
            array.insert(0, "boolean");
            break;

         case 76: // 'L'
            array.insert(0, type.substring(1, type.length() - 1));
            break;
      }
      return array.toString();
   }




   /**
    * Utility method to get a field inside of the specified class or its superclass
    * without throwing an exception. This will return null if the field is not
    * found.
    */
   public static Field getField(Class cls, String name)
   {
      while (cls != null) {
         try {
            return cls.getDeclaredField(name);
         } catch (NoSuchFieldException noSuch) {
            cls = cls.getSuperclass();
         }
      }
      return null;
   }

   /**
    * Get the specified method from the specified class or one of its super classes
    * without throwing an exception. This will return null if the method is not
    * found.
    */
   public static Method getMethod(Class<?> cls, String name, Class ... parameterTypes)
   {
      while (cls != null) {
         try {
            return cls.getDeclaredMethod(name, parameterTypes);
         } catch (NoSuchMethodException noSuch) {
            cls = cls.getSuperclass();
         }
      }
      return null;
   }

   /**
    * Get the constructor from the specified class that has the specified
    * argument set. This will return null if no constructor with the
    * specified argument set exists or if some exception is thrown trying
    * to access it.
    */
   public static <T> Constructor<T> getConstructor(Class<T> cls, Class ... parameterTypes)
   {
      try {
         return cls.getConstructor(parameterTypes);
      } catch(Exception ex) { return null; }
   }




   /**
    * Create a new instance of the specified class. If any errors occur trying
    * to create this new instance then this method will return null.
    */
   public static <T> T newInstance(Class<T> cls)
   {
      try {
         return cls.newInstance();
      } catch(Exception ex) { return null; }
   }


   /**
    * Construct a new instance of a class using the specified constructor and with the
    * specified initArguments. If any exception is encountered trying to fulfill this
    * request null will be returned.
    */
   public static <T> T newInstance(Constructor<T> con, Object ... args)
   {
      try {
         return con.newInstance(args);
      } catch(Exception ex) { return null; }
   }


   /**
    * This method returns the specified class as a <code>Class</code> object. If the
    * class can not be loaded using the specified class loader then {@code null} will
    * be returned.
    * <p>
    * The class loader must be specified for this to work in all contexts. In most
    * cases it is <tt>getClass().getClassLoader()</tt>
    *
    * @param loader The class loader to use to load the class.
    * @param clsName The class name as a string to load
    * @return The loaded Class object or null.
    */
   public static Class<?> loadNamedClass(ClassLoader loader, String clsName)
   {
      try {
         return Class.forName(clsName, true, loader);
      } catch (Exception ex) { return null; }
   }




   /**
    * Checks to see if a class is a sub class of another class.
    *
    * @param subClass The class to check
    * @param superClass The class you think sub may be.
    * @return {@code True} if sub is a sub class of sup
    * @throws NullPointerException If either subClass or superClass are {@code null}
    */
   public static boolean isSubclassOf(Class subClass, Class superClass)
   {
      Class sup;
      if(Objects.notNull(subClass, "subClass") == Objects.notNull(superClass, "superClass")) return true;
      for(sup = subClass.getSuperclass(); sup != null && sup != superClass;) sup = sup.getSuperclass();
      return sup != null;
   }
   




   /**
    * Gets a List of all interfaces implemented by the given class and its superclasses.
    */
   public static List<Class<?>> getAllInterfaces(Class<?> cls)
   {
      LinkedHashSet<Class<?>> result = new LinkedHashSet<>();
      while(cls != null) {
         Class<?>[] interfaces = cls.getInterfaces();
         Collections.addAll(result, interfaces);
         cls = cls.getSuperclass();
      }
      return new ArrayList<>(result);
   }

   /**
    * Gets a List of superclasses for the given class.
    */
   public static List<Class<?>> getAllSuperclasses(Class<?> cls)
   {
      List<Class<?>> classes = new ArrayList<>();
      if(cls != null) {
         Class<?> superclass = cls.getSuperclass();
         while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
         }
      }
      return classes;
   }


   /**
    * Checks if the specified object is an instance of or a subclass of one of the
    * specified types. Returns true if it is, false otherwise.
    *
    * @param obj The object to check ancestry on
    * @param types The types that may be the class' ancestor.
    * @return true if the class is an instance of, or a subclass of one of the
    *          specified types, false otherwise.
    * @throws NullPointerException If {@code types} is {@code null}
    */
   public static boolean isInstanceOf(Object obj, Class<?> ... types)
   {
      if(obj == null) return false;
      for(Class<?> type : Objects.notNull(types)) {
         if(type.isInstance(obj)) return true;
      }
      return false;
   }


   /**
    * Determines if the class or interface represented by the first argument is either
    * the same as, or is a super class or super interface of, the class or interface
    * represented by the second argument. It returns {@code true} if so; otherwise it
    * returns {@code false}.
    * <p/>
    * If the first argument represents a primitive type, this method returns {@code true}
    * if the second argument is exactly the same class object; otherwise it returns
    * {@code false}.
    * <p/>
    * If either of the arguments is {@code null} then {@code false} will be returned.
    *
    * @param toClass The class to be assigned
    * @param cls The class to be checked
    * @return {@code true} if cls can be assigned to toClass, {@code false} otherwise
    */
   public static boolean isAssignableFrom(Class<?> toClass, Class<?> cls)
   {
      return !(toClass == null || cls == null) && toClass.isAssignableFrom(cls);
   }


   /**
    * Null safe method to determine if the specified class is an inner class or static
    * nested class.
    *
    * @param cls  the class to check, may be {@code null}
    * @return {@code true} if the class is an inner or static nested class, {@code false}
    *    if not or {@code null}
    */
   public static boolean isInnerClass(Class<?> cls)
   {
      return cls != null && cls.getEnclosingClass() != null;
   }




   /**
    * Returns the array's component type if the given class is an array type or the
    * given class if not.
    *
    * @param cls The class to inspect
    * @return The component type if the class is an array, otherwise the class itself
    * @throws NullPointerException If the given class is {@code null}
    */
   public static Class<?> getComponentType(Class<?> cls)
   {
      return (cls.isArray()) ? cls.getComponentType() : cls;
   }

}