package com.easyjava.bean;

import com.easyjava.utils.PropertiesUtils;

public class Constants {
    public static Boolean IGNORE_TABLE_PREFIX;
    public static String SUFFIX_BEAN_QUERY;
    public static String SUFFIX_MAPPERS;

    private static String PATH_JAVA="java";
    private static String PATH_RESOURCES="resources";

    public static String PATH_BASE;
    public static String PATH_PO;
    public static String PATH_QUERY;
    public static String PATH_VO;
    public static String PATH_UTILS;
    public static String PATH_ENUMS;
    public static String PATH_EXCEPTION;
    public static String PATH_MAPPERS;
    public static String PATH_MAPPERS_XMLS;
    public static String PATH_SERVICE;
    public static String PATH_SERVICE_IMPL;
    public static String PATH_CONTROLLER;

    public static String PACKAGE_PO;
    public static String PACKAGE_QUERY;
    public static String PACKAGE_VO;
    public static String PACKAGE_BASE;
    public static String PACKAGE_UTIL;
    public static String PACKAGE_ENUMS;
    public static String PACKAGE_EXCEPTION;
    public static String PACKAGE_MAPPERS;
    public static String PACKAGE_SERVICE;
    public static String PACKAGE_SERVICE_IMPL;
    public static String PACKAGE_CONTROLLER;

    public static String AUTHER_COMMENT;

    public static String SUFFIX_BEAN_QUERY_FUZZY;
    public static String SUFFIX_BEAN_QUERY_TIME_START;
    public static String SUFFIX_BEAN_QUERY_TIME_END;
    //需要忽略的属性
    public static String IGNORE_BEAN_TOJSON_FILED;
    public static String IGNORE_BEAN_TOJSON_EXPRESSION;
    public static String IGNORE_BEAN_TOJSON_CLASS;

    //日期序列化与反序列化
    public static String BEAN_DATE_FORMAT_EXPRESSION;
    public static String BEAN_DATE_FORMAT_CLASS;

    public static String BEAN_DATE_UNFORMAT_EXPRESSION;
    public static String BEAN_DATE_UNFORMAT_CLASS;

    static {

        IGNORE_BEAN_TOJSON_FILED=PropertiesUtils.getString("ignore.bean.tojson.filed");
        IGNORE_BEAN_TOJSON_EXPRESSION=PropertiesUtils.getString("ignore.bean.tojson.expression");
        IGNORE_BEAN_TOJSON_CLASS=PropertiesUtils.getString("ignore.bean.tojson.class");

        SUFFIX_BEAN_QUERY_FUZZY=PropertiesUtils.getString("suffix.bean.query.fuzzy");
        SUFFIX_BEAN_QUERY_TIME_START=PropertiesUtils.getString("suffix.bean.query.time.start");
        SUFFIX_BEAN_QUERY_TIME_END=PropertiesUtils.getString("suffix.bean.query.time.end");
        SUFFIX_MAPPERS=PropertiesUtils.getString("suffix.mappers");

        BEAN_DATE_FORMAT_EXPRESSION=PropertiesUtils.getString("bean.date.format.expression");
        BEAN_DATE_FORMAT_CLASS=PropertiesUtils.getString("bean.date.format.class");
        BEAN_DATE_UNFORMAT_EXPRESSION=PropertiesUtils.getString("bean.date.unformat.expression");
        BEAN_DATE_UNFORMAT_CLASS=PropertiesUtils.getString("bean.date.unformat.class");

        AUTHER_COMMENT=PropertiesUtils.getString("auther.comment");

        IGNORE_TABLE_PREFIX=Boolean.valueOf(PropertiesUtils.getString("ignore.table.prefix"));
        SUFFIX_BEAN_QUERY=PropertiesUtils.getString("suffix.bean.query");


        PACKAGE_BASE=PropertiesUtils.getString("package.base");
        PACKAGE_PO=PACKAGE_BASE+"."+PropertiesUtils.getString("package.po");
        PACKAGE_QUERY=PACKAGE_BASE+"."+PropertiesUtils.getString("package.query");
        PACKAGE_VO=PACKAGE_BASE+"."+PropertiesUtils.getString("package.vo");
        PACKAGE_UTIL=PACKAGE_BASE+"."+PropertiesUtils.getString("package.utils");
        PACKAGE_ENUMS=PACKAGE_BASE+"."+PropertiesUtils.getString("package.enums");
        PACKAGE_EXCEPTION=PACKAGE_BASE+"."+PropertiesUtils.getString("package.exception");
        PACKAGE_MAPPERS=PACKAGE_BASE+"."+PropertiesUtils.getString("package.mappers");
        PACKAGE_SERVICE=PACKAGE_BASE+"."+PropertiesUtils.getString("package.service");
        PACKAGE_SERVICE_IMPL=PACKAGE_BASE+"."+PropertiesUtils.getString("package.service.impl");
        PACKAGE_CONTROLLER=PACKAGE_BASE+"."+PropertiesUtils.getString("package.controller");

        PATH_BASE=PropertiesUtils.getString("path.base")+PATH_JAVA;
        PATH_UTILS=PATH_BASE+"/"+PACKAGE_UTIL.replace(".","/");
        PATH_PO=PATH_BASE+"/"+PACKAGE_PO.replace(".","/");
        PATH_QUERY=PATH_BASE+"/"+PACKAGE_QUERY.replace(".","/");
        PATH_VO=PATH_BASE+"/"+PACKAGE_VO.replace(".","/");
        PATH_ENUMS=PATH_BASE+"/"+PACKAGE_ENUMS.replace(".","/");
        PATH_MAPPERS=PATH_BASE+"/"+PACKAGE_MAPPERS.replace(".","/");
        PATH_SERVICE=PATH_BASE+"/"+PACKAGE_SERVICE.replace(".","/");
        PATH_EXCEPTION=PATH_BASE+"/"+PACKAGE_EXCEPTION.replace(".","/");
        PATH_SERVICE_IMPL=PATH_BASE+"/"+PACKAGE_SERVICE_IMPL.replace(".","/");
        PATH_CONTROLLER=PATH_BASE+"/"+PACKAGE_CONTROLLER.replace(".","/");
        PATH_MAPPERS_XMLS=PropertiesUtils.getString("path.base")+PATH_RESOURCES+"/"+PACKAGE_MAPPERS.replace(".","/");



    }
    public static final String[]SQL_DATE_TIME_TYPES=new String[]{"datetime","timestamp"};

    public static final String[]SQL_DATE_TYPES=new String[]{"date"};

    public static final String[]SQL_DECIMAL_TYPE=new String[]{"decimal","double","float"};

    public static final String[]SQL_STRING_TYPE=new String[]{"char","varchar","text","mediumtext","longtext"};

    public static final String[]SQL_INTEGER_TYPE=new String[]{"int","tinyint"};

    public static final String[]SQL_LONG_TYPE=new String[]{"bigint"};



}
