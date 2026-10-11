package org.llgs.global.util;

import lombok.extern.slf4j.Slf4j;
import org.llgs.global.constant.GlobalConstant;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ObjectUtil {

    public static <T> Collection<Collection<T>> partitionCollection(Collection<T> collection) {
        return partitionCollection(collection, GlobalConstant.SPLIT_SIZE);
    }

    public static <T> Collection<Collection<T>> partitionCollection(Collection<T> collection, Integer splitSize) {
        Collection<Collection<T>> results = new ArrayList<>();
        int size = Optional.ofNullable(splitSize).filter(i -> i >= GlobalConstant.SPLIT_SIZE).orElse(GlobalConstant.SPLIT_SIZE);
        List<T> items = new ArrayList<>(collection);

        for (int i = 0; i < items.size(); i += size) {
            results.add(new ArrayList<>(items.subList(i, Math.min(i + size, items.size() ))));
        }

        return results;
    }

    public static void sleep(TimeUnit timeUnit, long time) {
        try {
            Optional.ofNullable(timeUnit).orElse(TimeUnit.MILLISECONDS).sleep(time <= 0 ? 0 : time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleep(long time) {
        sleep(TimeUnit.MILLISECONDS, time);
    }
}
