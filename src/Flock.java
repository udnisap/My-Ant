
import java.util.ArrayList;
import java.util.Set;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Rumal
 */
public class Flock {
    Tile leader;
    Set<Tile> folowers;
    int size;
    ArrayList<Tile> path = new ArrayList<Tile>();

    public Flock(Tile leader, Set<Tile> folowers) {
        this.leader = leader;
        this.folowers = folowers;
        this.size = folowers.size();
    }
    
    
    
    
}
