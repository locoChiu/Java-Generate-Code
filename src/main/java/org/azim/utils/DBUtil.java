package org.azim.utils;

import org.azim.model.Db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 获取数据库连接工具
 */
public class DBUtil {
    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    public static Connection intiDb(Db db) {
        if(connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(db.getUrl(),db.getUsername(),db.getPassword());
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
