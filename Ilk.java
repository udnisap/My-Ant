
/**
 * Represents type of tile on the game map.
 */
public enum Ilk {

    UNDISCOVERD,
    /** Water tile. */
    WATER,
    /** Food tile. */
    FOOD,
    /** Land tile. */
    LAND,
    /** Dead ant tile. */
    DEAD,
    /** Dead ant tile. */
    /** My ant tile. */
    MY_ANT,
    /** Enemy ant tile. */
    ENEMY_ANT,
    /** Enemy ant hill tile. */
    MY_HILL,
    /** Enemy ant hill tile. */
    ENEMY_HILL;

    /**
     * Checks if this type of tile is passable, which means it is not a water tile.
     * 
     * @return <code>true</code> if this is not a water tile, <code>false</code> otherwise
     */
    public boolean isPassable() {
        return ordinal() > WATER.ordinal();
    }

    /**
     * Checks if this type of tile is unoccupied, which means it is a land tile or a dead ant tile.
     * 
     * @return <code>true</code> if this is a land tile or a dead ant tile, <code>false</code>
     *         otherwise
     */
    public boolean isUnoccupied() {
        return this == LAND || this == DEAD || this == ENEMY_HILL;
    }

    public Ilk enemy() {
        if (this == MY_ANT) {
            return ENEMY_ANT;
        }
        if (this == ENEMY_ANT) {
            return MY_ANT;
        }
        throw new UnsupportedOperationException("Invalide Enemy");
    }
}
