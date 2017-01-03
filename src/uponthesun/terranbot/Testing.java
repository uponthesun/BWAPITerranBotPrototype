package uponthesun.terranbot;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import bwapi.UnitType;

public class Testing {

    @Test
    public void test() throws IllegalArgumentException, IllegalAccessException {
        List<Field> allUnits = Arrays.asList(UnitType.class.getDeclaredFields());
        List<Field> terranUnits = allUnits.stream()
                .filter(f -> f.getName().startsWith("Terran"))
                .collect(Collectors.toList());
        
        Map<String, UnitType> unitTypesByName = new HashMap<>();
        for(Field field : terranUnits) {
            unitTypesByName.put(field.getName(), (UnitType)field.get(null));
        }
    }
}
