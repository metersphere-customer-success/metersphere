package io.metersphere.utils;

import java.util.ArrayList;
import java.util.List;

public class MsListUtil {

    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        //(先计算出余数)
        int remainder = source.size() % n;
        //然后是商
        int number = source.size() / n;
        //偏移量
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remainder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }


    public static <T> List<T> toSublist(List<T> originalList, int firstIndex, int lastIndex) {
        if (originalList.size() < lastIndex && originalList.size() >= firstIndex) {
            return originalList.subList(firstIndex, originalList.size());
        } else {
            return (List) (originalList.size() < firstIndex ? new ArrayList(0) : originalList.subList(firstIndex, lastIndex));
        }
    }

    public static <T> List<List<T>> divSublist(List<T> list, int everyListSize) {
        List<List<T>> answer = new ArrayList();
        int size = list.size();
        int count = (size + everyListSize - 1) / everyListSize;

        for (int i = 0; i < count; ++i) {
            answer.add(list.subList(i * everyListSize, (i + 1) * everyListSize > size ? size : everyListSize * (i + 1)));
        }

        return answer;
    }
}
