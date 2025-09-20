package ru.yandex.my.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ListUtil {

    /**
     * Разбивает входной список на подсписки фиксированного размера (chunks).
     * Если последний chunk меньше указанного размера, он дополняется
     * элементами-заглушками {@code mock}, чтобы иметь ровно {@code chunkSize} элементов
     */
    public static <T> List<List<T>> chunkWithPadding(List<T> list, int chunkSize, T mock) {
        List<List<T>> chunkedList = ListUtils.partition(list, chunkSize);

        if (!chunkedList.isEmpty()) {
            List<T> lastChunk = chunkedList.getLast();
            int mocksToAdd = chunkSize - lastChunk.size();

            if (mocksToAdd > 0) {
                List<T> paddedLastChunk = new ArrayList<>(lastChunk);
                for (int i = 0; i < mocksToAdd; i++) {
                    paddedLastChunk.add(mock);
                }
                chunkedList = new ArrayList<>(chunkedList);
                chunkedList.set(chunkedList.size() - 1, paddedLastChunk);
            }
        }

        return chunkedList;
    }
}
