package com.example.skripsi.interfaces;

import com.example.skripsi.entities.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.company.*;

import java.util.List;
import java.util.Map;

public interface ICompanyService {
    CursorPageResponse<CompanyOptionsResponse> getCompany(Long cursor, int limit, String sort);

    CompanyOptionsResponse getCompanyBySlug(String slug);

    Long getCompanyIdBySlug(String slug);

    String getCompanyRequestName(Long requestId);

    CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryId(Long subCategoryId, Long cursor, int limit);

    CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryIdViaProfile(Long subCategoryId, Long cursor, int limit);

    CursorPageResponse<CompanyOptionsResponse> getCompaniesBySubCategoryNameViaProfile(String subCategoryName, Long cursor, int limit);

    CompanyRequestResponse submitCompanyRequest(CreateCompanyRequestRequest request);

    CursorPageResponse<CompanyRequestResponse> getCompanyRequests(CompanyRequestStatus status, Long cursor, int limit);

    CompanyRequestResponse reviewCompanyRequest(Long requestId, ReviewCompanyRequestRequest request);

    CompanyRequestDetailResponse getCompanyRequestDetail(Long requestId);

    List<CompanyOptionsResponse> searchCompanies(String search);

    List<CompanyOptionsResponse> getTopCompaniesAvgRating(Long userId);

    Boolean isCompanyRequestOwner(Long requestId, Long userId);

    Boolean saveCompany(String slug, SaveCompanyRequest request);

    CursorPageResponse<CompanyOptionsResponse> getMyBookmarks(Long cursor, int limit);

    Map<Long, Company> getCompanyInfoByIds(List<Long> companyIds);
}
