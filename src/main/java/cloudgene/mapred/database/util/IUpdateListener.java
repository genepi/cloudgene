package cloudgene.mapred.database.util;

public interface IUpdateListener {

	public void beforeUpdate(Database database);
	
	public void afterUpdate(Database database);
	
}
