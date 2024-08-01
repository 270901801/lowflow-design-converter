package com.lowflow.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommUtil {

    public static Map<String, Object> toMap(Object obj) {
        Boolean isStr = checkStr(obj);
        return isStr ? JSONObject.parseObject(obj.toString(), Map.class)
                : JSONObject.parseObject(JSONObject.toJSONString(obj), Map.class);
    }

    /**
     * https://www.idcnote.com/java/biji/1293.html
     *
     * @param obj
     * @return
     */
    private static Boolean checkStr(Object obj) {
        return obj instanceof String;
    }

    public static List<Map> toList(Object obj) {
        return JSON.parseArray(Objects.toString(obj), Map.class);
    }

    /**
     * 根据路径级别提取map中的信息
     *
     * @param map
     * @param selector xx > xx1 > xx2
     * @return
     */
    public static Object selectObj(Map map, String selector) throws Exception {
        if (StringUtils.isBlank(selector)) {
            throw new Exception("selector不能为空！");
        }
        Object result = null;
        try {
            List<String> list = Arrays.asList(replaceStrByBlank(selector, new String[]{" "}).split(">"));
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                String[] split = s.split(":nth-child");
                int index = -1;
                if (split.length>1) {
                    index = Integer.parseInt(CommUtil.replaceStrByBlank(split[1] ,new String[]{"(",")"}));
                }
                if (i == list.size() - 1) {
                    if (index>-1) {
                        List<Map> temp = (List<Map>) map.get(split[0]);
                        result = temp.get(index-1);
                    } else {
                        result = map.get(s);
                    }
                } else {
                    if (index>-1) {
                        List<Map> temp = (List<Map>) map.get(split[0]);
                        map = temp.get(index-1);
                    } else {
                        map = toMap(map.get(s));
                    }
                }
//                String s = list.get(i);
//                if (i == list.size() - 1) {
//                    result = map.get(s);
//                } else {
//                    map = toMap(map.get(s));
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return result==null?"":result;
    }

    public static void main(String[] args) {
//        String str = FileUtil.readAsString(new File("D:\\code\\end\\self\\ic-admin3\\eladmin-system\\src\\main\\resources\\tempFile\\temp.txt"));
//        Object o = CommUtil.selectObj(JSONObject.parseObject(str, Map.class), "jss > sitecore > route > placeholders > arrow-main:nth-child(3) > placeholders > product-details:nth-child(1) > fields > productDetail > buyingOptionAreaViewModel > groupedBuyingOptions > regionalBuyingOptions:nth-child(1) > buyingOptions:nth-child(1)");
////        Object o = CommUtil.selectObj(JSONObject.parseObject(str, Map.class), "jss > sitecore > route > placeholders > arrow-main:nth-child(3) > placeholders");
//        System.out.println(Objects.toString(o));

//        printToFile("test","saveErrMsg/test.txt");
        Matcher matcher = Pattern.compile("\\d+").matcher("52此产品有最大采购数量限制34");
        StringBuffer stringBuffer=new StringBuffer();
        if (matcher.find()) {
            stringBuffer.append(matcher.group());
        }
        System.out.println(stringBuffer);
    }

    public static String formatJSON(Object o) {
        return JSON.toJSONString(o, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty);
    }

    public static String replaceStrByBlank(String targetStr, String[] strings) {
        for (String string : strings) {
            targetStr = targetStr.replace(string, "");
        }
        return targetStr;
    }

    public static String getRegexStr(String inputText,String regex){
        Matcher matcher = Pattern.compile(regex).matcher(inputText);
        StringBuffer stringBuffer=new StringBuffer();
        while (matcher.find()) {
            stringBuffer.append(matcher.group());
        }
        return stringBuffer.toString();
    }

    public static void printToFile(String str, String filename) {
        Stopwatch started = Stopwatch.createStarted();
        File file = new File(filename);
        if (!file.getParentFile().exists()) {
            System.out.println("创建文件夹："+file.getParentFile().getName()+file.getParentFile().mkdirs());
        }
        writeStrToFile(str, file.getAbsolutePath());
        System.out.println("写入文件成功，耗费时间:" + started.elapsed(TimeUnit.SECONDS) + "秒");
    }

    private static void writeStrToFile(String str, String filePath) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath))) {
            bufferedWriter.write(str);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("文件名：" + filePath + "，写入文件过程失败：" + e.toString());
        }
    }

    public static void printLineBySplit(String str, String split) {
        if (str == null || split == null) {
            System.err.println("待分隔的字符串或者分隔符不能为空！");
        }
        Arrays.stream(str.split(split)).forEach(System.out::println);
    }

    public static int getRandomInt(int begin, int end) {
        return new Random().nextInt(end - begin) + begin;
    }

    public static Map returnMap(Map<String, Object> paramMap) {
        return (Map) paramMap.get("result");
    }

    public static String replaceStrByMap(String s, HashMap<String, String> map) {
        if (s == null) {
            return s;
        }
        for (Map.Entry<String, String> o : map.entrySet()) {
            s = s.replace(o.getKey(), o.getValue());
        }
        return s;
    }

    public static boolean checkNum(String numStr) {
        try {
            Integer.parseInt(numStr);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Map<String, Integer> groupByCount(List<String> strings) {
//        Map<String, List<IcCrawlerCookie>> listMap = icCrawlerCookies.stream().collect(Collectors.groupingBy(IcCrawlerCookie::getWebsite));
//        strings.stream().collect(Collectors.counting())
        return null;
    }

    @SneakyThrows
    public static void trimObjAttr(Object obj) {
        Class clazz = obj.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if ("java.lang.String".equals(declaredField.getType().getName())) {
                Object o = declaredField.get(obj);
                if (o != null) {
                    String value = (String) o;
                    declaredField.set(obj, value.trim());
                }
            }
        }
    }

    @SneakyThrows
    public static boolean allEmpty(Object obj) {
        Class clazz = obj.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Object o = declaredField.get(obj);
            if (o != null) {
                if ("java.lang.String".equals(declaredField.getType().getName())) {
                    if (!StringUtils.isBlank(o.toString())) {
                        return false;
                    } else {
                        continue;
                    }
                }
                return false;
            }
        }
        return true;
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return "";
        }
        return Objects.toString(obj);
    }

    public static String getCurDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String getCurDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
    }

    public static String getBetweenTimeStr(LocalDateTime after,LocalDateTime before ) {
        long secs = after.until(before, ChronoUnit.SECONDS);
        String result = "";
        long days = secs / (24 * 60 * 60);
        long hours = (secs / 60 * 60) % 24;
        long minutes = (secs / 60) % 60;
        long secss = secs % 60;
        if (days > 0) {
            result += days + "天";
        }
        if (hours > 0) {
            result += hours + "小时";
        }
        if (minutes > 0) {
            result += minutes + "分钟";
        }
        if (secss > 0) {
            result += secss + "秒";
        }
        return result;
    }

    public static String encodeIcModel(String icModel, String channel) {
        String result=null;
        switch (channel){
            case "findchips":
                result = icModel.replace("/","%2F");
                break;
            case "icnet":
                break;
            default:
                result = icModel;
                break;
        }
        return result;
    }

    public static boolean strContain(String remark, String[] strings) {
        for (String string : strings) {
            if (remark.contains(string)) {
                return true;
            }
        }
        return false;
    }

    public static void threadPollShutdown(ThreadPoolExecutor threadPool) {
        //所有正在执行的停止并销毁,返回未完成的任务
        List<Runnable> runnables = threadPool.shutdownNow();
        threadPool.shutdown();//不在接受新的任务
    }

    public static void log(String s, List<String> asList) {
        System.out.println("");
    }

    public static String ifNull(String s, String s1) {
        if (s==null) {
            return s1;
        }
        return s;
    }

    public static Map<String,String> getRandomItem(List<Map<String,String>> list) {
        int size = list.size();
        if (size==0) {
            return null;
        } else if (size == 1){
            return list.get(0);
        } else {
            return list.get(getRandomInt(1,size));
        }
    }
}
