package cloudgene.mapred.jobs;


public class Download implements Comparable<Download> {

	private String name = "";
	private String path = "";
	private String hash = "";
	private int count = 0;
	private String size;
	private CloudgeneParameterOutput parameter;
	private int parameterId;
	private String user;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public void setParameter(CloudgeneParameterOutput parameter) {
		this.parameter = parameter;
	}

	public CloudgeneParameterOutput getParameter() {
		return parameter;
	}

	public void setParameterId(int parameterId) {
		this.parameterId = parameterId;
	}

	public int getParameterId() {
		return parameterId;
	}

	public void decCount() {
		count--;
	}

	public void setUsername(String user) {
		this.user = user;
	}

	public String getUsername() {
		return user;
	}

	@Override
	public int compareTo(Download o) {
		return name.compareTo(o.getName());

	}

}
