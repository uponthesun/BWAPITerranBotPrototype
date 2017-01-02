import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit discovered " + unit.getType());
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();

        // Use BWTA to analyze map
        // This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        
        int i = 0;
        for (BaseLocation baseLocation : BWTA.getBaseLocations()) {
            System.out.println("Base location #" + (++i)
                    + ". Printing location's region polygon:");
            for (Position position : baseLocation.getRegion().getPolygon()
                    .getPoints()) {
                System.out.print(position + ", ");
            }
            System.out.println();
        }

        game.enableFlag(1);
        game.setLocalSpeed(10);
    }

    private void printGameInfo(Game game) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void drawBaseLocation(Game game, BaseLocation base) {
        List<Position> points = base.getRegion().getPolygon().getPoints();
        
        Position from = points.get(points.size() - 1);
        
        for(Position to : base.getRegion().getPolygon().getPoints()) {
            game.drawLineMap(from, to, Color.Red);

            from = to;
        }
        
        for(Chokepoint choke : base.getRegion().getChokepoints()) {
            game.drawLineMap(choke.getSides().first, choke.getSides().second, Color.Blue);
        }
    }
    
    private void buildDepot(Game game, BaseLocation base, Unit scv) {
        Chokepoint choke = base.getRegion().getChokepoints().get(0);
        
        Position chokeCenter = choke.getCenter();
        Position baseCenter = base.getRegion().getCenter();
        
        int deltaX = chokeCenter.getX() - baseCenter.getX();
        int deltaY = chokeCenter.getY() - baseCenter.getY();

        final Position defaultDepotLocation = new Position(baseCenter.getX() - deltaX/2, baseCenter.getY() - deltaY/2);
        game.drawCircleMap(defaultDepotLocation, 10, Color.Green);
        game.drawCircleMap(defaultDepotLocation.getX() + 96, defaultDepotLocation.getY(), 10, Color.Green);
        game.drawCircleMap(defaultDepotLocation.getX(), defaultDepotLocation.getY() + 64, 10, Color.Green);

        Position buildPosition = findBuildablePosition(defaultDepotLocation, UnitType.Terran_Supply_Depot);
        if(buildPosition == null) {
            throw new RuntimeException("could not find buildable position.");
        }

        scv.build(UnitType.Terran_Supply_Depot, buildPosition.toTilePosition());
    }

    private Position findBuildablePosition(final Position start, UnitType buildingType) {
        Comparator<Position> comparator = new Comparator<Position>() {
            @Override
            public int compare(Position arg0, Position arg1) {
                return (int)(arg0.getDistance(start) - arg1.getDistance(start));
            }
        };
        
        Queue<Position> queue = new PriorityQueue<>(comparator);
        Set<Position> visited = new HashSet<>();
        queue.add(start);

        final int buildingWidth = 96;
        final int buildingHeight = 64;
        while(!queue.isEmpty()) {
            System.out.println("queue: " + queue + " visited: " + visited);
            Position curr = queue.remove();
            if (visited.contains(curr)) {
                continue;
            }

            if(this.game.canBuildHere(curr.toTilePosition(), UnitType.Terran_Supply_Depot)) {
                System.out.println("found buildable: " + curr);
                return curr;
            }
            
            visited.add(curr);
            for(int dX : new int[]{-1, 0, 1}) {
                for(int dY : new int[]{-1, 0, 1}) {
                    Position newPos = new Position(curr.getX() + dX * buildingWidth, curr.getY() + dY * buildingHeight);
                    if(!visited.contains(newPos)) {
                        queue.add(newPos);
                    }
                }
            }
        }
        
        return null;
    }
    
    @Override
    public void onFrame() {
        try {
            // game.setTextSize(10);
            //game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - "
              //      + self.getRace());
            
            BaseLocation myMain = BWTA.getStartLocation(self);
            drawBaseLocation(game, myMain);
            printGameInfo(game);

            StringBuilder screen_text = new StringBuilder("My units:\n");
            
            Multimap<UnitType, Unit> unitsByType = new Multimap<>();
            
            // iterate through my units
            for (Unit unit : self.getUnits()) {
                unitsByType.put(unit.getType(), unit);

                if(unit.isCompleted()) {
                    screen_text.append(unit.getType()).append(" ")
                    .append("isIdle: " + unit.isIdle() + " ")
                    .append(unit.getTilePosition()).append("\n");
                }
            }

            for (Unit cc : unitsByType.get(UnitType.Terran_Command_Center)) {
                if((cc.getTrainingQueue().isEmpty())
                        && self.minerals() >= 50) {
                    cc.train(UnitType.Terran_SCV);
                } else if (cc.getTrainingQueue().size() == 1 && cc.getRemainingTrainTime() < 5
                        && cc.getRemainingTrainTime() > 0) {
                    cc.train(UnitType.Terran_SCV);
                }
            }

            Unit builder = null;
            for (Unit scv : unitsByType.get(UnitType.Terran_SCV)) {
                if(builder == null && myMain.getRegion().getPolygon().isInside(scv.getPosition())) {
                    //System.out.println("selected default builder");
                    builder = scv;
                }

                // if it's a worker and it's idle, send it to the closest mineral
                // patch
                if (scv.getType().isWorker() && scv.isIdle()) {
                    Unit closestMineral = null;

                    // find the closest mineral
                    for (Unit neutralUnit : game.neutral().getUnits()) {
                        if (neutralUnit.getType().isMineralField()) {
                            if (closestMineral == null
                                    || scv.getDistance(neutralUnit) < scv
                                    .getDistance(closestMineral)) {
                                closestMineral = neutralUnit;
                            }
                        }
                    }

                    // if a mineral patch was found, send the worker to gather it
                    if (closestMineral != null) {
                        scv.gather(closestMineral, false);
                    }

                    if (scv.isCompleted() && myMain.getRegion().getPolygon().isInside(scv.getPosition())) {
                        System.out.println("selected idle builder");
                        builder = scv;
                    }
                }
            }

            if(builder == null) {
                throw new RuntimeException("no builder selected");
            }
            
            //if(self.supplyUsed() >= self.supplyTotal() - 2 && 
            if (self.minerals() >= 100 &&
                    !unitsByType.get(UnitType.Terran_Supply_Depot).stream().anyMatch(Unit::isBeingConstructed)) {
                System.out.println("attempting to build depot");
                buildDepot(game, myMain, builder);
            }

            screen_text.append("\nMouse position: " + game.getMousePosition());
            // Draw debug info on screen
            game.drawTextScreen(10, 25, screen_text.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        new TestBot1().run();
    }
}