import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Multimap <K, V> {

    private final HashMap<K, List<V>> map;

    public Multimap() {
        this.map = new HashMap<>();
    }

    public List<V> get(K key) {
        if (!this.map.containsKey(key)) {
            return new ArrayList<V>();
        }
        
        return this.map.get(key);
    }

    public void put(K key, V value) {
        if (!this.map.containsKey(key)) {
            this.map.put(key, new ArrayList<V>());
        }

        this.map.get(key).add(value);
    }

    public Map<K, List<V>> getMap() {
        return this.map;
    }

    public List<V> allValues() {
        return this.map.values().stream()
            .flatMap(unitList -> unitList.stream())
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return this.map.toString();
    }
}
