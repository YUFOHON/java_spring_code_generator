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

public class BuildService {
    private static final Logger logger= LoggerFactory.getLogger(BuildPo.class);

    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_SERVICE);
        if(!folder.exists()){
            folder.mkdirs();
        }
        String className=tableInfo.getBeanName()+"Service";
        File poFile=new File(folder,className+".java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;
        try{
            out=new FileOutputStream(poFile);
            outw=new OutputStreamWriter(out,"utf8");
            bw=new BufferedWriter(outw);
            //先导包名
            bw.write("package "+Constants.PACKAGE_SERVICE+";");

            bw.newLine();
            bw.newLine();
            //import 需要的包名
            bw.write("import "+Constants.PACKAGE_QUERY+"."+tableInfo.getBeanParamName()+";");
            bw.newLine();
            bw.write("import "+Constants.PACKAGE_PO+"."+tableInfo.getBeanName()+";");
            bw.newLine();
            bw.write("import "+Constants.PACKAGE_VO+".PaginationResultVO;");
            bw.newLine();
            bw.write("import java.util.List;");
            bw.newLine();
            //类的注释
            BuildComment.createClassComment(bw,tableInfo.getComment()+"对应的Service");
            bw.newLine();
            //创建类
            bw.write("public interface "+className+"{");
            bw.newLine();
            bw.newLine();
            BuildComment.createFieldComment(bw,"Query list based on conditions");
            bw.write(" List<"+tableInfo.getBeanName()+">findListByParam("+tableInfo.getBeanParamName()+" query);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw,"Query quantity based on conditions");
            bw.write(" Integer findCountByParam("+tableInfo.getBeanParamName()+" query);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw,"Paging query");
            bw.write(" PaginationResultVO<"+tableInfo.getBeanName()+"> findListByPage("+tableInfo.getBeanParamName()+" query );");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw,"new");
            bw.write(" Integer add("+ tableInfo.getBeanName()+" bean);");
            bw.newLine();

            BuildComment.createFieldComment(bw,"Batch Add");
            bw.write(" Integer addBatch(List<"+tableInfo.getBeanName()+"> listBean);");
            bw.newLine();

            BuildComment.createFieldComment(bw,"Add or modify");
            bw.write(" Integer addOrUpdate("+ tableInfo.getBeanName()+" bean);");
            bw.newLine();


            BuildComment.createFieldComment(bw,"Batch addition or modification");
            bw.write(" Integer addOrUpdateBatch(List<"+tableInfo.getBeanName()+"> listBean);");
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
                    methodParams.append(fieldInfo.getJavaType()+" "+fieldInfo.getPropertyName());
                    if(index<keyFieldInfoList.size()){
                        methodParams.append(",");
                    }
                }
                //构建查询方法
                BuildComment.createFieldComment(bw,"base on "+methodName+" query");
                bw.write("\t "+tableInfo.getBeanName()+" getBy"+methodName.toString()+"("+methodParams+");");
                bw.newLine();
                bw.newLine();

                //构建更新方法
                BuildComment.createFieldComment(bw,"base on "+methodName+" update");
                bw.write("\t Integer updateBy"+methodName+"("+tableInfo.getBeanName()+" bean , "+methodParams+");");
                bw.newLine();
                bw.newLine();

                //构建删除方法
                BuildComment.createFieldComment(bw,"base on "+methodName+" deleted");
                bw.write("\t Integer deleteBy"+methodName.toString()+"("+methodParams+");");
                bw.newLine();
                bw.newLine();
            }




            bw.newLine();


            bw.write("}");
            bw.flush();
        }catch (Exception e){
            logger.error("创建service失败",e);
        }finally {
            BuildMapperXml.closeAll(out, outw, bw, logger);
        }
    }
}
