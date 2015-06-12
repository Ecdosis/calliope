/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.handler.post.importer;

/**
 *
 * @author desmond
 */
public class StandoffPair {
    public String text;
    public String stil;
    public String vid;
    StandoffPair( String stil, String text, String vid )
    {
        this.stil = stil;
        this.text = text;
        this.vid = vid;
    }
}
