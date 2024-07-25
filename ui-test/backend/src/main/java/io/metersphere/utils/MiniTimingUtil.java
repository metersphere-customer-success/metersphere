package io.metersphere.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 对应 TimingItem 和 MiniTimingItem 两种时间组件的时间转换类
 */
public class MiniTimingUtil {

    //一小时
    private static final long hRatio = 60 * 60 * 1000;
    //一天
    private static final long dRatio = 24 * 60 * 60 * 1000;
    //一个月
    private static final long mRatio = 30 * dRatio;
    //一年
    private static final long yRatio = 12 * mRatio;

    public static long getTimeFromStr(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return 0;
        }
        if (dateStr.contains("D")) {
            return Long.parseLong(dateStr.replace("D", "")) * dRatio;
        } else if (dateStr.contains("M")) {
            return Long.parseLong(dateStr.replace("M", "")) * mRatio;
        } else if (dateStr.contains("Y")) {
            return Long.parseLong(dateStr.replace("Y", "")) * yRatio;
        } else {
            return Long.parseLong(dateStr.replace("H", "")) * hRatio;
        }
    }
}
