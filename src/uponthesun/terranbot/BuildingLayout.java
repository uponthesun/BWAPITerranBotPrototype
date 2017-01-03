package uponthesun.terranbot;
import java.util.HashMap;
import java.util.Map;

import bwapi.Position;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class BuildingLayout {

    private final Map<LocationType, Position> defaultLocations;

    private BuildingLayout(Map<LocationType, Position> defaultLocations) {
        this.defaultLocations = defaultLocations;
    }

    public Position getDefaultLocation(LocationType locationType) {
        return this.defaultLocations.get(locationType);
    }

    public static BuildingLayout createFromBaseLocation(BaseLocation base) {
        Chokepoint choke = base.getRegion().getChokepoints().get(0);
        
        Position chokeCenter = choke.getCenter();
        Position baseCenter = base.getRegion().getCenter();
        
        int deltaX = chokeCenter.getX() - baseCenter.getX();
        int deltaY = chokeCenter.getY() - baseCenter.getY();

        final Position supplyDefaultLocation = new Position(baseCenter.getX() - deltaX/2, baseCenter.getY() - deltaY/2);
        final Position productionDefaultLocation = new Position(baseCenter.getX() + deltaX/2, baseCenter.getY() + deltaY/2);

        Map<LocationType, Position> defaultLocations = new HashMap<>();
        defaultLocations.put(LocationType.PRODUCTION, productionDefaultLocation);
        defaultLocations.put(LocationType.SUPPLY, supplyDefaultLocation);
        return new BuildingLayout(defaultLocations);
    }

    public enum LocationType {
        PRODUCTION,
        SUPPLY
    }
}
