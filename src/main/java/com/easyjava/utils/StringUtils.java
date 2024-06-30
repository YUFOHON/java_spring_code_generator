package com.easyjava.utils;

import java.util.Locale;

public class StringUtils {
    /**
     *
     * @param field 字段
     * @return 将首字母大写后返回
     */
    public static String uperCaseFirstLatter(String field){
        if(field==null||field.length()==0){
            return field;
        }
        return field.substring(0,1).toUpperCase()+field.substring(1);

    }

    public static String lowerCaseFirstLatter(String field){
        if(field==null||field.length()==0){
            return field;
        }
        return field.substring(0,1).toLowerCase()+field.substring(1);

    }


}
