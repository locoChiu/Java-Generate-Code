package org.azim.service;

import com.google.common.base.CaseFormat;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.azim.model.ColumnClass;
import org.azim.model.RespBean;
import org.azim.model.TableClass;
import org.azim.utils.DBUtil;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成代码服务
 */
@Service
public class GenerateCodeService {

    Configuration cfg = null;

    {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setTemplateLoader(new ClassTemplateLoader(GenerateCodeService.class,"/templates"));
        cfg.setDefaultEncoding("UTF-8");
    }

    /**
     * 获取表中信息并赋值于模板
     * @param tableClassList 所有表信息
     * @param realPath 文件输出路径
     * @return
     */
    public RespBean generateCode(List<TableClass> tableClassList, String realPath) {
        try {
            Template modelTemplate = cfg.getTemplate("Model.java.ftl");
            Template mapperJavaTemplate = cfg.getTemplate("Mapper.java.ftl");
            Template mapperXmlTemplate = cfg.getTemplate("Mapper.xml.ftl");
            Template serviceTemplate = cfg.getTemplate("Service.java.ftl");
            Template controllerTemplate = cfg.getTemplate("Controller.java.ftl");
            Connection connection = DBUtil.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            for (TableClass tableClass : tableClassList) {
                ResultSet columns = metaData.getColumns(connection.getCatalog(), null, tableClass.getTableName(), null);
                ResultSet primaryKeys = metaData.getPrimaryKeys(connection.getCatalog(), null, tableClass.getTableName());
                List<ColumnClass> columnClasses = new ArrayList<>();
                while(columns.next()) {
                    ColumnClass columnClass = new ColumnClass();
                    String column_name = columns.getString("COLUMN_NAME");
                    String type_name = columns.getString("TYPE_NAME");
                    String remark = columns.getString("REMARKS");
                    String property_name = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,column_name);
                    columnClass.setRemark(remark);
                    columnClass.setColumnName(column_name);
                    columnClass.setPropertyName(property_name);
                    columnClass.setType(type_name);
                    primaryKeys.first();
                    while(primaryKeys.next()) {
                        String name = primaryKeys.getString("COLUMN_NAME");
                        if(column_name.equals(name)) {
                            columnClass.setPrimary(true);
                        }
                    }
                    columnClasses.add(columnClass);
                }
                tableClass.setColumns(columnClasses);
                String path = realPath + "/" + tableClass.getPackageName().replace(".","/");
                generate(modelTemplate,tableClass,path+"/model/");
                generate(mapperJavaTemplate, tableClass, path + "/mapper/");
                generate(mapperXmlTemplate, tableClass, path + "/mapperxml/");
                generate(serviceTemplate, tableClass, path + "/service/");
                generate(controllerTemplate, tableClass, path + "/controller/");
            }
            return RespBean.ok("代码已生成", realPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RespBean.error("代码生成失败");
    }

    /**
     * 输出到文件
     * @param modelTemplate
     * @param tableClass
     * @param path
     * @throws IOException
     * @throws TemplateException
     */
    private void generate(Template modelTemplate, TableClass tableClass, String path) throws IOException, TemplateException {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName =path + "/" + tableClass.getModelName() + modelTemplate.getName().replace(".ftl","").replace("Model","");
        FileOutputStream fos = new FileOutputStream(fileName);
        OutputStreamWriter writer = new OutputStreamWriter(fos);
        modelTemplate.process(tableClass,writer);
        fos.close();
        writer.close();
    }

}
