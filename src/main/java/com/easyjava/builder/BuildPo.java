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
import java.lang.reflect.Field;

public class BuildPo {
    private static final Logger logger= LoggerFactory.getLogger(BuildPo.class);

    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_PO);
        if(!folder.exists()){
            folder.mkdirs();
        }
        File poFile=new File(folder,tableInfo.getBeanName()+".java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;
        try{
            out=new FileOutputStream(poFile);
            outw=new OutputStreamWriter(out,"utf8");
            bw=new BufferedWriter(outw);
            //先导包名
            bw.write("package "+Constants.PACKAGE_PO+";");
            bw.newLine();
            bw.newLine();
            //import需要的类
            bw.write("import java.io.Serializable;");
            bw.newLine();
            if(tableInfo.getHaveDate()||tableInfo.getHaveDateTime()){
                bw.write("import java.util.Date;");
                bw.newLine();
                bw.write(Constants.BEAN_DATE_FORMAT_CLASS+";");
                bw.newLine();
                bw.write(Constants.BEAN_DATE_UNFORMAT_CLASS+";");
                bw.newLine();
                bw.write("import "+Constants.PACKAGE_UTIL+" .DateUtils;");
                bw.newLine();
                bw.write("import "+Constants.PACKAGE_ENUMS+" .DateTimePatternEnum;");
            }
            bw.newLine();
            if(tableInfo.getHaveBigDecimal()){
                bw.write("import java.math.BigDecimal;");
            }
            bw.newLine();
            bw.newLine();


            for(FieldInfo field:tableInfo.getFieldList()){
                if(ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FILED.split(","),field.getPropertyName())){
                    bw.write(Constants.IGNORE_BEAN_TOJSON_CLASS+";");
                    bw.newLine();
                    break;
                }
            }
            //类的注释
            BuildComment.createClassComment(bw,tableInfo.getComment());
            bw.newLine();
            //创建类
            bw.write("public class "+tableInfo.getBeanName()+" implements Serializable {");
            bw.newLine();

            //每个字段
            for(FieldInfo field:tableInfo.getFieldList()){
                //字段的注释
                BuildComment.createFieldComment(bw,field.getComment());
                //判断字段是否需要加对应的注解
                if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,field.getSqlType())){
                    bw.write("    "+String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION,DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();
                    bw.write("    "+String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION,DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();
                }
                if(ArrayUtils.contains(Constants.SQL_DATE_TYPES,field.getSqlType())){
                    bw.write("    "+String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION,DateUtils.YYYY_MM_DD));
                    bw.newLine();
                    bw.write("    "+String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION,DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }
                if(ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FILED.split(","),field.getPropertyName())){
                    bw.write("    "+Constants.IGNORE_BEAN_TOJSON_EXPRESSION);
                    bw.newLine();
                }
                bw.write("\tprivate "+ field.getJavaType()+" "+field.getPropertyName()+";");
                bw.newLine();
                bw.newLine();
            }
            //get 和set方法
            for(FieldInfo field:tableInfo.getFieldList()){
                //set方法
                String tempField= StringUtils.uperCaseFirstLatter(field.getPropertyName());
                bw.write("\tpublic void set"+tempField+"("+field.getJavaType()+" "+field.getPropertyName()+") {" );
                bw.newLine();
                bw.write("        this."+field.getPropertyName()+" = "+field.getPropertyName()+";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
                //get方法
                bw.write("\tpublic "+field.getJavaType()+" get"+tempField+"() {" );
                bw.newLine();
                bw.write("        return this."+field.getPropertyName()+";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
            }
            StringBuilder sb=new StringBuilder();
            //重写toString
            int index=0;
            for(FieldInfo field:tableInfo.getFieldList()){
                String properName=field.getPropertyName();
                if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,field.getSqlType())){
                    properName="DateUtils.format("+properName+", DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())";
                }else if(ArrayUtils.contains(Constants.SQL_DATE_TYPES,field.getSqlType())){
                    properName="DateUtils.format("+properName+", DateTimePatternEnum.YYYY_MM_DD.getPattern())";
                }
                sb.append("\""+field.getComment()+field.getPropertyName()+":\"+ ("+field.getPropertyName()+" == null?\"空\":"+properName+")");
                if(index!=tableInfo.getFieldList().size()-1){
                    sb.append("+").append("\",\"").append("+");
                }
                index++;
            }
            sb.substring(0,sb.lastIndexOf(","));
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic String toString(){");
            bw.newLine();
            bw.write("        return "+sb.toString()+";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            bw.write("}");
            bw.flush();
        }catch (Exception e){
          logger.error("generate PO fail",e);
        }finally {
            BuildMapperXml.closeAll(out, outw, bw, logger);
        }
    }
}
