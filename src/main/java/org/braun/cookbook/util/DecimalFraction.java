package org.braun.cookbook.util;

/*
 * Created on Mar 26, 2003
 *
 */
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * @author mbraun The DecimalFraction class provides basic functions for
 * converting an Double given in fraction, for example 1 1/4, to the primitive
 * formats double, float, int and long.
 */
public class DecimalFraction extends Number implements Comparable<DecimalFraction> {

   /**
    *
    */
   private static final long serialVersionUID = -8350047744763237320L;
   public static final DecimalFormat df = new DecimalFormat("#");
   double val;
   static final char Fraction_One_Fourth = 0x00BC;
   static final char Fraction_One_Half = 0x00BD;
   static final char Fraction_Three_Fourths = 0x00BE;
   static final char Fraction_One_Seventh = 0x2150;
   static final char Fraction_One_Ninth = 0x2151;
   static final char Fraction_One_Tenth = 0x2152;
   static final char Fraction_One_Third = 0x2153;
   static final char Fraction_Two_Thirds = 0x2154;
   static final char Fraction_One_Fifth = 0x2155;
   static final char Fraction_Two_Fifths = 0x2156;
   static final char Fraction_Three_Fifths = 0x2157;
   static final char Fraction_Four_Fifths = 0x2158;
   static final char Fraction_One_Sixth = 0x2159;
   static final char Fraction_Five_Sixths = 0x215A;
   static final char Fraction_One_Eighth = 0x215B;
   static final char Fraction_Three_Eighths = 0x215C;
   static final char Fraction_Five_Eighths = 0x215D;
   static final char Fraction_Seven_Eighths = 0x215E;

   /**
    *
    */
   public DecimalFraction() {
      super();
      val = 0;
   }

   public DecimalFraction(double val) {
      this.val = val;
   }

   public DecimalFraction(String value) throws NumberFormatException {
      val = parseDecimalFraction(value);
   }

   @Override
   public int compareTo(DecimalFraction anotherDecimalFraction) {
      return Double.compare(val, anotherDecimalFraction.doubleValue());
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 59 * hash + (int) (Double.doubleToLongBits(this.val) ^ (Double.doubleToLongBits(this.val) >>> 32));
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (!(obj instanceof DecimalFraction)) {
         return false;
      }
      return val == ((DecimalFraction) obj).doubleValue();
   }

   /**
    * Returns the value of this DecimalFraction as a int (by casting to type
    * int)
    *
    * @see java.lang.Number#intValue()
    */
   @Override
   public int intValue() {
      return (int) val;
   }

   /*
    * (non-Javadoc) @see java.lang.Number#longValue()
    */
   @Override
   public long longValue() {
      return (long) val;
   }

   /*
    * (non-Javadoc) @see java.lang.Number#floatValue()
    */
   @Override
   public float floatValue() {
      return (float) val;
   }

   /*
    * (non-Javadoc) @see java.lang.Number#doubleValue()
    */
   @Override
   public double doubleValue() {
      return val;
   }

   /**
    * <p>Returns a new double initialized to the value represented by the
    * specified String, as performed by the valueOf method of class
    * DecimalFraction.</p> <p>Throws
    * <code>NumberFormatException</code> if value to parse is
    * <code>null</code>.
    *
    * @param s
    * @return double
    * @throws NumberFormatException - if the string does not contain a parsable
    * fraction.
    */
   public static double parseDecimalFraction(String s) throws NumberFormatException {
      if (s == null) {
         throw new NullPointerException(s);
      }
      String tmp = s.trim();
      if (s.length() == 1) {
         switch (s.charAt(0)) {
            case Fraction_One_Fourth:
               return 0.25; // 1/4
            case Fraction_One_Half:
               return 0.5; // 1/2
            case Fraction_Three_Fourths:
               return 0.75; // 3/4
            case Fraction_One_Seventh:
               return 1.0 / 7; // 1/7
            case Fraction_One_Ninth:
               return 1.0 / 9; // 1/9
            case Fraction_One_Tenth:
               return 0.1; // 1/10
            case Fraction_One_Third:
               return 1.0 / 3; // 1/3
            case Fraction_Two_Thirds:
               return 2.0 / 3; // 2/3
            case Fraction_One_Fifth:
               return 0.2; // 1/5
            case Fraction_Two_Fifths:
               return 0.4; // 2/5
            case Fraction_Three_Fifths:
               return 0.6; // 3/5
            case Fraction_Four_Fifths:
               return 0.8; // 4/5
            case Fraction_One_Sixth:
               return 1.0 / 6; // 1/6
            case Fraction_Five_Sixths:
               return 5.0 / 6; // 5/6
            case Fraction_One_Eighth:
               return 0.125; // 1/8
            case Fraction_Three_Eighths:
               return 0.375; // 3/8
            case Fraction_Five_Eighths:
               return 0.625; // 5/8
            case Fraction_Seven_Eighths:
               return 0.875; // 7/8
            default:
         }
      }

      int i = tmp.indexOf('/');
      if (i < 0) {
         return Double.parseDouble(s);
      }

      long l = 0;

      long fraction = Long.parseLong(tmp.substring(i + 1));
      int j = tmp.indexOf(' ');
      long decimal;
      if (j < 0) {
         decimal = Long.parseLong(tmp.substring(0, i));
      } else {
         decimal = Long.parseLong(tmp.substring(j + 1, i));
         l = Long.parseLong(tmp.substring(0, j));
      }

      return (double) l + (double) decimal / fraction;
   }

   /**
    * Returns a DecimalFraction object holding the double value represented by
    * the argument string s.
    *
    * @param s
    * @return DecimalFraction
    */
   public static DecimalFraction valueOf(String s) {
      return new DecimalFraction(s);
   }

   /**
    * Returns a string representation of this DecimalFraction object.
    *
    * @return String
    */
   @Override
   public String toString() {
      return toString(val);
   }

   public static String toString(String value, int length) {
      StringBuilder sb = new StringBuilder(toString(value));
      if (sb.length() < length) {
         for (int i = sb.length(); i < length; i++) {
            sb.append(' ');
         }
      }
      return sb.toString();
   }

   public static String toString(String value) {
      if (value == null || value.length() == 0) {
         return "";
      }
      try {
         return toString(parseDecimalFraction(value));
      } catch (NumberFormatException e) {
         return value;
      }
   }

   /**
    * Returns a string representation of an double formatted as an decimal
    * fraction.
    *
    * @param d
    * @return String
    */
   public static String toString(double d) {
      long l = (long) d;
      int k = (int) (1000 * (d - l));
      if (k == 0) {
         return Long.toString(l);
      } else if (l == 0) {
         switch (k) {
            case 125:
               return "1/8";
            case 250:
               return "1/4";
            case 375:
               return "3/8";
            case 500:
               return "1/2";
            case 625:
               return "5/8";
            case 750:
               return "3/4";
            case 333:
            case 334:
               return "1/3";
            case 666:
            case 667:
               return "2/3";
            case 875:
               return "7/8";
         }
      }
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator('.');
      DecimalFormat doubleformat = new DecimalFormat("#.###", dfs);
      return doubleformat.format(d);
   }

}
