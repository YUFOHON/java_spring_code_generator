package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class BuildMapperXml {
    private static final Logger logger = LoggerFactory.getLogger(BuildMapperXml.class);
    private static final String BASE_COLUMN_LIST = "base_column_list";
    private static final String BASE_QUERY_CONDITION = "base_query_condition";
    private static final String BASE_QUERY_CONDITION_EXTEND = "base_query_condition_extend";
    private static final String QUERY_CONDITION = "query_condition";

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPERS_XMLS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        //mapper的类名
        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPERS;
        //po的类名
        String poClass = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
        //主键的集合
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        FieldInfo idField = null;
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            if ("PRIMARY".equals(entry.getKey())) {
                List<FieldInfo> value = entry.getValue();
                if (value.size() == 1) {
                    idField = value.get(0);
                }
            }
        }
        File poFile = new File(folder, className + ".xml");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outw = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outw);
            //开始生成xml
            builderXmlHead(bw, className);
            //生成resultMap
            buildResultMap(tableInfo, poClass, idField, bw);
            //生成通用查询列
            buildCommonSelectCol(tableInfo, bw);
            //基础查询条件
            buildBaseSelectCondition(tableInfo, bw);
            //扩展查询条件
            buildExtendSelectCondition(tableInfo, bw);
            //通用查询条件
            buildCommonSelectCondition(bw);
            //查询列表
            buildSelectList(tableInfo, bw);
            //查询数量
            buildSelectCount(tableInfo, bw);
            //单条插入
            buildInsertOne(tableInfo, poClass, bw);
            //插入或者更新
            buildInsertOrUpdate(tableInfo, poClass, keyIndexMap, bw);
            //添加(批量插入)
            buildInsertBatch(tableInfo, poClass, bw);
            //批量插入或更新
            buildInsertOrUpdateBatch(tableInfo, poClass, bw);
            //根据索引进行增删改
            buildCRUDByIndex(tableInfo, poClass, keyIndexMap, bw);

            bw.newLine();
            bw.write("</mapper>");
            bw.flush();
        } catch (Exception e) {
            logger.error("创建mapperXML失败", e);
        } finally {
            closeAll(out, outw, bw, logger);
        }
    }

    private static void buildCRUDByIndex(TableInfo tableInfo, String poClass, Map<String, List<FieldInfo>> keyIndexMap, BufferedWriter bw) throws IOException {
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            StringBuilder methodName = new StringBuilder();
            StringBuilder paramNames = new StringBuilder();
            List<FieldInfo> keyFieldInfoList = entry.getValue();
            int index = 0;
            for (FieldInfo fieldInfo : keyFieldInfoList) {
                index++;
                methodName.append(StringUtils.uperCaseFirstLatter(fieldInfo.getPropertyName()));
                paramNames.append(fieldInfo.getFieldName() + "=#{" + fieldInfo.getPropertyName() + "}");
                if (index < keyFieldInfoList.size()) {
                    methodName.append("And");
                    paramNames.append(" and ");
                }
            }
            bw.newLine();
            bw.write("    <!-- 根据\"" + methodName + "\"查询-->");
            bw.newLine();
            bw.write("    <select id=\"selectBy" + methodName + "\" resultMap=\"base_result_map\">");
            bw.newLine();
            bw.write("        select <include refid=\"" + BASE_COLUMN_LIST + "\"/>  from " + tableInfo.getTableName() +
                    " where " + paramNames);
            bw.newLine();
            bw.write("    </select>");
            //构建更新方法
            bw.newLine();
            bw.write("    <!-- 根据\"" + methodName + "\"更新-->");
            bw.newLine();
            bw.write("    <update id=\"updateBy" + methodName + "\" parameterType=\""+ poClass + "\">");
            bw.newLine();
            bw.write("        update  " + tableInfo.getTableName());
            bw.newLine();
            bw.write("        <set>");
            bw.newLine();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write("            <if test=\"bean."+fieldInfo.getPropertyName()+"!=null\">");
                bw.newLine();;
                bw.write("                "+fieldInfo.getFieldName()+"=#{bean."+fieldInfo.getPropertyName()+"},");
                bw.newLine();
                bw.write("            </if>");
                bw.newLine();
            }
            bw.write("        </set>");
            bw.newLine();
            bw.write("        <where>");
            bw.newLine();
            bw.write("            "+paramNames);
            bw.newLine();
            bw.write("        </where>");
            bw.newLine();
            bw.write("    </update>");
            bw.newLine();

