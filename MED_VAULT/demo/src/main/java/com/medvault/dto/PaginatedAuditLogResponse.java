package com.medvault.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedAuditLogResponse {
    private List<AuditLogResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;

    // Manual getters and setters for compatibility
    public List<AuditLogResponse> getContent() { return content; }
    public void setContent(List<AuditLogResponse> content) { this.content = content; }
    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public boolean getIsFirst() { return isFirst; }
    public void setIsFirst(boolean isFirst) { this.isFirst = isFirst; }
    public boolean getIsLast() { return isLast; }
    public void setIsLast(boolean isLast) { this.isLast = isLast; }

    // Builder pattern static method
    public static PaginatedAuditLogResponseBuilder builder() {
        return new PaginatedAuditLogResponseBuilder();
    }

    // Builder inner class
    public static class PaginatedAuditLogResponseBuilder {
        private List<AuditLogResponse> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean isFirst;
        private boolean isLast;

        public PaginatedAuditLogResponseBuilder content(List<AuditLogResponse> content) { this.content = content; return this; }
        public PaginatedAuditLogResponseBuilder pageNumber(int pageNumber) { this.pageNumber = pageNumber; return this; }
        public PaginatedAuditLogResponseBuilder pageSize(int pageSize) { this.pageSize = pageSize; return this; }
        public PaginatedAuditLogResponseBuilder totalElements(long totalElements) { this.totalElements = totalElements; return this; }
        public PaginatedAuditLogResponseBuilder totalPages(int totalPages) { this.totalPages = totalPages; return this; }
        public PaginatedAuditLogResponseBuilder isFirst(boolean isFirst) { this.isFirst = isFirst; return this; }
        public PaginatedAuditLogResponseBuilder isLast(boolean isLast) { this.isLast = isLast; return this; }

        public PaginatedAuditLogResponse build() {
            PaginatedAuditLogResponse response = new PaginatedAuditLogResponse();
            response.content = this.content;
            response.pageNumber = this.pageNumber;
            response.pageSize = this.pageSize;
            response.totalElements = this.totalElements;
            response.totalPages = this.totalPages;
            response.isFirst = this.isFirst;
            response.isLast = this.isLast;
            return response;
        }
    }
}
