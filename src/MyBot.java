
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {

    static int turn = 1;
    static PrintWriter out;
    HashMap<Tile, Aim> currentPos = new HashMap<Tile, Aim>();
    HashMap<Tile, Aim> nextPos = new HashMap<Tile, Aim>();
    HashMap<Tile, String> worksAs = new HashMap<Tile, String>();
    Set<Tile> remainingAnts;
    Set<Tile> remainingFood;
    TreeSet<Tile> targets = new TreeSet<Tile>(new Comparator<Tile>() {

        @Override
        public int compare(Tile o1, Tile o2) {
            Ilk i1 = ants.getIlk(o1);
            Ilk i2 = ants.getIlk(o2);
            if (i1.equals(i2)) {
                return o1.hashCode() - o2.hashCode();
            } else {
                return i1.ordinal() - i2.ordinal();
            }
        }
    });
    HashMap<Tile, Order> workAssignments = new HashMap<Tile, Order>();  // key = current location value order's location is target and  aim is next location 
    static Ants ants;
    static Mesh mesh;
    List<Ilk> priority = new LinkedList<Ilk>();
    static int maxVisBox;
    Set<Tile> Lefty = new HashSet<Tile>();
    Set<Tile> Hunter = new HashSet<Tile>();
    Set<Tile> myHillGuards;
    Tile currTarget;
    HashMap<Tile, Aim> targetPath = new HashMap<Tile, Aim>();

    /**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
       // out = new PrintWriter("./" + args[0] + ".txt");
        new MyBot().readSystemInput();

       // out.close();
    }

    private void BeforeTurn() {
        debugln("Turn " + (turn) + "------------------------------------------------------------");
        debugln(ants.getTimeRemaining() + "");
        //debugln(ants.getMyHills().toString());
        //debugln(ants.getEnemyHills().toString());
        remainingAnts = ants.getMyAnts();
        remainingFood = ants.getFoodTiles();
        newStraight = new HashMap<Tile, Aim>();
        newLefty = new HashMap<Tile, Aim>();
        nextPos = new HashMap<Tile, Aim>();
        ants.discoverNewTiles();
        for (Tile t : ants.getEnemyAnts()) {
            Redar.updateNoOfThreatsEnemy(t);
        }
        for (Tile tile : ants.getEnemyHills()) {
            targets.add(tile);
            addCurrTarget(tile);
        }
        //prevent steping into the own hill
        for (Tile myHill : ants.getMyHills()) {
            nextPos.put(myHill, Aim.NORTH);
        }




//        for (int i = 0; i < ants.rows; i++) {
//            for (int j = 0; j < ants.cols; j++) {
//                if (ants.discovered[i][j]) {
//                    debug("T\t");
//                } else {
//                    debug("F\t");
//                }
//            }
//            debugln("");
//        }



    }

    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
     * passable.
     */
    @Override
    public void doTurn() {
        int land = 0;
        int water = 0;
        try {
            BeforeTurn();
            Tile[] s = remainingAnts.toArray(new Tile[0]);
            for (Tile ant : s) {
                Redar r = redar.get(ant);
                r = new Redar(ant);
                r.redar();
                land += r.sur.get(Ilk.LAND).size();
                water += r.sur.get(Ilk.WATER).size();
                redar.put(ant, r);
                //debugln(r.toString());
            }
            if (water != 0) {
                debugln("ratios " + land + " " + water + " " + (land / water));
            }
            guardMyHill();
            for (Tile ant : s) {
                moveAnt(ant);
            }
            //prevent attacked ants
            preventAttacks();




//        attackEnemyHills();
//        attackTarget();
//        gatherFood();
            //attackEnemy();
            //Hunterbot(remainingAnts.size()/2);
            //LeftyBot(remainingAnts.size()/2);
//        LeftyBot();
            // debugln(ants.getOrders().toString());



        } catch (Exception e) {
            if ("TIME".equals(e.getMessage())) {
                debugln("Time Exceed");
            } else {
                debugln(e.toString());
                debugln("Orders issued" + ants.getOrders());
                debugln("Remaining Ants" + remainingAnts.toString());

               // Logger.getLogger(MyBot.class.getName()).log(Level.SEVERE, null, e);
               // System.exit(0);
            }
        }
        AfterTurn();

    }

    public boolean doMoveDirection(Tile antLoc, Aim direction) {
        if (direction == (null)) {
            return false;
        }
        // Track all moves, prevent collisions
        debug("try to move" + antLoc + " " + direction);
        Tile newLoc = ants.getTile(antLoc, direction);
        if (remainingAnts.contains(antLoc) && ants.getIlk(newLoc).isUnoccupied()
                && !nextPos.containsKey(newLoc) && Redar.noOfThreats(newLoc) < 1) {
            debugln(" success");
            ants.issueOrder(antLoc, direction);
            currentPos.put(antLoc, direction);
            nextPos.put(newLoc, direction);
            remainingAnts.remove(antLoc);
            return true;
        } else {
            debugln(" failed");
            return false;
        }
    }

    public void gatherFood() {
        Tile[] s = remainingFood.toArray(new Tile[0]);
        for (Tile food : s) {
            //debugln("Food " + food.toString());
            //find nearest ant to the food
            Order order = getDirectionToTileFromNearestTile(food);
            if (order != null && doMoveDirection(order.getTile(), order.getDirection())) {
                //debugln("found path" + order);
                remainingFood.remove(order.getTile());
            }
        }
    }

    private List<Order> getDirectionToTileFromArchievableTiles(Tile tile) {
        List<Order> ans = new LinkedList<Order>();
        TreeSet<Tile> visited = new TreeSet<Tile>();
        Queue<Tile> q = new ArrayDeque<Tile>();
        q.add(tile);
        //if tile is next to an ant
        int maxNodes = maxVisBox * maxVisBox;

        while (!q.isEmpty()) {
            Tile t = q.poll();
            visited.add(t);
            if (ants.getDirectDistance2(tile, t) > ants.getViewRadius2() || !ants.isDiscovered(t)) {
                continue;
            }
            if (maxNodes == 0) {
                break;
            }
            //debug("inside que" + q);
            for (Aim dir : Aim.values()) {
                Tile next = ants.getTile(t, dir);
                //debugln("now in " + t + " looking at " + dir + " next is " + next);
                if (ants.getIlk(next).equals(Ilk.MY_ANT)) {
                    // debugln("found a ant at " + next.toString());
                    ans.add(new Order(next, dir.behind()));
                }
                if (ants.getIlk(next).isPassable() && !visited.contains(next)) {
                    visited.add(next);
                    q.add(next);
                }
            }
            maxNodes--;
        }
        return ans;
    }

    private Order getDirectionToTileFromNearestTile(Tile tile) {
        TreeSet<Tile> visited = new TreeSet<Tile>();
        Queue<Tile> q = new ArrayDeque<Tile>();
        q.add(tile);
        //if tile is next to an ant
        int maxNodes = maxVisBox * maxVisBox;

        while (!q.isEmpty()) {
            Tile t = q.poll();
            visited.add(t);
            if (ants.getDirectDistance2(tile, t) > ants.getViewRadius2() || !ants.isDiscovered(t)) {
                continue;
            }
            if (maxNodes == 0) {
                break;
            }
            //debug("inside que" + q);
            for (Aim dir : Aim.values()) {
                Tile next = ants.getTile(t, dir);
                //  debug("now in "+ t + " looking at "+ dir + " next is "+next);
                if (ants.getIlk(next).equals(Ilk.MY_ANT) && remainingAnts.contains(next)) {
                    return new Order(next, dir.behind());
                }
                if (ants.getIlk(next).isPassable() && !visited.contains(next)) {
                    visited.add(next);
                    q.add(next);
                }
            }
            maxNodes--;
        }

        //unreachable
        return null;
    }

    public static void debugln(String string) {
        try {
          //  out.println(string);
          //  out.flush();
        } catch (Exception e) {
        }

    }

    public static void debug(String string) {
        try {
           // out.print(string);
          //  out.flush();
        } catch (Exception e) {
        }

    }

    private void Hunterbot() {
        Tile[] myants = remainingAnts.toArray(new Tile[0]);
        for (Tile location : myants) {
            Hunterbot(location);
        }
    }

    private void Hunterbot(int size) {
        Set<Tile> myants = Lefty;
        assignWork('H');

        for (Tile location : myants) {
            if (size == 0) {
                return;
            }
            Hunterbot(location);
            size--;
        }
    }

    private void Hunterbot(Tile location) {
        Tile closestTarget = null;
        int closestDistance = 999999;
        Set<Tile> myTargets = new HashSet<Tile>();
        debugln("Hunter at " + location);
        myTargets.addAll(targets);
        if (myTargets.isEmpty()) {
            myTargets.addAll(ants.unDiscovered);
        }
        for (Tile target : myTargets) {
            int distance = ants.getDistance(location, target);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = target;
            }
        }
        debugln("Selected Target " + closestTarget);
        if (closestTarget != null) {
            List<Aim> directions = ants.getDirections(location, closestTarget);
            Collections.shuffle(directions);
            for (Aim direction : directions) {
                if (doMoveDirection(location, direction)) {
                    return;
                }
            }

        } else {
            LeftyBot(location);
        }

    }
    private Map<Tile, Aim> antStraight = new HashMap<Tile, Aim>();
    private Map<Tile, Aim> antLefty = new HashMap<Tile, Aim>();
    Map<Tile, Aim> newStraight = new HashMap<Tile, Aim>();
    Map<Tile, Aim> newLefty = new HashMap<Tile, Aim>();

    public void LeftyBot() {
        Tile[] myants = remainingAnts.toArray(new Tile[0]);
        for (Tile location : myants) {
            LeftyBot(location);
        }
    }

    public void LeftyBot(int size) {
        Tile[] myants = remainingAnts.toArray(new Tile[0]);
        for (Tile location : myants) {
            if (size == 0) {
                return;
            }
            LeftyBot(location);
            size--;
        }
    }

    private void LeftyBot(Tile location) {
        // send new ants in a straight line
        if (!antStraight.containsKey(location) && !antLefty.containsKey(location)) {
            Aim direction;
            if (location.getRow() % 2 == 0) {
                if (location.getCol() % 2 == 0) {
                    direction = Aim.NORTH;
                } else {
                    direction = Aim.SOUTH;
                }
            } else {
                if (location.getCol() % 2 == 0) {
                    direction = Aim.EAST;
                } else {
                    direction = Aim.WEST;
                }
            }
            antStraight.put(location, direction);
        }
        // send ants going in a straight line in the same direction
        if (antStraight.containsKey(location)) {
            Aim direction = antStraight.get(location);
            Tile destination = ants.getTile(location, direction);
            if (ants.getIlk(destination).isPassable()) {
                if (doMoveDirection(location, direction)) {
                    newStraight.put(destination, direction);
                } else {
                    // pause ant, turn and try again next turn
                    newStraight.put(location, direction.left());
                }
            } else {
                // hit a wall, start following it
                antLefty.put(location, direction.right());
                //doMoveDirection(location, direction.right());
            }
        }
        // send ants following a wall, keeping it on their left
        if (antLefty.containsKey(location)) {
            Aim direction = antLefty.get(location);
            List<Aim> directions = new ArrayList<Aim>();
            directions.add(direction.left());
            directions.add(direction);
            directions.add(direction.right());
            directions.add(direction.behind());
            // try 4 directions in order, attempting to turn left at corners
            for (Aim new_direction : directions) {
                Tile destination = ants.getTile(location, new_direction);
                if (ants.getIlk(destination).isPassable()) {
                    if (doMoveDirection(location, new_direction)) {
                        newLefty.put(destination, new_direction);
                        break;
                    } else {
                        newStraight.put(location, direction.right());
                        break;
                    }
                }
            }

        }


    }

    @Override
    public void afterSetup() {
        debugln("after setup");
        ants = getAnts();
        mesh = new Mesh(getAnts());
        maxVisBox = (int) Math.sqrt(2 * ants.getViewRadius2());
        priority.add(Ilk.FOOD);
        priority.add(Ilk.ENEMY_HILL);
        priority.add(Ilk.ENEMY_ANT);
//        int size = ants.getRows()*Ants.MAX_MAP_SIZE + ants.getCols();
//        shortestPath = new Aim[size][size];
//        shortestDis = new int[size][size];
    }
    static TreeSet<Tile> radarVisited = new TreeSet<Tile>();

    private void browse(EnumMap<Ilk, HashMap<Tile, Aim>> surrounding, Tile tile, Aim aim, Tile source, Aim AimfrmSource) {
        Tile cur = ants.getTile(tile, aim);
        //  debugln(cur.toString() + " " + source.toString());
        if (ants.getDirectDistance2(cur, source) > ants.getViewRadius2() || ants.getIlk(cur).equals(Ilk.WATER)) {
            return;
        }

        if (ants.getIlk(cur).equals(Ilk.FOOD) || ants.getIlk(cur).equals(Ilk.ENEMY_ANT) || ants.getIlk(cur).equals(Ilk.ENEMY_HILL)) {
            //  debugln(ants.getIlk(cur).name());
            surrounding.get(ants.getIlk(cur)).put(cur, AimfrmSource);
        }
        for (Aim a : Aim.values()) {
            if (ants.getIlk(cur, a).isPassable() && !radarVisited.contains(ants.getTile(cur, a))) {
                browse(surrounding, cur, a, source, AimfrmSource);
                radarVisited.add(ants.getTile(cur, a));
            }
        }


    }

    private EnumMap<Ilk, HashMap<Tile, Aim>> redar(Tile tile) {
        //radarVisited.clear();
        EnumMap<Ilk, HashMap<Tile, Aim>> surrounding = new EnumMap<Ilk, HashMap<Tile, Aim>>(Ilk.class);
        surrounding.put(Ilk.FOOD, new HashMap<Tile, Aim>());
        surrounding.put(Ilk.ENEMY_ANT, new HashMap<Tile, Aim>());
        surrounding.put(Ilk.ENEMY_HILL, new HashMap<Tile, Aim>());
        // debugln("North");
        browse(surrounding, tile, Aim.NORTH, tile, Aim.NORTH);
        // debugln("south");
        browse(surrounding, tile, Aim.SOUTH, tile, Aim.SOUTH);
        // debugln("East");
        browse(surrounding, tile, Aim.EAST, tile, Aim.EAST);
        // debugln("west");
        browse(surrounding, tile, Aim.WEST, tile, Aim.WEST);
        return surrounding;
    }
    static HashSet<Tile> guards = new HashSet<Tile>();

    private void guardMyHill() {
        //check if there are sufficient ants to guard
        //debugln(ants.getMyHills().toString());
        //debugln(redar.toString());
        int limit = ants.getMyAnts().size() / 4;
        if (limit < 1 || ants.getMyHills().size() < 1) {
            return;
        }

        for (Tile myHill : ants.getMyHills()) {

            Tile pos = ants.getTile(myHill, new Tile(1, 1));
            if (ants.getIlk(pos).equals(Ilk.MY_ANT)) {
                remainingAnts.remove(pos);
                limit--;
            } else if (remainingAnts.contains(myHill) && ants.getIlk(pos).isPassable()) {
                doMoveDirection(myHill, redar.get(myHill).directions.get(pos));
                limit--;
            }
            if (limit < 1) {
                return;
            }
            pos = ants.getTile(myHill, new Tile(1, -1));
            if (ants.getIlk(pos).equals(Ilk.MY_ANT)) {
                remainingAnts.remove(pos);
                limit--;
            } else if (remainingAnts.contains(myHill) && ants.getIlk(pos).isPassable()) {
                doMoveDirection(myHill, redar.get(myHill).directions.get(pos));
                limit--;
            }
            if (limit < 1) {
                return;
            }
            pos = ants.getTile(myHill, new Tile(-1, -1));
            if (ants.getIlk(pos).equals(Ilk.MY_ANT)) {
                remainingAnts.remove(pos);
                limit--;
            } else if (remainingAnts.contains(myHill) && ants.getIlk(pos).isPassable()) {
                doMoveDirection(myHill, redar.get(myHill).directions.get(pos));
                limit--;
            }
            if (limit < 1) {
                return;
            }
            pos = ants.getTile(myHill, new Tile(-1, 1));
            if (ants.getIlk(pos).equals(Ilk.MY_ANT)) {
                remainingAnts.remove(pos);
                limit--;
            } else if (remainingAnts.contains(myHill) && ants.getIlk(pos).isPassable()) {
                doMoveDirection(myHill, redar.get(myHill).directions.get(pos));
                limit--;
            }
            if (limit < 1) {
                return;
            }

        }


    }

    @Override
    public void AfterTurn() {
        Tile[] targ = targets.toArray(new Tile[0]);
        boolean changed = false;
        for (Tile t : targ) {
            if (nextPos.containsKey(t)) {
                targets.remove(t);
                changed = true;
            }
        }
        if (changed) {
            clearCurrTarget();
        }


        //debugln("Targets " + targets.toString());
        //debugln("Undiscovered " + ants.unDiscovered.toString());
        Redar.noOfThreats.clear();
        enemies.clear();
        isGettingKilled.clear();
        currentPos = nextPos;
        antStraight = newStraight;
        antLefty = newLefty;
        turn++;
//        if (currTarget != null) {
//            debugln("current Target " + currTarget.toString());
//        }
        debugln(ants.getTimeRemaining() + "");

    }

    private void assignWork(char type) {
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    private List<Tile> Astart(Tile start, Tile end) {
        targetPath.clear();
        debugln("ASTART from " + start + " to " + end);
        if (!ants.isDiscovered(start) || !ants.isDiscovered(end)) {
            debugln("Not discovered yet");
            return null;
        }
        if (start.equals(end)) {
            return new LinkedList<Tile>();
        }
        final Tile dest = end;
        TreeSet<Tile> visited = new TreeSet<Tile>();
        HashMap<Tile, Tile> previous = new HashMap<Tile, Tile>();
        Queue<Tile> q = new PriorityQueue<Tile>(ants.rows * ants.cols, new Comparator<Tile>() {

            @Override
            public int compare(Tile o1, Tile o2) {
                return ants.getDirectDistance2(o1, dest) - ants.getDirectDistance2(o2, dest);
            }
        });
        previous.put(start, null);
        visited.add(start);
        q.add(start);
        //if tile is next to an ant
        int maxNodes = maxVisBox * maxVisBox;
        boolean foundPath = false;
        q:
        while (!q.isEmpty() && ants.getTimeRemaining() > 50) {
            Tile current = q.poll();
            if (maxNodes == 0) {
                //return null;
            }
            for (Aim dir : Aim.values()) {
                Tile next = ants.getTile(current, dir);
                //  debug("now in "+ t + " looking at "+ dir + " next is "+next);
                if ((next).equals(dest)) {
                    foundPath = true;
                    previous.put(next, current);
                    break q;
                }
                if (ants.getIlk(next).isPassable() && ants.isDiscovered(current) && !visited.contains(next)) {
                    previous.put(next, current);
                    visited.add(next);
                    q.add(next);
                }
            }
            maxNodes--;
            //backtrack.pop();
        }
        if (foundPath) {
            List<Tile> path = new LinkedList<Tile>();
            Tile inbetween = dest;
            while (inbetween != null) {
                path.add(inbetween);
                Tile preinbetween = previous.get(inbetween);
                targetPath.put(preinbetween, ants.getDirections(preinbetween, inbetween).get(0));
            }
            debugln(path.toString());
            return path;
        }
        return null;
    }

    private void clearCurrTarget() {
        if (currTarget == null) {
            return;
        }
        if (ants.currViewRange.contains(currTarget) && !ants.getIlk(currTarget).equals(Ilk.ENEMY_HILL)) {
            currTarget = null;
        }
        if (targets.size() > 0) {
            currTarget = targets.iterator().next();
        }
        targetPath.clear();
    }

    private void addCurrTarget(Tile tile) {
        if (currTarget == null || (ants.currViewRange.contains(currTarget) && !ants.getIlk(currTarget).equals(Ilk.ENEMY_HILL))) {
            clearCurrTarget();
            currTarget = tile;
        }
    }

    private void attackTarget() {
        Tile[] myAnts = remainingAnts.toArray(new Tile[0]);
        for (Tile ant : myAnts) {
            if (targetPath.containsKey(ant)) {
                doMoveDirection(ant, targetPath.get(ant));
            }
        }
    }
    HashMap<Tile, Redar> redar = new HashMap<Tile, Redar>();

    private void addToRedar() {
        //redar.clear();
        for (Tile myAnt : ants.getMyAnts()) {
            Redar r = redar.get(myAnt);
            r = new Redar(myAnt);
            r.redar();
            redar.put(myAnt, r);
        }
    }

    private void moveAnt(Tile ant) {
        if (!remainingAnts.contains(ant)) {
            return;
        }
        Redar r = redar.get(ant);
        if (r.sur.get(Ilk.ENEMY_ANT).isEmpty()) {
            if (r.sur.get(Ilk.ENEMY_HILL).isEmpty()) {
                if (r.sur.get(Ilk.FOOD).isEmpty()) {
                    // no target to work on
                    //send to discover the map
                    debug(ant.toString() + " NoTarget");
                    if (r.sur.get(Ilk.LAND).size() < 6 * r.sur.get(Ilk.WATER).size()) {
                        debugln(" Lefty");
                        LeftyBot(ant);
                    } else {
                        debugln(" Hunter");
                        Hunterbot(ant);
                    }
                } else {
                    //only food is viewable 
                    //get it
                    debugln(ant.toString() + " Food only");
                    for (Tile food : r.sur.get(Ilk.FOOD)) {
                        if (doMoveDirection(ant, r.directions.get(food))) {
                            remainingFood.remove(food);
                            return;
                        }

                    }
                }
            } else {
                //un protected enemy hill
                //attack
                debugln(ant.toString() + " Unprotected Hill");
                for (Tile hill : r.sur.get(Ilk.ENEMY_HILL)) {
                    debugln("attack hill" + r.directions.get(hill));
                    if (doMoveDirection(ant, r.directions.get(hill))) {
                        break;
                    }
                }
            }
        } else {
            if (r.sur.get(Ilk.ENEMY_HILL).isEmpty()) {
                if (r.sur.get(Ilk.FOOD).isEmpty()) {
                    //only enemies are there
                    debugln(ant.toString() + " Enemies only");
                    //try to form a attacking grid
                    //current ant to forward
                    Tile nearestEneyAnt = r.getNextTileToNearest(Ilk.ENEMY_ANT);
                    Aim nextAim = r.getDirectionsToNearest(Ilk.ENEMY_ANT);
                    Tile nextTile = ants.getTile(ant, nextAim);
                    int noEnemies = r.sur.get(Ilk.ENEMY_ANT).size();
                    if (Redar.noOfThreats(nextTile) < 1) {
                        doMoveDirection(ant, nextAim);
                    } else {
                        nextTile = ant;
                    }
                    int count = 0;
                    //if other ants of mine are in the area
                    if (r.sur.get(Ilk.MY_ANT).size() > 0) {

                        for (Tile myAnts : r.sur.get(Ilk.MY_ANT)) {
                            debugln(redar.get(myAnts).toString());
                            // if the other ant of mine can also see the enemy
                            if (redar.get(myAnts).directions.get(nearestEneyAnt) != null) {
                                doMoveDirection(myAnts, redar.get(myAnts).directions.get(nearestEneyAnt));
                            } else {
                                doMoveDirection(myAnts, redar.get(myAnts).directions.get(ant));
                            }
                            if (++count > noEnemies) {
                                break;
                            }
                        }
                    }
                    if (count < noEnemies) {
                        targets.add(nearestEneyAnt);
                    }

                } else {
                    //enemy and food
                    //try to get the food with out attacking
                    debug(ant.toString() + " Enemy and Food");
                    for (Tile food : r.sur.get(Ilk.FOOD)) {
                        Tile target = ants.getTile(ant, r.directions.get(food));
                        if (Redar.noOfThreats(target) <= 0) {
                            doMoveDirection(ant, r.directions.get(food));
                            remainingFood.remove(food);
                            debugln("Get Food only Suceess");
                            return;
                        }
                    }
                    debugln("Get Food only failed");
                    //Todo Attack enemy and grab food

                }
            } else {
                if (r.sur.get(Ilk.FOOD).isEmpty()) {
                    //enemy and a hill
                    // try to attack the hill with out attacking ants
                    debug(ant.toString() + " Enemy and Hill");
                    for (Tile hill : r.sur.get(Ilk.ENEMY_HILL)) {
                        Tile target = ants.getTile(ant, r.directions.get(hill));
                        if (Redar.noOfThreats(target) <= 0) {
                            doMoveDirection(ant, r.directions.get(hill));
                            debugln("Attack Hill only suceeded");
                            return;
                        }
                    }
                    debugln("Attack Hill only failed");
                    //Todo attack the enemy and the hill

                } else {
                    //enemy, hill, and a food
                    // try to attack the hill with out attacking ants
                    debug(ant.toString() + " Enemy, Hill, Food");
                    for (Tile hill : r.sur.get(Ilk.ENEMY_HILL)) {
                        Tile target = ants.getTile(ant, r.directions.get(hill));
                        if (Redar.noOfThreats(target) <= 0) {
                            doMoveDirection(ant, r.directions.get(hill));
                            debugln("Attack Hill only Sucess");
                            return;
                        }
                    }
                    // try to grab the food with out attacking ants
                    for (Tile food : r.sur.get(Ilk.FOOD)) {
                        Tile target = ants.getTile(ant, r.directions.get(food));
                        if (Redar.noOfThreats(target) <= 0) {
                            doMoveDirection(ant, r.directions.get(food));
                            remainingFood.remove(food);
                            debugln("Get Food only sucess");
                            return;
                        }
                    }
                    debugln("Attack Hill or Food only Failed");
                    //Todo attack enemy and ..

                }
            }
        }
    }

    private void preventAttacks() {
        Tile[] s = remainingAnts.toArray(new Tile[0]);
        for (Tile t : s) {
            if (ants.getTimeRemaining() < 50) {
                throw new UnsupportedOperationException("TIME");
            }
            if (isGettingKilled(t)) {
                for (Aim a : Aim.values()) {
                    if (doMoveDirection(t, a)) {
                        break;
                    }
                }
            }
        }

    }
    HashMap<Tile, Boolean> isGettingKilled = new HashMap<Tile, Boolean>();

    private boolean isGettingKilled(Tile ant) {
        if (isGettingKilled.containsKey(ant)) {
            return isGettingKilled.get(ant);
        }
        Ilk ops = ants.getIlk(ant).enemy();
        int myEnemies = enemies(ant);
        for (Tile t : Ants.attackOffset) {
            Tile next = ants.getTile(ant, t);
            if (ants.getIlk(next).equals(ops) && (myEnemies >= enemies(next))) {
                isGettingKilled.put(ant, Boolean.TRUE);
                return true;
            }
        }
        isGettingKilled.put(ant, Boolean.FALSE);
        return false;
    }
    HashMap<Tile, Integer> enemies = new HashMap<Tile, Integer>();

    private int enemies(Tile ant) {
        if (enemies.containsKey(ant)) {
            return enemies.get(ant);
        }
        int count = 0;
        Ilk ops = ants.getIlk(ant).enemy();
        for (Tile t : Ants.attackOffset) {
            Tile next = ants.getTile(ant, t);
            if (ants.getIlk(next).equals(ops)) {
                count++;
            }
        }
        enemies.put(ant, count);
        return count;
    }
}
