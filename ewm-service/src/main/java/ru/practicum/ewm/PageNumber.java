package ru.practicum.ewm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PageNumber {
    public static Integer get(Integer from, Integer size) {
        int pageNumber;

        if (from < size) {
            pageNumber = 0;
        } else if (from.equals(size)) {
            pageNumber = size;
        } else {
            pageNumber = from / size;
        }
        return pageNumber;
    }
}
