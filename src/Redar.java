
import java.util.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rumal
 */
public class Redar {

    static Ants ants = MyBot.ants;
    EnumMap<Ilk, TreeSet<Tile>> sur = new EnumMap<Ilk, TreeSet<Tile>>(Ilk.class);
    HashMap<Tile, Aim> directions = new HashMap<Tile, Aim>();
    HashMap<Tile, Integer> distance = new HashMap<Tile, Integer>();
    final Tile currTile;
    static HashMap<Tile, Integer> noOfThreats = new HashMap<Tile, Integer>();

    public Redar(Tile curr) {
        this.currTile = curr;

        sur.put(Ilk.DEAD, new TreeSet<Tile>(new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                if (distance.get(o1) == distance.get(o2)) {
                    return 1;
                }
                return distance.get(o1) - distance.get(o2);
            }
        }));
        sur.put(Ilk.WATER, new TreeSet<Tile>(new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                if (distance.get(o1) == distance.get(o2)) {
                    return 1;
                }
                return distance.get(o1) - distance.get(o2);
            }
        }));
        sur.put(Ilk.LAND, new TreeSet<Tile>(new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                if (distance.get(o1) == distance.get(o2)) {
                    return 1;
                }
                return distance.get(o1) - distance.get(o2);
            }
        }));

        sur.put(Ilk.FOOD, new TreeSet<Tile>(new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                if (distance.get(o1) == distance.get(o2)) {
                    return 1;
                }
                return distance.get(o1) - distance.get(o2);
            }
        }));
        sur.put(Ilk.MY_ANT, new TreeSet<Tile>(new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                if (distance.get(o1) == distance.get(o2)) {
                    return 1;
                }
                return distance.get(o1) - distance.get(o2);
            }
        }));
        sur.put(Ilk.MY_HILL, new TreeSet<Tile>(new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                if (distance.get(o1) == distance.get(o2)) {
                    return 1;
                }
                return distance.get(o1) - distance.get(o2);
            }
        }));
        sur.put(Ilk.ENEMY_ANT, new TreeSet<Tile>(new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                if (distance.get(o1) == distance.get(o2)) {
                    return 1;
                }
                return distance.get(o1) - distance.get(o2);
            }
        }));
        sur.put(Ilk.ENEMY_HILL, new TreeSet<Tile>(new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                if (distance.get(o1) == distance.get(o2)) {
                    return 1;
                }
                return distance.get(o1) - distance.get(o2);
            }
        }));
    }

    void addenamyAnts(Tile enamyAnt) {
        sur.get(Ilk.ENEMY_ANT).add(enamyAnt);
    }

    void addenamyHills(Tile enamyHill) {
        sur.get(Ilk.ENEMY_HILL).add(enamyHill);
    }

    void addfoods(Tile food) {
        sur.get(Ilk.FOOD).add(food);
    }

    void addmyAnts(Tile mine) {
        sur.get(Ilk.MY_ANT).add(mine);
    }

    @Override
    public String toString() {
        String ret = currTile.toString() + "\n";
        for (Ilk k : sur.keySet()) {
            ret += k.name() + " ";
            for (Tile t : sur.get(k)) {
                ret += "[" + t.toString() + " " + directions.get(t).name() + " " + distance.get(t) + " ]";
            }
            ret += "\n";
        }
        return ret;
    }

    void redar() {
        //clear pre vals
        for (Ilk k : sur.keySet()) {
            sur.get(k).clear();
        }
        TreeSet<Tile> visited = new TreeSet<Tile>();
        Queue<Tile> q = new ArrayDeque<Tile>();
        visited.add(currTile);
        for (Aim dir : Aim.values()) {
            Tile next = ants.getTile(currTile, dir);
            if (ants.getIlk(next).isUnoccupied()) {
                q.add(next);
            }
            //MyBot.debugln(currTile.toString() + " " + dir + " " + next.toString() + " " + ants.getIlk(next).toString());
            distance.put(next, 1);
            sur.get(ants.getIlk(next)).add(next);
            visited.add(next);
            directions.put(next, dir);
        }
        //MyBot.debugln(toString());
        while (!q.isEmpty()) {
            Tile current = q.poll();
            if (ants.getTimeRemaining() < 50) {
                throw new UnsupportedOperationException("TIME");
            }
            //debug("inside que" + q);
            for (Aim dir : Aim.values()) {
                Tile next = ants.getTile(current, dir);
                //debugln("now in " + t + " looking at " + dir + " next is " + next);
                if (!visited.contains(next) && (ants.getDirectDistance2(currTile, next) <= ants.getViewRadius2())) {
                    directions.put(next, directions.get(current));
                    distance.put(next, distance.get(current) + 1);
                    sur.get(ants.getIlk(next)).add(next);
                    visited.add(next);
                    if (ants.getIlk(next).isUnoccupied()) {
                        q.add(next);
                    }

                }
            }
        }
    }

    public static void updateNoOfThreatsEnemy(Tile enemyAnt) {
        for (Tile t : Ants.attackOffset) {
            Tile next = ants.getTile(enemyAnt, t);
            if (noOfThreats.containsKey(next)) {
                noOfThreats.put(next, noOfThreats.get(next) + 1);
            } else {
                noOfThreats.put(next, 1);
            }
        }
    }

    public static void updateNoOfThreatsMy(Tile enemyAnt) {
        for (Tile t : Ants.attackOffset) {
            Tile next = ants.getTile(enemyAnt, t);
            if (noOfThreats.containsKey(next)) {
                noOfThreats.put(next, noOfThreats.get(next) - 1);
            } else {
                noOfThreats.put(next, -1);
            }
        }
    }

    public static int noOfThreats(Tile t) {
        if (noOfThreats.containsKey(t)) {
            return noOfThreats.get(t);
        }
        return 0;
    }

    public Aim getDirectionsToNearest(Ilk type) {
        return directions.get(sur.get(type).first());
    }

    public Tile getNextTileToNearest(Ilk type) {
        return ants.getTile(currTile, getDirectionsToNearest(type));
    }
}
