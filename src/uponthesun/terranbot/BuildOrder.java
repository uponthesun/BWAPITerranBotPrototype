package uponthesun.terranbot;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import bwapi.UnitType;
import uponthesun.terranbot.BuildingLayout.LocationType;

public class BuildOrder {
    private static final String DELIMITER = ",";

    private final List<BuildStep> steps;

    public BuildOrder(List<BuildStep> steps) {
        this.steps = steps;
    }

    public List<BuildStep> getSteps() {
        return steps;
    }

    public static BuildOrder parseBuildOrder(String fileName) {
        return BuildOrder.parseBuildOrder(new File(fileName));
    }
    
    public static BuildOrder parseBuildOrder(File file) {
        try (Scanner scanner = new Scanner(file)) {
            List<String> lines = new ArrayList<>();
            while(scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }

            return BuildOrder.parseBuildOrder(lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BuildOrder parseBuildOrder(List<String> inputLines) {
        List<BuildStep> steps = new ArrayList<>();

        List<Field> allUnits = Arrays.asList(UnitType.class.getDeclaredFields());
        List<Field> terranUnits = allUnits.stream()
                .filter(f -> f.getName().startsWith("Terran"))
                .collect(Collectors.toList());
        
        Map<String, UnitType> unitTypesByName = new HashMap<>();
        try {
            for(Field field : terranUnits) {
                unitTypesByName.put(field.getName(), (UnitType)field.get(null));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for(String line : inputLines) {
            String[] split = line.split(DELIMITER);
            String unitTypeSection = split[0].trim();

            if(!unitTypesByName.containsKey(unitTypeSection)) {
                throw new RuntimeException("Unknown unit type: " + unitTypeSection + " Known types: " + 
                        unitTypesByName.keySet());
            }

            UnitType unitType = unitTypesByName.get(unitTypeSection);
            final LocationType locationType;
            if(!unitType.isBuilding()) {
                locationType = null;
            } else if(unitType.equals(UnitType.Terran_Barracks) 
                    || unitType.equals(UnitType.Terran_Factory)
                    || unitType.equals(UnitType.Terran_Starport)) {
                locationType = LocationType.PRODUCTION;
            } else {
                locationType = LocationType.SUPPLY;
            }

            BuildStep newStep = new BuildStep(unitType, Optional.ofNullable(locationType));
            System.out.println(line);
            System.out.println(newStep);
            steps.add(newStep);
        }

        return new BuildOrder(Collections.unmodifiableList(steps));
    }

    public static class BuildStep {
        private final UnitType unitTypeToBuild;
        private final Optional<LocationType> locationType;
        
        public BuildStep(UnitType unitTypeToBuild, Optional<LocationType> locationType) {
            this.unitTypeToBuild = unitTypeToBuild;
            this.locationType = locationType;
        }
        
        public UnitType getUnitTypeToBuild() {
            return unitTypeToBuild;
        }
        
        public Optional<LocationType> getLocationType() {
            return locationType;
        }

        @Override
        public String toString() {
            return String.format("UnitType: %s LocationType: %s", unitTypeToBuild, locationType);
        }
    }
}