//                构建删除方法
            bw.newLine();
            bw.write("    <!-- 根据\"" + methodName + "\"删除-->");
            bw.newLine();
            bw.write("    <delete id=\"deleteBy" + methodName + "\">");
            bw.newLine();
            bw.write("        delete from " + tableInfo.getTableName()+" where "+paramNames);
            bw.newLine();
            bw.write("    </delete>");
            bw.newLine();
        }
    }

    static void closeAll(OutputStream out, OutputStreamWriter outw, BufferedWriter bw, Logger logger) {
        try {
            if (bw != null) {
                bw.close();
            }
            if (outw != null) {
                outw.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            logger.info("关闭流失败", e);
        }
    }

    private static void buildInsertOrUpdateBatch(TableInfo tableInfo, String poClass, BufferedWriter bw) throws IOException {
        bw.newLine();
        bw.write("    <!--批量插入或更新 -->");
        bw.newLine();
        bw.write("    <insert id=\"insertOrUpdateBatch\" parameterType=\"" + poClass + "\">");
        bw.newLine();
        insertBatchSamePath(tableInfo, bw);
        bw.newLine();
        bw.write("        on DUPLICATE key update");
        bw.newLine();
        StringBuilder insertBatchUpdateBuilder = new StringBuilder();
        insertBatchUpdateBuilder.append("            ");
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            insertBatchUpdateBuilder.append(fieldInfo.getFieldName() + "= VALUES(" + fieldInfo.getFieldName() + "),");
        }
        String batchInsertOrUpdate = insertBatchUpdateBuilder.substring(0, insertBatchUpdateBuilder.lastIndexOf(","));
        bw.write(batchInsertOrUpdate);
        bw.newLine();
        bw.write("    </insert>");
    }

    private static void buildInsertBatch(TableInfo tableInfo, String poClass, BufferedWriter bw) throws IOException {
        bw.write("    <!--添加(批量插入)-->");
        bw.newLine();
        bw.write("    <insert id=\"insertBatch\" parameterType=\"" + poClass + "\">");
        bw.newLine();
        insertBatchSamePath(tableInfo, bw);
        bw.newLine();
        bw.write("    </insert>");
    }

    private static void insertBatchSamePath(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        StringBuilder insertFileStringBuilder = new StringBuilder();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (fieldInfo.getAutoIncrement()) {
                continue;
            }
            insertFileStringBuilder.append(fieldInfo.getFieldName()).append(",");
        }
        String string = insertFileStringBuilder.substring(0, insertFileStringBuilder.lastIndexOf(","));
        bw.write("        insert into " + tableInfo.getTableName() + "(" + string + ")values");
        bw.newLine();
        bw.write("        <foreach collection=\"list\" item=\"item\" separator=\",\">");
        bw.newLine();
        StringBuilder insertPropertyBuilder = new StringBuilder();
