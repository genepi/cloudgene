package cloudgene.mapred.database.util;

import org.apache.commons.dbcp.BasicDataSource;

public abstract class AbstractDatabaseConnector implements DatabaseConnector {

	private int maxActive = 10;

	private int maxWait = 10000;

	private boolean defaultAutoCommit = true;

	private boolean testWhileIdle = true;

	private int minEvictableIdleTimeMillis = 1800000;

	private int timeBetweenEvictionRunsMillis = 1800000;

	protected BasicDataSource createDataSource() {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setMaxActive(maxActive);
		dataSource.setMaxWait(maxWait);
		dataSource.setMaxIdle(maxActive);
		dataSource.setDefaultAutoCommit(defaultAutoCommit);
		dataSource.setTestWhileIdle(testWhileIdle);
		dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		return dataSource;

	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}

	public boolean isDefaultAutoCommit() {
		return defaultAutoCommit;
	}

	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		this.defaultAutoCommit = defaultAutoCommit;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public int getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public int getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

}
