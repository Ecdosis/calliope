/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.search;

public class HitProfile
{
    public int numHits;
    public int from;
    public int to;
    public HitProfile( int from, int to )
    {
        this.from = from;
        this.to = to;
    }
}