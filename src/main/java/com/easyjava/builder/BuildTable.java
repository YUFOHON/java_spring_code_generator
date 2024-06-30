package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.JsonUtils;
import com.easyjava.utils.PropertiesUtils;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class BuildTable {

    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    private static Connection conn = null;
    private static String SQL_SHOW_TABLE_STATUS = "show table status";
    private static String SQL_SHOW_TABLE_FIELDS = "show full fields from %s";
    private static String SQL_SHOW_TABLE_INDEX = "show  index from %s";
    static {
        String driverName = PropertiesUtils.getString("db.driver.name");
        String url = PropertiesUtils.getString("db.url");
        String user = PropertiesUtils.getString("db.username");
        String password = PropertiesUtils.getString("db.password");
        try {
            Class.forName(driverName);
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            logger.error("database connection fail", e);
        }
    }

    public static List<TableInfo> getTables() {
        PreparedStatement ps = null;
        ResultSet tableResult = null;

        List<TableInfo> tableInfoList = new ArrayList<>();
        try {
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResult = ps.executeQuery();
            while (tableResult.next()) {
                String tableName = tableResult.getString("name");
                String comment = tableResult.getString("comment");
                logger.info("tableName:{},comment:{}", tableName, comment);


                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setComment(comment);
                String beanName = tableName;
                if (Constants.IGNORE_TABLE_PREFIX) {
                    beanName = tableName.substring(beanName.indexOf("_" + 1));
                }
                beanName = processField(beanName, true);
                tableInfo.setBeanName(beanName);
                tableInfo.setBeanParamName(beanName + Constants.SUFFIX_BEAN_QUERY);
                readFieldInfo(tableInfo);
                getKeyIndexInfo(tableInfo);
                tableInfoList.add(tableInfo);
            }
        } catch (Exception e) {
            logger.error("read table fail");
        } finally {
            try {
                if (tableResult != null) {
                    tableResult.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("resultSet or ps close fail", e);
            }
        }
        return tableInfoList;
    }

    /**
     * @param field               原始数据
     * @param uperCaseFirstLatter 是否要将第一个字母大写
     * @return 将字符串处理成对应的bean
     */
    private static String processField(String field, Boolean uperCaseFirstLatter) {
        StringBuilder sb = new StringBuilder();
        String[] fields = field.split("_");
        sb.append(uperCaseFirstLatter ? StringUtils.uperCaseFirstLatter(fields[0]) : fields[0]);
        for (int i = 1; i < fields.length; i++) {
            sb.append(StringUtils.uperCaseFirstLatter(fields[i]));
        }
        return sb.toString();
    }

    /**
     * get field info
     *
     * @param tableInfo
     * @return
     */
    private static void readFieldInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        List<FieldInfo> fieldExtendList = new ArrayList<>();
        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();
            tableInfo.setHaveDateTime(false);
            tableInfo.setHaveDate(false);
            tableInfo.setHaveBigDecimal(false);
            while (fieldResult.next()) {
                String field = fieldResult.getString("field");
                String type = fieldResult.getString("type");
                String comment = fieldResult.getString("comment");
                String extra = fieldResult.getString("extra");
                if (type.indexOf("(") > 0) {
                    type = type.substring(0, type.indexOf("("));
                }
                String propertyName = processField(field, false);
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.setFieldName(field);
                fieldInfo.setComment(comment);
                fieldInfo.setSqlType(type);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra));
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setJavaType(processJavaType(type));
                //是否有date类型   方便后续使用
                if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type)){
                    tableInfo.setHaveDateTime(true);
                }
                if(ArrayUtils.contains(Constants.SQL_DATE_TYPES,type)){
                    tableInfo.setHaveDate(true);
                }
                if(ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
                    tableInfo.setHaveBigDecimal(true);
                }
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
                    FieldInfo fuzzyField = new FieldInfo();
                    fuzzyField.setJavaType(fieldInfo.getJavaType());
                    fuzzyField.setPropertyName(propertyName+Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    fuzzyField.setFieldName(fieldInfo.getFieldName());
                    fuzzyField.setSqlType(type);
                    fieldExtendList.add(fuzzyField);
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
                    FieldInfo startField = new FieldInfo();
                    startField.setJavaType("String");
                    startField.setPropertyName(fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_START);
                    startField.setFieldName(fieldInfo.getFieldName());
                    startField.setSqlType(type);
                    fieldExtendList.add(startField);

                    FieldInfo endField = new FieldInfo();
                    endField.setJavaType("String");
                    endField.setPropertyName(fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_END);
                    endField.setFieldName(fieldInfo.getFieldName());
                    endField.setSqlType(type);
                    fieldExtendList.add(endField);
                }

                fieldInfoList.add(fieldInfo);
            }
            tableInfo.setFieldList(fieldInfoList);
            tableInfo.setFieldExtendList(fieldExtendList);
        } catch (Exception e) {
            logger.error("Failed to read field");
        } finally {
            try {
                if (fieldResult != null) {
                    fieldResult.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                logger.error("resultSet或者ps关闭失败", e);
            }
        }
    }


    /**
     * get key index info
     * @param tableInfo
     * @return
     */
    private static void getKeyIndexInfo(TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;
        try {
            Map<String,FieldInfo>tempMap=new HashMap<>();
            for (FieldInfo fieldInfo:tableInfo.getFieldList()){
                tempMap.put(fieldInfo.getFieldName(),fieldInfo);
            }
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();
            while (fieldResult.next()) {
                String keyName = fieldResult.getString("key_name");
                Integer nonUnique = fieldResult.getInt("non_unique");
                String columnName = fieldResult.getString("column_name");
                if(nonUnique==1){
                    continue;
                }
                List<FieldInfo> keyFieldList = tableInfo.getKeyIndexMap().get(keyName);
                if(null==keyFieldList){
                    keyFieldList=new ArrayList<>();
                    tableInfo.getKeyIndexMap().put(keyName,keyFieldList);
                }
                keyFieldList.add(tempMap.get(columnName));
            }
        } catch (Exception e) {
            logger.error("read index fail");
        } finally {
            try {
                if (fieldResult != null) {
                    fieldResult.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                logger.error("resultSet或者ps关闭失败", e);
            }
        }

    }
    private static String processJavaType(String type) {
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)) {
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
            return "BigDecimal";
        } else {
            throw new RuntimeException("无法识别的类型" + type);
        }
    }
}
