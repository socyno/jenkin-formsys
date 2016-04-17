package org.socyno.jenkins.formsys;

import org.socyno.jenkins.formsys.Messages;

public class Pager {
	public final long totalSize;
	public final long pageNumber;
	public final long nextPage;
	public final long previousPage;
	public final long pageCount;
	public final int perPageSize;
	public final int currentPageSize;
	
	public Pager(long total, int perSize) throws SysException {
		this(total, perSize, 1);
	}
	
	public Pager(long total, int perSize, long currPage) throws SysException {
		totalSize = total;
		perPageSize = perSize;
		pageNumber = currPage;
		pageCount = (long)(totalSize / perPageSize) + 1;
		
		if ( totalSize < 0 || perPageSize <= 0 || currPage > pageCount ) {
			throw new SysException(
				Messages.SCMErrorPagerInitialization()
			);
		}
		previousPage = pageNumber - 1;
		nextPage = pageNumber < pageCount ? pageNumber + 1 : 0;
		currentPageSize = pageNumber == pageCount ? (int)(totalSize % perPageSize) : perPageSize;
	}
}
