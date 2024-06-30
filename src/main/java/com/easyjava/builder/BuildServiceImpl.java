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

public class BuildServiceImpl {
    private static final Logger logger= LoggerFactory.getLogger(BuildPo.class);

    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_SERVICE_IMPL);
        if(!folder.exists()){
            folder.mkdirs();
        }
        String interfaceName=tableInfo.getBeanName()+"Service";
        File poFile=new File(folder,interfaceName+"Impl.java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;
        try{
            out=new FileOutputStream(poFile);
            outw=new OutputStreamWriter(out,"utf8");
            bw=new BufferedWriter(outw);
            //先导包名
            bw.write("package "+Constants.PACKAGE_SERVICE_IMPL+";");

            String mapperName=tableInfo.getBeanName()+Constants.SUFFIX_MAPPERS;
            String mapperBeanName=StringUtils.lowerCaseFirstLatter(mapperName);
            bw.newLine();
            bw.newLine();
            //import 需要的包名
            ImportNeedPackge(tableInfo, interfaceName, bw, mapperName);
            //类的注释
            BuildComment.createClassComment(bw,tableInfo.getComment()+" correspond ServiceImpl");
            //注解
            BuildAnnotation(interfaceName, bw);
            //导入mapper
            imprtMapper(tableInfo, bw, mapperName);

            //构建方法
            //base on 条件查询列表
            selectByParam(tableInfo, bw, mapperBeanName);
            //base on 条件查询数量
            selectCountByParam(tableInfo, bw, mapperBeanName);
            //分页查询
            selectByPage(tableInfo, bw);
            //新增
            insertOne(tableInfo, bw, mapperBeanName);
            //批量新增
            insertBatch(tableInfo, bw, mapperBeanName);
            //新增或修改
            insertOrUpdateOne(tableInfo, bw, mapperBeanName);
            //批量新增或修改
            insertOrUpdateBatch(tableInfo, bw, mapperBeanName);
            //通过索引的增删改
            crudByKey(tableInfo, bw, mapperBeanName);
            bw.newLine();


            bw.write("}");
            bw.flush();
        }catch (Exception e){
            logger.error("创建serviceImpl失败",e);
        }finally {
            BuildMapperXml.closeAll(out, outw, bw, logger);
        }
    }

    private static void crudByKey(TableInfo tableInfo, BufferedWriter bw, String mapperBeanName) throws Exception {
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
            bw.write("    @Override");
            bw.newLine();
            bw.write("\t public "+ tableInfo.getBeanName()+" getBy"+methodName.toString()+"("+methodParams+"){");
            bw.newLine();
            bw.write("        return this."+ mapperBeanName +".selectBy"+methodName+"("+methodUse+");");
            bw.newLine();
            bw.write("\t }");
            bw.newLine();

            //构建update 方法
            BuildComment.createFieldComment(bw,"base on "+methodName+"update ");
            bw.write("    @Override");
            bw.newLine();
            bw.write("\t public Integer updateBy"+methodName+"("+ tableInfo.getBeanName()+" bean , "+methodParams+"){");
            bw.newLine();
            bw.write("        return this."+ mapperBeanName +".updateBy"+methodName+"(bean,"+methodUse+");");
            bw.newLine();
            bw.write("\t }");
            bw.newLine();

            //构建删除方法
            BuildComment.createFieldComment(bw,"base on "+methodName+" delete");
            bw.write("    @Override");
            bw.newLine();
            bw.write("\t public Integer deleteBy"+methodName.toString()+"("+methodParams+"){");
            bw.newLine();
            bw.write("        return this."+ mapperBeanName +".deleteBy"+methodName+"("+methodUse+");");
            bw.newLine();
            bw.write("\t }");
            bw.newLine();
        }
    }

    private static void insertOrUpdateBatch(TableInfo tableInfo, BufferedWriter bw, String mapperBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"Add or modify in batches");
        bw.write("    @Override");
        bw.newLine();
        bw.write("    public Integer addOrUpdateBatch(List<"+ tableInfo.getBeanName()+"> listBean){");
        bw.newLine();
        bw.write("        if(listBean==null||listBean.isEmpty()){\n" +
                "            return 0;\n" +
                "        }\n" +
                "        return this."+ mapperBeanName +".insertOrUpdateBatch(listBean);");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void insertOne (TableInfo tableInfo, BufferedWriter bw, String mapperBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"insert");
        bw.write("    @Override");
        bw.newLine();
        bw.write("    public Integer add("+ tableInfo.getBeanName()+" bean){");
        bw.newLine();
        bw.write("        return this."+mapperBeanName+".insert(bean);");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void insertOrUpdateOne (TableInfo tableInfo, BufferedWriter bw, String mapperBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"insert or update");
        bw.write("    @Override");
        bw.newLine();
        bw.write("    public Integer addOrUpdate("+ tableInfo.getBeanName()+" bean){");
        bw.newLine();
        bw.write("        return this."+mapperBeanName+".insertOrUpdate(bean);");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void insertBatch(TableInfo tableInfo, BufferedWriter bw, String mapperBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"batch insert");
        bw.write("    @Override");
        bw.newLine();
        bw.write("    public Integer addBatch(List<"+ tableInfo.getBeanName()+"> listBean){");
        bw.newLine();
        bw.write("        if(listBean==null||listBean.isEmpty()){\n" +
                "            return 0;\n" +
                "        }");
        bw.newLine();
        bw.write("        return this."+ mapperBeanName +".insertBatch(listBean);");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void selectByPage(TableInfo tableInfo, BufferedWriter bw) throws Exception {
        BuildComment.createFieldComment(bw,"paging query");
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
        bw.write("        List<"+tableInfo.getBeanName()+"> list = this.findListByParam(query);");
        bw.newLine();
        bw.write("        PaginationResultVO<"+tableInfo.getBeanName()+"> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);");
        bw.newLine();
        bw.write("        return result;");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void selectCountByParam(TableInfo tableInfo, BufferedWriter bw, String mapperBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"base on Condition query quantity");
        bw.write("    @Override");
        bw.newLine();
        bw.write("    public Integer findCountByParam("+ tableInfo.getBeanParamName()+" query){");
        bw.newLine();
        bw.write("        return this."+ mapperBeanName +".selectCount(query);");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void selectByParam(TableInfo tableInfo, BufferedWriter bw, String mapperBeanName) throws Exception {
        BuildComment.createFieldComment(bw,"base on Condition query list");
        bw.write("    @Override");
        bw.newLine();
        bw.write("    public List<"+ tableInfo.getBeanName()+">findListByParam("+ tableInfo.getBeanParamName()+" query){");
        bw.newLine();
        bw.write("        return this."+ mapperBeanName +".selectList(query);");
        bw.newLine();
        bw.write("\t }");
        bw.newLine();
    }

    private static void imprtMapper(TableInfo tableInfo, BufferedWriter bw, String mapperName) throws Exception {
        bw.write("  @Resource");
        bw.newLine();
        bw.write("  private "+ mapperName +"<"+ tableInfo.getBeanName()+","+ tableInfo.getBeanParamName()+">"+StringUtils.lowerCaseFirstLatter(mapperName)+";");
        bw.newLine();
    }

    private static void BuildAnnotation(String interfaceName, BufferedWriter bw) throws IOException {
        bw.newLine();
        bw.write("@Service(\""+StringUtils.lowerCaseFirstLatter(interfaceName)+"\")");
        bw.newLine();
        bw.write("public class "+ interfaceName +"Impl implements "+ interfaceName +"{");
        bw.newLine();
        bw.newLine();
    }

    private static void ImportNeedPackge(TableInfo tableInfo, String interfaceName, BufferedWriter bw, String mapperName) throws IOException {
        bw.write("import " + Constants.PACKAGE_QUERY + "." + tableInfo.getBeanParamName() + ";");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_QUERY + ".SimplePage;");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_VO + ".PaginationResultVO;");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_MAPPERS + "." + mapperName + ";");
        bw.newLine();
        bw.write("import org.springframework.stereotype.Service;");
        bw.newLine();
        bw.write("import " + Constants.PACKAGE_SERVICE + "." + interfaceName + ";");
        bw.newLine();
        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import javax.annotation.Resource;");
        bw.newLine();
        bw.write("import "+Constants.PACKAGE_ENUMS+".PageSize;");
        bw.newLine();
    }
}

