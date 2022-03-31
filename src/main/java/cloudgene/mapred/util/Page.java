package cloudgene.mapred.util;

import java.util.List;

public class Page<o extends Object> {

	private List<o> data;

	private int count;

	private int pageSize;

	private int page;

	public List<o> getData() {
		return data;
	}

	public void setData(List<o> data) {
		this.data = data;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

}
