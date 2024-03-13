package com.siyufeng.cli.example;


import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

class Login implements Callable<Integer> {
    @Option(names = {"-u", "--user"}, description = "User name")
    String user;

    @Option(names = {"-p", "--password"}, arity = "0..1", description = "Passphrase", interactive = true,prompt = "请输入密码")
    String password;

    @Option(names = {"-cp", "--checkPassword"}, description = "Check Password", interactive = true)
    String checkPassword;


    public Integer call() throws Exception {
        // 打印出密码
        System.out.println("password：" + password);
        System.out.println("checkPassword:" + checkPassword);
        return 0;
    }

    public static void main(String[] args) {
        String input[] = {"-u", "user123", "-p", "xxx"};
        String[] inputs = getFinalInput(input);
        new CommandLine(new Login()).execute(inputs);
    }

    //得到用户输入并经过强制交互后的数据
    private static String[] getFinalInput(String[] input) {
        //Arrays.asList不能修改
        List<String> inputList = Arrays.asList(input);
        List<String> list = new ArrayList<>();
        list.addAll(inputList);
        list = forceInteractive(list);
        input = new String[list.size()];
        input = list.toArray(input);
        return input;
    }


    //强制用户交互，利用反射读取注解的字段
    public static List<String> forceInteractive(List<String> inputList) {
        Class<Login> loginClass = Login.class;
        Field[] declaredFields = loginClass.getDeclaredFields();
        for (Field field : declaredFields) {
            Option option = field.getAnnotation(Option.class);
            String[] names = option.names();
            if (!inputList.contains(names[0])) {
                inputList.add(names[0]);
            }
        }
        return inputList;
    }
}