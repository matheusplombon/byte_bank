package br.com.alura.bytebank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    public Connection recuperarConexao() {
        try {
            return DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/byte_bank", "root", "1234");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
    // CÓDIGO COMENTADO POIS O LOG VINHA COM MUITAS INFORMAÇÕES, PORÉM FUNCIONANDO CORRETAMENTE.

//    public class ConnectionFactory {
//
//        public Connection recuperarConexao() {
//            try {
//                return createDataSource().getConnection();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        private HikariDataSource createDataSource() {
//            HikariConfig config = new HikariConfig();
//            config.setJdbcUrl("jdbc:mysql://localhost:3306/byte_bank");
//            config.setUsername("root");
//            config.setPassword("root");
//            config.setMaximumPoolSize(10);
//
//            return new HikariDataSource(config);
//        }
//    }


