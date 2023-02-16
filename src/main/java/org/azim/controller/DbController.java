package org.azim.controller;

import com.google.common.base.CaseFormat;
import org.azim.model.Db;
import org.azim.model.RespBean;
import org.azim.model.TableClass;
import org.azim.utils.DBUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库控制器
 */
@RestController
public class DbController {

    /**
     * 数据库连接
     * @param db 包含数据库用户、密码、链接
     * @return
     */
    @PostMapping("/connect")
    public RespBean connec(@RequestBody Db db) {
        Connection connection = DBUtil.intiDb(db);
        if(connection != null) {
            return RespBean.ok("数据库连接成功");
        } else {
            return RespBean.error("数据库连接失败");
        }
    }

    /**
     * 读取数据库信息
     * @param map 包名
     * @return
     */
    @PostMapping("/config")
    public RespBean config(@RequestBody Map<String ,String> map) {
        String packageName = map.get("packageName");
        try {
            Connection connection = DBUtil.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(connection.getCatalog(), null, null, null);
            List<TableClass> tableClassList = new ArrayList<>();
            while(tables.next()) {
                TableClass tableItem = new TableClass();
                tableItem.setPackageName(packageName);
                String table_name = tables.getString("TABLE_NAME");
                tableItem.setTableName(table_name);
                String modelName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,table_name);
                tableItem.setModelName(modelName);
                tableItem.setControllerName(modelName + "Controller");
                tableItem.setMapperName(modelName + "Mapper");
                tableItem.setServiceName(modelName+"Service");
                tableClassList.add(tableItem);
            }
            return RespBean.ok("数据库信息读取成功",tableClassList);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return RespBean.error("数据库信息读取失败");
    }
}
