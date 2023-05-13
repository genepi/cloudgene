package cloudgene.mapred.server.responses;

import java.util.ArrayList;
import java.util.List;

import cloudgene.mapred.util.Page;

public class PageResponse {

	private int count;
	private int page;
	private int pageSize;
	private int prev;
	private int next;
	private ArrayList<Integer> pages;
	private List<?> data;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPrev() {
		return prev;
	}

	public void setPrev(int prev) {
		this.prev = prev;
	}

	public int getNext() {
		return next;
	}

	public void setNext(int next) {
		this.next = next;
	}

	public ArrayList<Integer> getPages() {
		return pages;
	}

	public void setPages(ArrayList<Integer> pages) {
		this.pages = pages;
	}

	public List<?> getData() {
		return data;
	}

	public void setData(List<?> data) {
		this.data = data;
	}

	public static PageResponse build(Page<?> page, List<?> responses) {
		return build(page.getPage(), page.getPageSize(), page.getCount(), responses);
	}

	public static PageResponse build(int page, int pageSize, int count, List<?> responses) {

		PageResponse response = new PageResponse();
		response.setData(responses);

		response.setCount(count);

		if (count != 0) {
			response.setPage(page);
			int pageCount = (count + pageSize - 1) / pageSize;
			if (page > 1) {
				response.setPrev(page - 1);
			}

			if (page < pageCount) {
				response.setNext(page + 1);
			}

			ArrayList<Integer> pages = new ArrayList<Integer>();

			for (int i = 3; i > 0; i--) {
				if (page - i > 0) {
					pages.add(page - i);
				}
			}
			pages.add(page);
			for (int i = 1; i <= 3; i++) {
				if (page + i <= pageCount) {
					pages.add(page + i);
				}
			}
			response.setPages(pages);
		} else {
			response.setPage(0);
			ArrayList<Integer> pages = new ArrayList<Integer>();
			response.setPages(pages);
		}
		return response;
	}

}
