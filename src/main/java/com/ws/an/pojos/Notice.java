package com.ws.an.pojos;

import com.ws.an.properties.enums.ProjectEnviroment;
import org.springframework.util.DigestUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.toList;
/**
 * @author WuSong
 * @version 1.0
 * @date 2023/1/6 23:34
 * @description
 */
public class Notice extends AbnormalNotice {

    /**
     * 工程名
     */
    protected String project;

    /**
     * 异常的标识码
     */
    protected String uid;

    /**
     * 方法名
     */
    protected String methodName;

    /**
     * 方法参数信息
     */
    protected List<Object> parames;

    /**
     * 类路径
     */
    protected String classPath;

    /**
     * 异常信息
     */
    protected List<String> exceptionMessage;

    /**
     * 异常追踪信息
     */
    protected List<String> traceInfo = new ArrayList<>();

    /**
     * 出现次数
     */
    protected Long showCount = 1L;

    /**
     * @param title
     * @param projectEnviroment
     */
    public Notice(Throwable ex, String filterTrace, Object[] args, ProjectEnviroment projectEnviroment,
                  String title) {
        super(title, projectEnviroment);
        this.exceptionMessage = gainExceptionMessage(ex);
        this.parames = args == null ? null : Arrays.stream(args).collect(toList());
        List<StackTraceElement> list = this.stackTrace(ex, filterTrace);
        if (list.size() > 0) {
            this.traceInfo = list.stream().map(x -> x.toString()).collect(toList());
            this.methodName = list.get(0).getMethodName();
            this.classPath = list.get(0).getClassName();
        }
        this.uid = calUid();
    }

    private List<String> gainExceptionMessage(Throwable exception) {
        List<String> list = new LinkedList<String>();
        gainExceptionMessage(exception, list);
        return list;
    }

    private void gainExceptionMessage(Throwable throwable, List<String> list) {
        list.add(String.format("%s:%s", throwable.getClass().getName(), throwable.getMessage()));
        Throwable cause = throwable.getCause();
        if (cause != null)
            gainExceptionMessage(cause, list);
    }

    private List<StackTraceElement> stackTrace(Throwable throwable, String filterTrace) {
        List<StackTraceElement> list = new LinkedList<StackTraceElement>();
        addStackTrace(list, throwable, filterTrace);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            addStackTrace(list, cause, filterTrace);
            cause = cause.getCause();
        }
        return list;
    }

    public void addStackTrace(List<StackTraceElement> list, Throwable throwable, String filterTrace) {
        list.addAll(0,
                Arrays.stream(throwable.getStackTrace())
                        .filter(x -> filterTrace == null ? true : x.getClassName().startsWith(filterTrace))
                        .filter(x -> !x.getFileName().equals("<generated>")).collect(toList()));
    }

    private String calUid() {
        String md5 = DigestUtils.md5DigestAsHex(
                String.format("%s-%s", exceptionMessage, traceInfo.size() > 0 ? traceInfo.get(0) : "").getBytes());
        return md5;
    }

    public String createText() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("工程信息：").append(project).append("(").append(projectEnviroment.getName()).append(")")
                .append("\r\n");
        stringBuilder.append("类路径：").append(classPath).append("\r\n");
        stringBuilder.append("方法名：").append(methodName).append("\r\n");
        if (parames != null && parames.size() > 0) {
            stringBuilder.append("参数信息：")
                    .append(String.join(",", parames.stream().map(x -> x.toString()).collect(toList()))).append("\r\n");
        }
        stringBuilder.append("异常信息：").append(String.join("\r\n caused by: ", exceptionMessage)).append("\r\n");
        stringBuilder.append("异常追踪：").append("\r\n").append(String.join("\r\n", traceInfo)).append("\r\n");
        stringBuilder.append("最后一次出现时间：").append(createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\r\n");
        stringBuilder.append("出现次数：").append(showCount).append("\r\n");
        return stringBuilder.toString();

    }

}
