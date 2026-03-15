package com.catalyst.shared.infrastructure.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseHealthIndicatorTest {

    @Test
    void health_shouldReturnUp_whenConnectionIsValid() {
        // GIVEN
        DataSource dataSource = new StubDataSource(true, false);
        DatabaseHealthIndicator healthIndicator = new DatabaseHealthIndicator(dataSource);

        // WHEN
        Health health = healthIndicator.health();

        // THEN
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("status", "Connection successful");
    }

    @Test
    void health_shouldReturnDown_whenConnectionIsInvalid() {
        // GIVEN
        DataSource dataSource = new StubDataSource(false, false);
        DatabaseHealthIndicator healthIndicator = new DatabaseHealthIndicator(dataSource);

        // WHEN
        Health health = healthIndicator.health();

        // THEN
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("status", "Connection validation failed");
    }

    @Test
    void health_shouldReturnDown_whenSQLExceptionOccurs() {
        // GIVEN
        DataSource dataSource = new StubDataSource(false, true);
        DatabaseHealthIndicator healthIndicator = new DatabaseHealthIndicator(dataSource);

        // WHEN
        Health health = healthIndicator.health();

        // THEN
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("error").toString()).contains("Connection failed");
    }

    private static class StubDataSource implements DataSource {
        private final boolean valid;
        private final boolean throwException;

        StubDataSource(boolean valid, boolean throwException) {
            this.valid = valid;
            this.throwException = throwException;
        }

        @Override
        public Connection getConnection() throws SQLException {
            if (throwException)
                throw new SQLException("Connection failed");
            return new StubConnection(valid);
        }

        @Override
        public Connection getConnection(String u, String p) throws SQLException {
            return getConnection();
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }

    private static class StubConnection implements Connection {
        private final boolean valid;

        StubConnection(boolean valid) {
            this.valid = valid;
        }

        @Override
        public boolean isValid(int timeout) {
            return valid;
        }

        @Override
        public void close() {
        }

        // Other methods omitted for brevity, adding minimum defaults needed
        @Override
        public <T> T unwrap(Class<T> iface) {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        @Override
        public java.sql.Statement createStatement() {
            return null;
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String s) {
            return null;
        }

        @Override
        public java.sql.CallableStatement prepareCall(String s) {
            return null;
        }

        @Override
        public String nativeSQL(String s) {
            return null;
        }

        @Override
        public void setAutoCommit(boolean b) {
        }

        @Override
        public boolean getAutoCommit() {
            return false;
        }

        @Override
        public void commit() {
        }

        @Override
        public void rollback() {
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public java.sql.DatabaseMetaData getMetaData() {
            return null;
        }

        @Override
        public void setReadOnly(boolean b) {
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public void setCatalog(String s) {
        }

        @Override
        public String getCatalog() {
            return null;
        }

        @Override
        public void setTransactionIsolation(int i) {
        }

        @Override
        public int getTransactionIsolation() {
            return 0;
        }

        @Override
        public java.sql.SQLWarning getWarnings() {
            return null;
        }

        @Override
        public void clearWarnings() {
        }

        @Override
        public java.sql.Statement createStatement(int i1, int i2) {
            return null;
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String s, int i1, int i2) {
            return null;
        }

        @Override
        public java.sql.CallableStatement prepareCall(String s, int i1, int i2) {
            return null;
        }

        @Override
        public java.util.Map<String, Class<?>> getTypeMap() {
            return null;
        }

        @Override
        public void setTypeMap(java.util.Map<String, Class<?>> map) {
        }

        @Override
        public void setHoldability(int i) {
        }

        @Override
        public int getHoldability() {
            return 0;
        }

        @Override
        public java.sql.Savepoint setSavepoint() {
            return null;
        }

        @Override
        public java.sql.Savepoint setSavepoint(String s) {
            return null;
        }

        @Override
        public void rollback(java.sql.Savepoint s) {
        }

        @Override
        public void releaseSavepoint(java.sql.Savepoint s) {
        }

        @Override
        public java.sql.Statement createStatement(int i1, int i2, int i3) {
            return null;
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String s, int i1, int i2, int i3) {
            return null;
        }

        @Override
        public java.sql.CallableStatement prepareCall(String s, int i1, int i2, int i3) {
            return null;
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String s, int i) {
            return null;
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String s, int[] ia) {
            return null;
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String s, String[] sa) {
            return null;
        }

        @Override
        public java.sql.Clob createClob() {
            return null;
        }

        @Override
        public java.sql.Blob createBlob() {
            return null;
        }

        @Override
        public java.sql.NClob createNClob() {
            return null;
        }

        @Override
        public java.sql.SQLXML createSQLXML() {
            return null;
        }

        @Override
        public void setClientInfo(String s1, String s2) {
        }

        @Override
        public void setClientInfo(java.util.Properties p) {
        }

        @Override
        public String getClientInfo(String s) {
            return null;
        }

        @Override
        public java.util.Properties getClientInfo() {
            return null;
        }

        @Override
        public java.sql.Array createArrayOf(String s, Object[] oa) {
            return null;
        }

        @Override
        public java.sql.Struct createStruct(String s, Object[] oa) {
            return null;
        }

        @Override
        public void setSchema(String s) {
        }

        @Override
        public String getSchema() {
            return null;
        }

        @Override
        public void abort(java.util.concurrent.Executor e) {
        }

        @Override
        public void setNetworkTimeout(java.util.concurrent.Executor e, int i) {
        }

        @Override
        public int getNetworkTimeout() {
            return 0;
        }
    }
}
