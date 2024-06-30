package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.utils.DateUtils;

import java.io.BufferedWriter;
import java.util.Date;

public class BuildComment {
    //创建类上的注解
    public static void createClassComment(BufferedWriter bw,String classComment)throws Exception{
        bw.write("/**");
        bw.newLine();
        bw.write(" * @Description "+classComment);
        bw.newLine();
        bw.write(" * @author: "+ Constants.AUTHER_COMMENT);
        bw.newLine();
        bw.write(" * @date "+ DateUtils.format(new Date(),DateUtils._YYYYMMDD));
        bw.newLine();
        bw.write(" */");

    }
    //创建字段上的注解
    public static void createFieldComment(BufferedWriter bw,String fieldComment)throws Exception{
        bw.write("    /**");
        bw.newLine();
        bw.write("     * "+ (fieldComment==null?"":fieldComment));

        bw.newLine();
        bw.write("     */");
        bw.newLine();
    }
    //创建方法上的注解
    public static void createMethodComment(BufferedWriter bw)throws Exception{

    }
}
