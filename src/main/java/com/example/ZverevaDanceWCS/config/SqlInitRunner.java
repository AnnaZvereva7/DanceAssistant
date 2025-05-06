package com.example.ZverevaDanceWCS.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class SqlInitRunner implements CommandLineRunner {
    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = new String(
                    getClass().getResourceAsStream("/init.sql").readAllBytes()
            );
            stmt.execute(sql);
        }
    }
}
