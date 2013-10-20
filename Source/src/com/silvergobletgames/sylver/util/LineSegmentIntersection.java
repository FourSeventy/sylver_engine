
package com.silvergobletgames.sylver.util;

/**
 *
 * @author Mike
 */
public class LineSegmentIntersection {
 
    public static boolean testLineSegmentIntersection(SylverVector2f one, SylverVector2f two, SylverVector2f three, SylverVector2f four)
    {
        //if these conditions are true they intersect
        //line segment 1 goes from P1 = (x1, y1) to P2 = (x2, y2)
        //line segment 2 goes from P3 = (x3, y3) to P4 = (x4, y4).
         //det(P1, P2, P3) * det(P1, P2, P4) < 0
        //det(P3, P4, P1) * det(P3, P4, P2) < 0
        
        //(x2 - x1)*(y3 - y1) - (x3 - x1)*(y2 - y1) = det(P1, P2, P3)
        //(x2 - x1)*(y4 - y1) - (x4 - x1)*(y2 - y1) = det(P1, P2, P4)
        //(x3 - x1)*(y4 - y1) - (x4 - x1)*(y3 - y1) = det(P3, P4, P1)
       //det(P1, P2, P3) - det(P1, P2, P4) + det(P3, P4, P1) = det(P3, P4, P2)
        
        float det123 =(two.x - one.x)*(three.y - one.y) - (three.x - one.x) *(two.y - one.y);
        float det124 = (two.x - one.x)*(four.y - one.y) - (four.x - one.x)*(two.y - one.y);
        float det341 = (three.x - one.x)*(four.y - one.y) - (four.x - one.x)*(three.y - one.y);
        float det342 = det123 - det124 - det341;
        
        if(det123 == 0 || det124 == 0 || det341 == 0 || det342 == 0)
            return false;
        
        if(det123 * det124 < 0 && det341 * det342 < 0)
            return true;
        else
            return false;
    }
}
