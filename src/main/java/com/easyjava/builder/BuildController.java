package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildController {
    private static final Logger logger= LoggerFactory.getLogger(BuildPo.class);

    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_CONTROLLER);
        if(!folder.exists()){
            folder.mkdirs();
        }


        String className=tableInfo.getBeanName()+"Controller";
        String serviceName=tableInfo.getBeanName()+"Service";
        String serviceBeanName=StringUtils.lowerCaseFirstLatter(serviceName);
        File poFile=new File(folder,className+".java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;
        try{
            out=new FileOutputStream(poFile);
            outw=new OutputStreamWriter(out,"utf8");
            bw=new BufferedWriter(outw);
            //先导包名
            bw.write("package "+Constants.PACKAGE_CONTROLLER+";");

            bw.newLine();
            bw.newLine();
            //import 需要的包名
            ImportNeedPackge(tableInfo, className, bw, serviceName);
            //类的注释
            BuildComment.createClassComment(bw,tableInfo.getComment()+"Controller");
            //注解
            BuildAnnotation(className, bw,StringUtils.lowerCaseFirstLatter(tableInfo.getBeanName()));
            //导入mapper
            importService(serviceBeanName, bw, serviceName);
            bw.newLine();



            //构建方法
            BuildComment.createFieldComment(bw,"base on condition paging query");
            bw.write("    @RequestMapping(\"loadDataList\")");
            bw.newLine();
            bw.write("    public ResponseVO loadDataList("+tableInfo.getBeanParamName()+" query) {");
            bw.newLine();
            bw.write("        return getSuccessResponseVo("+serviceBeanName+".findListByPage(query));");
            bw.newLine();
            bw.write("    }");
            bw.newLine();
             //新增
             insertOne(tableInfo, bw, serviceBeanName);
            //批量新增
            insertBatch(tableInfo, bw, serviceBeanName);
            //新增或修改
            insertOrUpdateOne(tableInfo, bw, serviceBeanName);
            //批量新增或修改
            insertOrUpdateBatch(tableInfo, bw, serviceBeanName);
            //通过索引的增删改
            crudByKey(tableInfo, bw, serviceBeanName);
            bw.newLine();


            bw.write("}");
            bw.flush();
        }catch (Exception e){
            logger.error("创建serviceImpl失败",e);
        }finally {
            BuildMapperXml.closeAll(out, outw, bw, logger);
        }
    }

    private static void crudByKey(TableInfo tableInfo, BufferedWriter bw, String serviceBeanName) throws Exception {
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            StringBuilder methodName=new StringBuilder();
            StringBuilder methodParams=new StringBuilder();
            StringBuilder methodUse=new StringBuilder();

            List<FieldInfo> keyFieldInfoList = entry.getValue();
            int index=0;
            for (FieldInfo fieldInfo : keyFieldInfoList) {
                index++;
                methodName.append(StringUtils.uperCaseFirstLatter(fieldInfo.getPropertyName()));
                if(index<keyFieldInfoList.size()){
                    methodName.append("And");
                }
                methodParams.append(fieldInfo.getJavaType()+" "+fieldInfo.getPropertyName());
                methodUse.append(fieldInfo.getPropertyName());
                if(index<keyFieldInfoList.size()){
                    methodParams.append(",");
                    methodUse.append(",");
                }
            }
            //构建查询方法
            BuildComment.createFieldComment(bw,"base on "+methodName+" query");
            bw.write("    @RequestMapping(\""+"get"+tableInfo.getBeanName()+"By"+methodName+"\")");
            bw.newLine();
            bw.write("\t public ResponseVO get"+ tableInfo.getBeanName()+"By"+methodName.toString()+"("+methodParams+"){");
            bw.newLine();
            bw.write("        return getSuccessResponseVo(this."+serviceBeanName  +".getBy"+methodName+"("+methodUse+"));");
            bw.newLine();
            bw.write("\t }");
            bw.newLine();

            //构建更新方法
            BuildComment.createFieldComment(bw,"base on "+methodName+" update");
            bw.write("    @RequestMapping(\""+"update"+tableInfo.getBeanName()+"By"+methodName+"\")");
            bw.newLine();
            bw.write("\t public ResponseVO update"+ tableInfo.getBeanName()+"By"+methodName+"("+tableInfo.getBeanName()+" bean,"+methodParams+"){");
            bw.newLine();
            bw.write("        return getSuccessResponseVo(this."+serviceBeanName  +".updateBy"+methodName+"(bean,"+methodUse+"));");
            bw.newLine();
            bw.write("\t }");
            bw.newLine();

            //构建删除方法
            BuildComment.createFieldComment(bw,"base on "+methodName+" delete");
            bw.write("    @RequestMapping(\""+"delete"+tableInfo.getBeanName()+"By"+methodName+"\")");
            bw.newLine();
            bw.write("\t public ResponseVO delete"+ tableInfo.getBeanName()+"By"+methodName.toString()+"("+methodParams+"){");
            bw.newLine();
            bw.write("        return getSuccessResponseVo(this."+serviceBeanName  +".deleteBy"+methodName+"("+methodUse+"));");
            bw.newLine();
            bw.write("\t }");
            bw.newLine();
        }
    }

    private static void insertOrUpdateBatch(TableInfo tableInfo, BufferedWriter bw, String serviceBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"batch insert or update");
        bw.write("    @RequestMapping(\"addOrUpdateBatch\")");
        bw.newLine();
        bw.write("    public ResponseVO addOrUpdate(@RequestBody List<"+ tableInfo.getBeanName()+"> listBean){");
        bw.newLine();
        bw.write("        return getSuccessResponseVo("+serviceBeanName+".addOrUpdateBatch(listBean));");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void insertOne (TableInfo tableInfo, BufferedWriter bw, String serviceBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"insert");
        bw.write("    @RequestMapping(\"add\")");
        bw.newLine();
        bw.write("    public ResponseVO add("+ tableInfo.getBeanName()+" bean){");
        bw.newLine();
        bw.write("        return getSuccessResponseVo("+serviceBeanName+".add(bean));");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void insertOrUpdateOne (TableInfo tableInfo, BufferedWriter bw, String serviceBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"insert or update");
        bw.write("    @RequestMapping(\"addOrUpdate\")");
        bw.newLine();
        bw.write("    public ResponseVO addOrUpdate("+ tableInfo.getBeanName()+" bean){");
        bw.newLine();
        bw.write("        return getSuccessResponseVo("+serviceBeanName+".addOrUpdate(bean));");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void insertBatch(TableInfo tableInfo, BufferedWriter bw, String serviceBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"batch insert");
        bw.write("    @RequestMapping(\"addBatch\")");
        bw.newLine();
        bw.write("    public ResponseVO addBatch(@RequestBody List<"+ tableInfo.getBeanName()+"> listBean){");
        bw.newLine();
        bw.write("        return getSuccessResponseVo("+serviceBeanName+".addBatch(listBean));");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void selectByPage(TableInfo tableInfo, BufferedWriter bw) throws Exception {
        BuildComment.createFieldComment(bw,"Paging query");
        bw.write("    @Override");
        bw.newLine();
        bw.write("    public PaginationResultVO<"+ tableInfo.getBeanName()+"> findListByPage("+ tableInfo.getBeanParamName()+" query ){");
        bw.newLine();
        bw.write("        Integer count = this.findCountByParam(query); ");
        bw.newLine();
        bw.write("        Integer pageSize=query.getPageSize()==null? PageSize.SIZE15.getSize():query.getPageSize();");
        bw.newLine();
        bw.write("        SimplePage page=new SimplePage(query.getPageNo(),count,pageSize);");
        bw.newLine();
        bw.write("        query.setSimplePage(page);");
        bw.newLine();
        bw.write("        List<ProductInfo> list = this.findListByParam(query);");
        bw.newLine();
        bw.write("        PaginationResultVO<ProductInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);");
        bw.newLine();
        bw.write("        return result;");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }



    private static void importService(String serviceBeanName, BufferedWriter bw, String serviceName) throws Exception {
        bw.write("  @Resource");
        bw.newLine();
        bw.write("  private "+ serviceName +" "+serviceBeanName+";");
        bw.newLine();
    }

    private static void BuildAnnotation(String className, BufferedWriter bw,String restMapping) throws IOException {
        bw.newLine();
        bw.write("@RestController");
        bw.newLine();
        bw.write("@RequestMapping(\"/"+restMapping+"\")");
        bw.newLine();
        bw.write("public class "+ className +" extends ABaseController{");
        bw.newLine();
        bw.newLine();
    }

    private static void ImportNeedPackge(TableInfo tableInfo, String interfaceName, BufferedWriter bw, String serviceName) throws IOException {
        bw.write("import org.springframework.web.bind.annotation.RestController;");
        bw.newLine();
        bw.write("import javax.annotation.Resource;");
        bw.newLine();
        bw.write("import org.springframework.web.bind.annotation.RequestMapping;");
        bw.newLine();
        bw.write("import org.springframework.web.bind.annotation.RequestBody;");
        bw.newLine();
        bw.write("import "+Constants.PACKAGE_SERVICE+"."+serviceName+";");
        bw.newLine();
        bw.write("import "+Constants.PACKAGE_PO+"."+tableInfo.getBeanName()+";");
        bw.newLine();
        bw.write("import "+Constants.PACKAGE_QUERY+"."+tableInfo.getBeanName()+"Query;");
        bw.newLine();
        bw.write("import "+Constants.PACKAGE_VO+".ResponseVO;");
        bw.newLine();
        bw.write("import java.util.List;");
        bw.newLine();


    }
}
