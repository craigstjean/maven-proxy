package com.craigstjean.mavenproxy.service.cache;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.craigstjean.mavenproxy.model.ProxyConfiguration;
import com.craigstjean.mavenproxy.model.dto.UrlStream;
import com.craigstjean.mavenproxy.model.dto.UrlStreamHeader;

public class CacheImpl implements Cache {

	private static final Logger logger = LoggerFactory.getLogger(CacheImpl.class);

	@Inject
	private ProxyConfiguration configuration;

	private JdbcConnectionPool connectionPool;

	@PostConstruct
	public void init() {
		String path = configuration.getCache();
		if (path.startsWith("~" + File.separator)) {
			path = System.getProperty("user.home") + path.substring(1);
		}

		new File(path + File.separator).mkdirs();

		connectionPool = JdbcConnectionPool.create("jdbc:h2:" + configuration.getCache() + "/cache", "sa", "sa");
		createTables();
	}

	private void createTables() {
		try (Connection connection = connectionPool.getConnection()) {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS FILES (ID INT AUTO_INCREMENT PRIMARY KEY, URL VARCHAR(2000), FILENAME VARCHAR(40) NOT NULL)");

			stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS UX_FILE_URL ON FILES (URL)");

			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS HEADERS (ID INT AUTO_INCREMENT PRIMARY KEY, FILE_ID INT NOT NULL, KEY VARCHAR(200) NOT NULL, VALUE VARCHAR(2000))");
		} catch (SQLException se) {
			logger.error("Error creating database", se);
		}
	}

	@Override
	public String getFilePath(String url) {
		String filePath = null;

		try (Connection connection = connectionPool.getConnection()) {
			PreparedStatement stmt = connection.prepareStatement("SELECT FILENAME FROM FILES WHERE LOWER(URL) = ?");
			stmt.setString(1, url.toLowerCase());

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				filePath = rs.getString("FILENAME");
			}

			rs.close();
			stmt.close();
		} catch (SQLException se) {
			logger.error("Error querying for file path: " + url, se);
		}

		return filePath;
	}

	@Override
	public List<UrlStreamHeader> getHeaders(String url) {
		List<UrlStreamHeader> headers = new ArrayList<>();

		try (Connection connection = connectionPool.getConnection()) {
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT H.KEY, H.VALUE FROM HEADERS H JOIN FILES F ON F.ID = H.FILE_ID WHERE LOWER(F.URL) = ?");
			stmt.setString(1, url.toLowerCase());

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				headers.add(new UrlStreamHeader(rs.getString("KEY"), rs.getString("VALUE")));
			}

			rs.close();
			stmt.close();
		} catch (SQLException se) {
			logger.error("Error querying for headers for: " + url, se);
		}

		return headers;
	}

	@Override
	public void cacheUrlStream(String url, UrlStream urlStream) {
		try (Connection connection = connectionPool.getConnection()) {
			connection.setAutoCommit(false);

			PreparedStatement stmt = connection.prepareStatement("INSERT INTO FILES (URL, FILENAME) VALUES (?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, url);
			stmt.setString(2, urlStream.getFilePath());

			stmt.executeUpdate();

			Long fileId = null;
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				fileId = rs.getLong(1);
			}

			rs.close();
			stmt.close();

			stmt = connection.prepareStatement("INSERT INTO HEADERS (FILE_ID, KEY, VALUE) VALUES (?, ?, ?)");
			for (UrlStreamHeader header : urlStream.getHeaders()) {
				stmt.setLong(1, fileId);
				stmt.setString(2, header.getKey());
				stmt.setString(3, header.getValue());
				stmt.addBatch();
			}

			stmt.executeBatch();
			stmt.close();

			connection.commit();
		} catch (SQLException se) {
			logger.error("Error writing to cache", se);
			throw new RuntimeException(se);
		}
	}

}
