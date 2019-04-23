package li.earth.urchin.twic;

import io.atlassian.util.concurrent.CopyOnWriteMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConcurrentMapBenchmarks {

    private static final int MAP_SIZE = 1009; // must be prime so that striding works

    private static Random newRandom(String seasoning) {
        return new Random(ConcurrentMapBenchmarks.class.getName().hashCode() + seasoning.hashCode());
    }

    private static Map<Integer, Integer> prepareMap(Map<Integer, Integer> map) {
        Random random = newRandom("prepareMap");

        List<Integer> keys = IntStream.range(0, MAP_SIZE).boxed().collect(Collectors.toList());
        Collections.shuffle(keys, random);

        for (Integer key : keys) {
            map.put(key, random.nextInt());
        }

        return map;
    }

    // note that for the plan HashMap, every thread gets its own instance
    @State(Scope.Thread)
    public static class HashMapFixture {
        final Map<Integer, Integer> map = prepareMap(new HashMap<>());
    }

    @State(Scope.Benchmark)
    public static class SynchronizedHashMapFixture {
        final Map<Integer, Integer> map = prepareMap(Collections.synchronizedMap(new HashMap<>()));
    }

    @State(Scope.Benchmark)
    public static class ConcurrentHashMapFixture {
        final Map<Integer, Integer> map = prepareMap(new ConcurrentHashMap<>());
    }

    @State(Scope.Benchmark)
    public static class CopyOnWriteMapFixture {
        final Map<Integer, Integer> map = prepareMap(CopyOnWriteMap.newHashMap());
    }

    @State(Scope.Thread)
    public static class IndexIterator {
        private final int step;
        private int index;

        public IndexIterator() {
            Random random = newRandom(Thread.currentThread().getName());
            step = random.nextInt(MAP_SIZE - 1) + 1;
            index = random.nextInt(MAP_SIZE);
        }

        int nextIndex() {
            index = (index + step) % MAP_SIZE;
            return index;
        }

        private boolean nextBoolean(int oddsOfTrue) {
            return nextIndex() % oddsOfTrue == 0;
        }
    }

    // control

    @Benchmark
    public int nothing(IndexIterator indexIterator) {
        return indexIterator.nextIndex();
    }

    // get

    private Integer get(Map<Integer, Integer> map, IndexIterator indexIterator) {
        return map.get(indexIterator.nextIndex());
    }

    @Benchmark
    public Integer getHashMap(HashMapFixture fixture, IndexIterator indexIterator) {
        return get(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer getSynchronizedHashMap(SynchronizedHashMapFixture fixture, IndexIterator indexIterator) {
        return get(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer getConcurrentHashMap(ConcurrentHashMapFixture fixture, IndexIterator indexIterator) {
        return get(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer getCopyOnWriteMap(CopyOnWriteMapFixture fixture, IndexIterator indexIterator) {
        return get(fixture.map, indexIterator);
    }

    // put

    private Integer put(Map<Integer, Integer> map, IndexIterator indexIterator) {
        return map.put(indexIterator.nextIndex(), indexIterator.nextIndex());
    }

    @Benchmark
    public Integer putHashMap(HashMapFixture fixture, IndexIterator indexIterator) {
        return put(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer putSynchronizedHashMap(SynchronizedHashMapFixture fixture, IndexIterator indexIterator) {
        return put(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer putConcurrentHashMap(ConcurrentHashMapFixture fixture, IndexIterator indexIterator) {
        return put(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer putCopyOnWriteMap(CopyOnWriteMapFixture fixture, IndexIterator indexIterator) {
        return put(fixture.map, indexIterator);
    }

    // mixed

    private Integer mixed(Map<Integer, Integer> map, IndexIterator indexIterator) {
        if (indexIterator.nextBoolean(100)) {
            return put(map, indexIterator);
        } else {
            return get(map, indexIterator);
        }
    }

    @Benchmark
    public Integer mixedHashMap(HashMapFixture fixture, IndexIterator indexIterator) {
        return mixed(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer mixedSynchronizedHashMap(SynchronizedHashMapFixture fixture, IndexIterator indexIterator) {
        return mixed(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer mixedConcurrentHashMap(ConcurrentHashMapFixture fixture, IndexIterator indexIterator) {
        return mixed(fixture.map, indexIterator);
    }

    @Benchmark
    public Integer mixedCopyOnWriteMap(CopyOnWriteMapFixture fixture, IndexIterator indexIterator) {
        return mixed(fixture.map, indexIterator);
    }

}
