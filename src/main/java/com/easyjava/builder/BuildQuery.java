package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.DateUtils;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BuildQuery {
    private static final Logger logger = LoggerFactory.getLogger(BuildQuery.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_QUERY);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName() + Constants.SUFFIX_BEAN_QUERY;
        File poFile = new File(folder, className + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outw);
            //先导包名
            bw.write("package " + Constants.PACKAGE_QUERY + ";");
            bw.newLine();
            bw.newLine();
            //import需要的类
            if (tableInfo.getHaveDate() || tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
            }
            if (tableInfo.getHaveBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
            }
            bw.newLine();
            bw.newLine();


            //类的注释
            BuildComment.createClassComment(bw, tableInfo.getComment() + "查询对象");
            bw.newLine();
            //创建类
            bw.write("public class " + className + " extends BaseQuery{");
            bw.newLine();

            //每个字段
            for (FieldInfo field : tableInfo.getFieldList()) {
                //字段的注释
                BuildComment.createFieldComment(bw, field.getComment());
                bw.write("\tprivate " + field.getJavaType() + " " + field.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();
                //String类型
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, field.getSqlType())) {
                    bw.write("\tprivate " + field.getJavaType() + " " + field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_FUZZY + ";");
                    bw.newLine();
                    bw.newLine();
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, field.getSqlType()) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, field.getSqlType())) {
                    bw.write("\tprivate String" + " " + field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_START + ";");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tprivate String" + " " + field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_END + ";");
                    bw.newLine();
                    bw.newLine();

                }
            }
            //get和set方法
            buildGetAndSet(tableInfo.getFieldList(), bw);
            buildGetAndSet(tableInfo.getFieldExtendList(), bw);
            bw.write("}");
            bw.flush();
        } catch (Exception e) {
            logger.error("创建po失败", e);
        } finally {
            BuildMapperXml.closeAll(out, outw, bw, logger);
        }
    }

    private static void buildGetAndSet(List<FieldInfo> fieldList, BufferedWriter bw) throws Exception {
        //get 和set方法
        for (FieldInfo field : fieldList) {
            //set方法
            String tempField = StringUtils.uperCaseFirstLatter(field.getPropertyName());
            bw.write("\tpublic void set" + tempField + "(" + field.getJavaType() + " " + field.getPropertyName() + ") {");
            bw.newLine();
            bw.write("        this." + field.getPropertyName() + " = " + field.getPropertyName() + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();
            //get方法
            bw.write("\tpublic " + field.getJavaType() + " get" + tempField + "() {");
            bw.newLine();
            bw.write("        return this." + field.getPropertyName() + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();
        }
    }
}
