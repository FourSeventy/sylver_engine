/**
 * This file is part of Multigraph, which is multipurpose java
 * software for interactive scientific graphics.
 *
 * Copyright (c) 2004 Mark Phillips
 * 
 * Multigraph is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * Multigraph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multigraph in a file called COPYING; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA, or see http://www.fsf.org.
 **/

package com.silvergobletgames.sylver.util;
/**
 * Handle linear interpolation of a single floating
 * point (double) variable.
 *
 * <p>
 *
 * In computer graphics and other mathematical software there is often
 * a need to interpolate one range of values to another.  This class
 * encapsulates the mathematics for that simple but very common
 * process, providing several convenient methods for performing the
 * interpolation and related operations.
 *
 * <p>
 *
 * The general idea is that you create a LinearInterpolator instance
 * by specifying domain endpoints [a,b] and range endpoints [A,B].
 * That creates an interpolator which takes a to A and b
 * to B.  You then call its {@link #interp(double) interp} method to
 * perform the interpolation as many times as you want.
 *
 * <p>
 *
 * There are also several other methods for performing computations
 * related to the interpolation.
 *
 * @author Mark Phillips
 * @version $Id: LinearInterpolator.java,v 1.1.1.1 2004/11/30 22:30:41 mphillips Exp $
 */
public class LinearInterpolator {

    private double A, B, a, b, f;


    /**
     * Create a linear interpolator which maps [a,b] to [A,B]
     */
    public LinearInterpolator(double A, double B,
                              double a, double b) {
        this.A = A;  this.B = B;
        this.a = a;  this.b = b;
        this.f = (B - A) / (b - a);
    }

    /**
     * Create a linear interpolator which maps [a,b] to [A,B]
     */
    public LinearInterpolator(long A, long B,
                              double a, double b) {
        this((double)A, (double)B, a, b);
    }

    /**
     * Create a linear interpolator which maps [a,b] to [A,B]
     */
    public LinearInterpolator(double A, double B,
                              long a, long b) {
        this(A, B, (double)a, (double)b);
    }

    /**
     * Create a linear interpolator which maps [a,b] to [A,B]
     */
    public LinearInterpolator(long A, long B,
                              long a, long b) {
        this((double)A, (double)B, (double)a, (double)b);
    }

    /**
     * Return a the inverse linear interpolator.  I.e.  an
     * interpolator that maps [A,B] to [a,b], where A,B,a,b are the
     * value used to create this interpolator.
     */
    public LinearInterpolator inverse() {
        return new LinearInterpolator(a, b, A, B);
    }

    /**
     * Return the interpolated value corresponding to x.
     */
    public double interp(double x) {
        if(x >= b)
            return B;
        else
           return A + (x - a) * f;
    }

    /**
     * Return the interpolated value corresponding to x.
     */
    public double interp(long x) {
        if(x >= b)
            return B;
        else
            return A + ((double)x - a) * f;
    }

    /**
     * Perform the scaling part of this interpolation.
     * Omit the offset part.
     */
    public double scale(double x) {
        return x * f;
    }

    /**
     * Perform the scaling part of this interpolation.
     * Omit the offset part.
     */
    public double scale(long x) {
        return ((double)x) * f;
    }

    /**
     * Return an interpolated value
     * rounded off to the nearest integer.
     */
    public long iInterp(double x)
    {
        return (long)Math.round(interp(x));
    }

    /**
     * Return an interpolated value
     * rounded off to the nearest integer.
     */
    public long iInterp(long x)
    {
        return (long)Math.round(interp(x));
    }

    /**
     * Return the scaling part of this interpolation, rounded off
     * to the nearest integer.
     */
    public long iScale(long x)
    {
        return (long)Math.round(scale(x));
    }

    /**
     * Return the left endpoint of the domain interval for
     * this interpolator.  This is the value of 'a' that
     * was passed to its constructor.
     */
    public double geta() { return a; }

    /**
     * Return the right endpoint of the domain interval for
     * this interpolator.  This is the value of 'b' that
     * was passed to its constructor.
     */
    public double getb() { return b; }

    /**
     * Return the left endpoint of the range interval for
     * this interpolator.  This is the value of 'A' that
     * was passed to its constructor.
     */
    public double getA() { return A; }

    /**
     * Return the right endpoint of the range interval for
     * this interpolator.  This is the value of 'B' that
     * was passed to its constructor.
     */
    public double getB() { return B; }

    /**
     * Return a string representation of this interpolator.
     */
    public String toString() {
        return "LinearInterpolator[" + A + "," + B + "," + a + "," + b + "]";
    }
}