//        insertPropertyBuilder.append("            ");
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (fieldInfo.getAutoIncrement()) {
                continue;
            }
            insertPropertyBuilder.append("#{item." + fieldInfo.getPropertyName() + "}").append(",");
        }
        String substring = insertPropertyBuilder.substring(0, insertPropertyBuilder.lastIndexOf(","));
        bw.write("            ("+substring+")");
        bw.newLine();
        bw.write("        </foreach>");
    }

    private static void buildInsertSamePart(BufferedWriter bw, TableInfo tableInfo) throws Exception {
        bw.newLine();
        bw.write("        insert into " + tableInfo.getTableName());
        bw.newLine();
        bw.write("        <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write("            <if test=\"bean." + fieldInfo.getPropertyName() + "!=null\">");
            bw.newLine();
            bw.write("                " + fieldInfo.getFieldName() + ",");
            bw.newLine();
            bw.write("            </if>");
            bw.newLine();
        }
        bw.newLine();
        bw.write("        </trim>");

        bw.newLine();
        //生成values()
        bw.write("        <trim prefix=\"values(\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write("            <if test=\"bean." + fieldInfo.getPropertyName() + "!=null\">");
            bw.newLine();
            bw.write("                #{bean." + fieldInfo.getPropertyName() + "},");
            bw.newLine();
            bw.write("            </if>");
            bw.newLine();
        }
        bw.write("        </trim>");
        bw.newLine();
    }

    private static void buildInsertOrUpdate(TableInfo tableInfo, String poClass, Map<String, List<FieldInfo>> keyIndexMap, BufferedWriter bw) throws Exception {
        bw.write("    <!--插入或者更新(匹配有值的字段)-->");
        bw.newLine();
        bw.write("\t<insert id=\"insertOrUpdate\"  parameterType=\"" + poClass + "\">");
        buildInsertSamePart(bw, tableInfo);
        //生成duplicate
        Set<String> tempSet = new HashSet<>();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            List<FieldInfo> value = entry.getValue();
            for (FieldInfo item : value) {
                tempSet.add(item.getFieldName());
            }
        }
        bw.write("        on DUPLICATE key update");
        bw.newLine();
        bw.write("        <trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (tempSet.contains(fieldInfo.getFieldName())) {
                continue;
            }
            bw.write("            <if test=\"bean." + fieldInfo.getPropertyName() + "!=null\">");
            bw.newLine();
            bw.write("                " + fieldInfo.getFieldName() + " =VALUES(" + fieldInfo.getFieldName() + "),");
            bw.newLine();
            bw.write("            </if>");
            bw.newLine();
        }
        bw.newLine();
        bw.write("        </trim>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
    }

    private static void buildInsertOne(TableInfo tableInfo, String poClass, BufferedWriter bw) throws Exception {
        bw.write("    <!--插入(匹配有值的字段)-->");
        bw.newLine();
        bw.write("\t<insert id=\"insert\"  parameterType=\"" + poClass + "\">");

        bw.newLine();
        FieldInfo autoIncrementField = null;
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (fieldInfo.getAutoIncrement() != null && fieldInfo.getAutoIncrement()) {
                autoIncrementField = fieldInfo;
                break;
            }
        }
        if (autoIncrementField != null) {
            bw.write("\t\t<selectKey keyProperty=\"bean." + autoIncrementField.getFieldName() + "\" resultType=\"" + autoIncrementField.getJavaType() + "\" order=\"AFTER\">");
            bw.newLine();
            bw.write("\t\t\tSELECT LAST_INSERT_ID();");
            bw.newLine();
            bw.write("\t\t</selectKey>");
        }
        buildInsertSamePart(bw, tableInfo);
        bw.write("\t</insert>");
        bw.newLine();
    }

    private static void buildSelectCount(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write("    <!--查询数量-->");
        bw.newLine();
        bw.write("\t<select id=\"selectCount\"  resultType=\"java.lang.Integer\">");
        bw.newLine();
        bw.write("\t\tSELECT count(1) FROM " + tableInfo.getTableName() + " <include refid=\"" + QUERY_CONDITION + "\"/>");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
    }

    private static void buildSelectList(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write("    <!--查询列表-->");
        bw.newLine();
        bw.write("\t<select id=\"selectList\"  resultMap=\"base_result_map\">");
        bw.newLine();
        bw.write("\t\tSELECT <include refid=\"" + BASE_COLUMN_LIST + "\"/> FROM " + tableInfo.getTableName() + " <include refid=\"" +
                QUERY_CONDITION + "\"/>");
        bw.newLine();
        bw.write("\t\t<if test=\"query.orderBy!=null\"> order by ${query.orderBy}</if>");
        bw.newLine();
        bw.write("\t\t<if test=\"query.simplePage!=null\">limit #{query.simplePage.start},#{query.simplePage.end}</if>");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
    }

    private static void buildCommonSelectCondition(BufferedWriter bw) throws IOException {
        bw.write("    <!--通用查询条件-->");
        bw.newLine();
        bw.write("    <sql id=\"" + QUERY_CONDITION + "\">");
        bw.newLine();
        bw.write("\t\t<where>");
        bw.newLine();
        bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION + "\"/>");
        bw.newLine();
        bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION_EXTEND + "\"/>");
        bw.newLine();
        bw.write("\t\t</where>");
        bw.newLine();
        bw.write("    </sql>");
        bw.newLine();
    }

    private static void buildExtendSelectCondition(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write("    <!--扩展查询条件-->");
        bw.newLine();
        bw.write("    <sql id=\"" + BASE_QUERY_CONDITION_EXTEND + "\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldExtendList()) {
            String andWhere = "";
            if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                andWhere = "and " + fieldInfo.getFieldName() + " like concat('%',#{query." + fieldInfo.getPropertyName() + "},'%')";
            } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType()) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())) {
                if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_START)) {
                    andWhere = "<![CDATA[ and  " + fieldInfo.getFieldName() + " >= str_to_date(#{query." + fieldInfo.getPropertyName() + "},'%Y-%m-%d') ]]>";
                } else if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_END)) {
                    andWhere = "<![CDATA[ and  " + fieldInfo.getFieldName() + " < date_sub(str_to_date(#{query." + fieldInfo.getPropertyName() + "},'%Y-%m-%d')," +
                            "interval -1 day) ]]>";
                }
            }
            bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + "!=null" + " and query." + fieldInfo.getPropertyName() + "!=''" + "\">");
            bw.newLine();
            bw.write("\t\t\t" + andWhere);
            bw.newLine();
            bw.write("\t\t</if>");
            bw.newLine();
        }
        bw.write("    </sql>");
        bw.newLine();
    }

    private static void buildBaseSelectCondition(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.write("    <!--基础查询条件-->");
        bw.newLine();
        bw.write("    <sql id=\"" + BASE_QUERY_CONDITION + "\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            String stringQuery = "";
            if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                stringQuery = " and query." + fieldInfo.getPropertyName() + "!=''";
            }
            bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + "!=null" + stringQuery + "\">");
            bw.newLine();
            bw.write("\t\t\tand " + fieldInfo.getPropertyName() + "=#{query." + fieldInfo.getPropertyName() + "} ");
            bw.newLine();
            bw.write("\t\t</if>");
            bw.newLine();

        }
        bw.write("    </sql>");
        bw.newLine();
    }

    private static void buildCommonSelectCol(TableInfo tableInfo, BufferedWriter bw) throws IOException {

        bw.write("    <!--通用查询结果列-->");
        bw.newLine();
        bw.write("    <sql id=\"" + BASE_COLUMN_LIST + "\">");
        bw.newLine();
        StringBuilder columnBuilder = new StringBuilder();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            columnBuilder.append(fieldInfo.getFieldName()).append(",");
        }
        String columnBuilderStr = columnBuilder.substring(0, columnBuilder.lastIndexOf(","));
        bw.write("\t" + columnBuilderStr);
        bw.newLine();
        bw.write("    </sql>");
        bw.newLine();
        bw.newLine();
    }

    private static void buildResultMap(TableInfo tableInfo, String poClass, FieldInfo idField, BufferedWriter bw) throws IOException {
        bw.write("    <!--实体映射-->");
        bw.newLine();
        bw.write("    <resultMap id=\"base_result_map\" type=\"" + poClass + "\">");

        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write("        <!--" + fieldInfo.getComment() + "-->");
            bw.newLine();
            String key = "";
            if (idField != null && idField.getPropertyName().equals(fieldInfo.getPropertyName())) {
                key = "id";
            } else {
                key = "result";
            }
            bw.write("        <" + key + " column=\"" + fieldInfo.getFieldName() + "\" property=\"" + fieldInfo.getPropertyName() + "\"/>");
            bw.newLine();

        }
        bw.write("    </resultMap>");
        bw.newLine();
    }

    private static void builderXmlHead(BufferedWriter bw, String className) throws Exception {
        //生成固定头
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        bw.newLine();
        bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"");
        bw.newLine();
        bw.write("        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        bw.newLine();
        bw.write("<mapper namespace=\"" + Constants.PACKAGE_MAPPERS + "." + className + "\">");
        bw.newLine();

    }
}
