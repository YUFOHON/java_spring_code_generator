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
import java.util.List;
import java.util.Map;

public class BuildMapper {
    private static final Logger logger= LoggerFactory.getLogger(BuildMapper.class);
    public static  void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_MAPPERS);
        if(!folder.exists()){
            folder.mkdirs();
        }
        String className=tableInfo.getBeanName()+Constants.SUFFIX_MAPPERS;
        File poFile=new File(folder,className+".java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;
        try{
            out=new FileOutputStream(poFile);
            outw=new OutputStreamWriter(out,"utf8");
            bw=new BufferedWriter(outw);
            //先导包名
            bw.write("package "+Constants.PACKAGE_MAPPERS+";");
            bw.newLine();
            bw.newLine();
            //import需要的类
            bw.write("import org.apache.ibatis.annotations.Param;");
            bw.newLine();
            bw.write("import org.apache.ibatis.annotations.Mapper;");
            bw.newLine();

            //类的注释
            BuildComment.createClassComment(bw,tableInfo.getComment()+"corresponding mapper class");
            bw.newLine();
            //创建类
            bw.write("@Mapper");
            bw.newLine();
            bw.write("public interface "+className+"<T,P> extends BaseMapper {");
            bw.newLine();


            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                StringBuilder methodName=new StringBuilder();
                StringBuilder methodParams=new StringBuilder();

                List<FieldInfo> keyFieldInfoList = entry.getValue();
                int index=0;
                for (FieldInfo fieldInfo : keyFieldInfoList) {
                    index++;
                    methodName.append(StringUtils.uperCaseFirstLatter(fieldInfo.getPropertyName()));

                    if(index<keyFieldInfoList.size()){
                        methodName.append("And");
                    }
                    methodParams.append("@Param(\""+fieldInfo.getPropertyName()+"\") "+fieldInfo.getJavaType()+" "+fieldInfo.getPropertyName());
                    if(index<keyFieldInfoList.size()){
                        methodParams.append(",");
                    }
                }
                //构建查询方法
                BuildComment.createFieldComment(bw,"base on "+methodName+" query");
                bw.write("\t T selectBy"+methodName.toString()+"("+methodParams+");");
                bw.newLine();
                bw.newLine();

                //构建更新方法
                BuildComment.createFieldComment(bw,"base on "+methodName+" update");
                bw.write("\t Integer updateBy"+methodName+"(@Param(\"bean\") T t, "+methodParams+");");
                bw.newLine();
                bw.newLine();

                //构建删除方法
                BuildComment.createFieldComment(bw,"base on "+methodName+" delete");
                bw.write("\t Integer deleteBy"+methodName.toString()+"("+methodParams+");");
                bw.newLine();
                bw.newLine();
            }


            bw.write("}");
            bw.flush();
        }catch (Exception e){
            logger.error("创建mapper失败",e);
        }finally {
            BuildMapperXml.closeAll(out, outw, bw, logger);
        }
    }
    }

