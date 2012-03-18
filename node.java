
import java.util.ArrayList;
import java.util.TreeSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rumal
 */
public class node {

    Tile current;
    TreeSet<Tile> accessibles = new TreeSet<Tile>();
    boolean unvisited = true;

    public node(Tile current) {
        this.current = current;
    }

    public TreeSet<Tile> getAccessibles() {
        return accessibles;
    }
    
    public void addAccessible(Tile t){
        accessibles.add(t);
    }
}
