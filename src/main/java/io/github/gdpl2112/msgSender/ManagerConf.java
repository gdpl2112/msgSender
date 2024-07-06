package io.github.gdpl2112.msgSender;

import java.sql.*;

/**
 * @author github.kloping
 */
public class ManagerConf {
    private static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:./conf/msgsender/switch.db");
        return connection;
    }

    public static ManagerConf INSTANCE = new ManagerConf();

    public ManagerConf() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS sw(id VARCHAR(20) PRIMARY KEY,k BLOB);");
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Boolean getStateByIdDefault(Long id, Boolean k) {
        Boolean a = query(String.format("SELECT k FROM sw WHERE id='%s'",  id));
        if (a == null) {
            update(String.format("INSERT INTO sw VALUES ('%s', %s);", id, k));
            return k;
        }
        return a;
    }

    public void setStateById( Long id, boolean k) {
        getStateByIdDefault( id, k);
        update(String.format("UPDATE sw SET k=%s WHERE id='%s';", k,  id));
    }

    public Integer update(String sql) {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            int i = statement.executeUpdate(sql);
            statement.close();
            connection.close();
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Boolean query(String sql) {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            Boolean k = null;
            if (rs.next()) {
                k = rs.getBoolean("k");
            }
            statement.close();
            connection.close();
            return k;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
