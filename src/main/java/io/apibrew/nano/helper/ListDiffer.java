package io.apibrew.nano.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class ListDiffer {

    public static class DiffResult<T> {
        final public List<T> added = new ArrayList<>();
        final public List<T> deleted = new ArrayList<>();
        final public List<T> updated = new ArrayList<>();
        final public List<T> same = new ArrayList<>();
    }

    public static <T> DiffResult<T> diff(List<T> existingList, List<T> newList, BiPredicate<T, T> isSame, BiPredicate<T, T> isEqual) {
        DiffResult<T> result = new DiffResult<>();

        for (T existing : existingList) {
            var found = false;
            for (T newItem : newList) {
                if (isSame.test(existing, newItem)) {
                    if (isEqual.test(existing, newItem)) {
                        result.same.add(newItem);
                    } else {
                        result.updated.add(newItem);
                    }
                    found = true;
                }
            }

            if (!found) {
                result.deleted.add(existing);
            }
        }

        for (T newItem : newList) {
            var found = false;
            for (T existing : existingList) {
                if (isSame.test(existing, newItem)) {
                    found = true;
                }
            }

            if (!found) {
                result.added.add(newItem);
            }
        }

        return result;
    }

}
