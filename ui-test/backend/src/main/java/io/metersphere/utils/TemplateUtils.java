package io.metersphere.utils;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtils {

    private static Pattern varPattern = Pattern.compile("(\\$\\{(\\w+)\\})");

    /**
     * 替换形如 ${cmdName} 的形式参数
     *
     * @param originalTemplate
     * @param paramMap
     * @return
     */
    public static String replaceVars(String originalTemplate, Map paramMap) {
        if (StringUtils.isBlank(originalTemplate)) {
            return originalTemplate;
        }
        Matcher m = varPattern.matcher(originalTemplate);
        while (m.find()) {
            if (StringUtils.isNotBlank(m.group(2)) && paramMap.get(m.group(2)) != null) {
                originalTemplate = originalTemplate.replace(m.group(1), String.valueOf(paramMap.get(m.group(2))));
            }
        }
        return originalTemplate;
    }

    public static String replaceVarsOnce(String originalTemplate, Map paramMap) {
        Matcher m = varPattern.matcher(originalTemplate);
        //只替换一次 target value
        originalTemplate = replaceParamOnce(originalTemplate, paramMap);
        while (m.find()) {
            if (StringUtils.isNotBlank(m.group(2)) && paramMap.get(m.group(2)) != null) {
                originalTemplate = originalTemplate.replace(m.group(1), String.valueOf(paramMap.get(m.group(2))));
            }
        }
        return originalTemplate;
    }

    public static String replaceParamOnce(String str, Map paramMap) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        str = replace(str, "${target}", String.valueOf(paramMap.get("target")), true);
        str = replace(str, "${value}", String.valueOf(paramMap.get("value")), false);
        paramMap.remove("target");
        paramMap.remove("value");
        return str;
    }

    /**
     * 替换最开始出现的字符串或者最后出现的字符串
     *
     * @param string  原始字符串
     * @param find    需要被替换的字符串
     * @param replace 需要替换的字符串
     * @param first   true 替换第一次出现的字符串，false 替换最后一次出现的字符串
     * @return
     */
    public static String replace(String string, String find, String replace, boolean first) {
        int index = -1;
        if (first) {
            index = string.indexOf(find);
        } else {
            index = string.lastIndexOf(find);
        }
        if (index == -1) {
            return string;
        }

        String beginString = string.substring(0, index);
        String endString = string.substring(index + find.length());

        return beginString + replace + endString;
    }

    public static String readContent(String path) {
        return readContent(new File(path));
    }

    public static String readContent(InputStream in) {
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            StringBuffer content = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line + "\n");
            }
            reader.close();
            inputStreamReader.close();
            return content.toString();
        } catch (IOException e) {
            LogUtil.error(e);
            MSException.throwException("读取UI模板出错！");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                MSException.throwException(e.getMessage());
                LogUtil.error(e.getMessage(), e);
            }
        }
        return "";
    }

    public static String readContent(File file) {
        if (!file.exists()) {
            return "";
        }
        try (
                FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader);
        ) {
            StringBuffer content = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line + "\n");
            }
            reader.close();
            fileReader.close();
            return content.toString();
        } catch (IOException e) {
            LogUtil.error(e);
            MSException.throwException("读取UI模板出错！");
        }
        return "";
    }


    public static String escapeQuotes(String target) {
        if (StringUtils.isBlank(target)) {
            return "";
        }
        return target.replace("\"", "\\\"");
    }

    public static String resetQuotes(String target) {
        if (StringUtils.isBlank(target)) {
            return "";
        }
        return target.replace("\\", "");
    }

    /**
     * 迁移
     *
     * @param originJSON
     * @return
     */
    public static String replaceClassName(String originJSON) {
        if (StringUtils.isBlank(originJSON)) {
            return originJSON;
        }
        return originJSON.replace("io.metersphere.xpack.ui.hashtree", "io.metersphere.hashtree");
    }

    public static String escapeSqlSpecialChars(String str) {
        if (StringUtils.isNotBlank(str)) {
            str = str.replaceAll("\\\\", "\\\\\\\\");
            str = str.replaceAll("_", "\\\\_");
            str = str.replaceAll("%", "\\\\%");
        }
        return str;
    }

    public static void main(String[] args) {
        String script = "${b} ${a}!";
        Map map = new HashMap();
        map.put("a", "world");
        map.put("b", "hello");
        System.out.println(replaceVars(script, map));
    }
}
