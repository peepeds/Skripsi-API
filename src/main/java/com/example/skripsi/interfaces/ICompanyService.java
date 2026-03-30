package com.example.skripsi.interfaces;

import com.example.skripsi.entities.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.company.*;

import java.util.List;

public interface ICompanyService {
    PageResponse<CompanyOptionsResponse> getCompany(int page, int limit);

    CompanyOptionsResponse getCompanyBySlug(String slug);

    CompanyRequestResponse submitCompanyRequest(CreateCompanyRequestRequest request);

    PageResponse<CompanyRequestResponse> getCompanyRequests(CompanyRequestStatus status, int page, int limit);

    CompanyRequestResponse reviewCompanyRequest(Long requestId, ReviewCompanyRequestRequest request);

    CompanyRequestDetailResponse getCompanyRequestDetail(Long requestId);

    List<CompanyOptionsResponse> searchCompanies(String search);

    List<CompanyOptionsResponse> getTopCompaniesAvgRating();
}
