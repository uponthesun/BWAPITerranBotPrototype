import bwapi.Position;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class BuildingLayout {

    private final Position productionDefaultLocation;
    private final Position supplyDefaultLocation;

    public BuildingLayout(Position productionDefaultLocation, Position supplyDefaultLocation) {
        this.productionDefaultLocation = productionDefaultLocation;
        this.supplyDefaultLocation = supplyDefaultLocation;
    }

    public Position getProductionDefaultLocation() {
        return productionDefaultLocation;
    }

    public Position getSupplyDefaultLocation() {
        return supplyDefaultLocation;
    }

    public static BuildingLayout createFromBaseLocation(BaseLocation base) {
        Chokepoint choke = base.getRegion().getChokepoints().get(0);
        
        Position chokeCenter = choke.getCenter();
        Position baseCenter = base.getRegion().getCenter();
        
        int deltaX = chokeCenter.getX() - baseCenter.getX();
        int deltaY = chokeCenter.getY() - baseCenter.getY();

        final Position supplyDefaultLocation = new Position(baseCenter.getX() - deltaX/2, baseCenter.getY() - deltaY/2);
        final Position productionDefaultLocation = new Position(baseCenter.getX() + deltaX/2, baseCenter.getY() + deltaY/2);

        return new BuildingLayout(productionDefaultLocation, supplyDefaultLocation);
    }
}
