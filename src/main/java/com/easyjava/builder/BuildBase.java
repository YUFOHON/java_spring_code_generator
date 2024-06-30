package com.easyjava.builder;

import com.easyjava.bean.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class BuildBase {
    private static Logger logger = LoggerFactory.getLogger(BuildBase.class);

    public static void execute() {
        List<String> headInfoList=new ArrayList<>();
        //生成日期枚举
        headInfoList.add("package "+Constants.PACKAGE_ENUMS);
        build(headInfoList,"DateTimePatternEnum", Constants.PATH_ENUMS);
        //生成时间转换工具类
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_UTIL);
        build(headInfoList,"DateUtils", Constants.PATH_UTILS);

        //生成baseMapper
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_MAPPERS);
        build(headInfoList,"BaseMapper", Constants.PATH_MAPPERS);

        //生成pageSize枚举
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_ENUMS);
        build(headInfoList,"PageSize", Constants.PATH_ENUMS);

        //生成responseCode枚举
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_ENUMS);
        build(headInfoList,"ResponseCodeEnum", Constants.PATH_ENUMS);

        //生成SimplePage
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_QUERY);
        headInfoList.add("import "+Constants.PACKAGE_ENUMS+".PageSize;");
        build(headInfoList,"SimplePage", Constants.PATH_QUERY);

        //生成BaseQuery
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_QUERY);
        build(headInfoList,"BaseQuery", Constants.PATH_QUERY);

        //生成paginationResultVO
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_VO);
        build(headInfoList,"PaginationResultVO", Constants.PATH_VO);

        //生成ResponseVO
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_VO);
        build(headInfoList,"ResponseVO", Constants.PATH_VO);

        //生成BusinessException
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_EXCEPTION);
        headInfoList.add("import  "+Constants.PACKAGE_ENUMS+".ResponseCodeEnum;");
        build(headInfoList,"BusinessException", Constants.PATH_EXCEPTION);

        //生成BaseController
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_CONTROLLER);

        headInfoList.add("import  "+Constants.PACKAGE_ENUMS+".ResponseCodeEnum;");
        headInfoList.add("import  "+Constants.PACKAGE_VO+".ResponseVO;");
        build(headInfoList,"ABaseController", Constants.PATH_CONTROLLER);

        //生成AGlobalExceptionHandlerController
        headInfoList.clear();
        headInfoList.add("package "+Constants.PACKAGE_CONTROLLER);

        headInfoList.add("import  "+Constants.PACKAGE_ENUMS+".ResponseCodeEnum;");
        headInfoList.add("import  "+Constants.PACKAGE_VO+".ResponseVO;");
        headInfoList.add("import  "+Constants.PACKAGE_EXCEPTION+".BusinessException;");
        build(headInfoList,"AGlobalExceptionHandlerController", Constants.PATH_CONTROLLER);
    }

    private static void build(List<String> headerInfoList,String fileName, String outPutPath) {
        File folder = new File(outPutPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File javaFile = new File(outPutPath, fileName + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        InputStream in = null;
        InputStreamReader inr = null;
        BufferedReader bf = null;
        try {
            out=new FileOutputStream(javaFile);
            outw=new OutputStreamWriter(out,"utf-8");
            bw=new BufferedWriter(outw);

            String templatePath=BuildBase.class.getClassLoader().getResource("template/"+fileName+".txt").getPath();
            in=new FileInputStream(templatePath);
            inr=new InputStreamReader(in,"utf-8");
            bf=new BufferedReader(inr);
            for (String head : headerInfoList) {
                bw.write(head+";");
                bw.newLine();
                if(head.contains("package")){
                    bw.newLine();
                }
            }
            String lineInfo=null;
            while ((lineInfo=bf.readLine())!=null){
                bw.write(lineInfo);
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            logger.error("生成基础类:{}失败,{}", fileName, e);
        } finally {
            try {
                if (bf != null) {
                    bf.close();
                }
                if (inr != null) {
                    inr.close();
                }
                if (in != null) {
                    in.close();
                }
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
    }

}
