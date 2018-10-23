package com.zzmf.bonus.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author mzyupc@163.com
 * @date 2018/9/29 10:35
 */
public class Util {

    /**
     * java controller convert to JS service (DVA, UMI, etc.)
     *
     * @param controllerClass controller类
     * @param contextPath     项目部署的context path
     * @param isWriteFile     是否要输出文件
     * @param filePath        输出文件的路径
     * @throws IOException
     */
    public static void genJsServiceCode(Class<?> controllerClass, String contextPath, boolean isWriteFile, String filePath) throws IOException {
        String path = controllerClass.getAnnotation(RequestMapping.class).value()[0];
        StringBuilder result = new StringBuilder();
        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        for (Method method : controllerClass.getDeclaredMethods()) {
            String name = method.getName();
            String comment = method.getAnnotation(ApiOperation.class).value();

            result.append("\n// ").append(comment).append("\nexport async function ").append(name);
            RequestMethod requestMethod = method.getAnnotation(RequestMapping.class).method()[0];
            // 没有参数
            if (method.getParameters().length == 0) {
                result.append("(){\n").append("\treturn request(`").append(contextPath).append(path).append("/").append(name);
            } else {
                // 有参数
                result.append("(params){\n").append("\treturn request(`").append(contextPath).append(path).append("/").append(name);
            }

            // get请求
            if (requestMethod.equals(RequestMethod.GET)) {

                if (method.getParameters().length > 0) {
                    result.append("?");
                }

                for (String parameter : pnd.getParameterNames(method)) {
                    result.append(parameter).append("=${params.").append(parameter).append("}&");
                }

                if (result.lastIndexOf("&") == result.length() - 1) {
                    result.deleteCharAt(result.lastIndexOf("&"));
                }

                result.append("`);\n}\n");

                // post请求
            } else {
                result.append("`,{\n\t\tmethod:'POST',\n\t\tbody:params\n\t});\n}\n");
            }
        }

        System.out.println(result);

        if (isWriteFile) {
            Assert.isTrue(StringUtils.isEmpty(filePath), "文件路径不能为空!");
            File file = new File(filePath);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                bufferedWriter.write(result.toString());
                bufferedWriter.flush();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        genJsServiceCode(BonusController.class, "demo", false, null);
    }
}