package com.example.skripsi.interfaces;

import com.example.skripsi.entities.CompanyRequestStatus;
import com.example.skripsi.models.PageResponse;
import com.example.skripsi.models.company.CompanyOptionsResponse;
import com.example.skripsi.models.company.CompanyRequestDetailResponse;
import com.example.skripsi.models.company.CompanyRequestResponse;
import com.example.skripsi.models.company.CreateCompanyRequestRequest;
import com.example.skripsi.models.company.ReviewCompanyRequestRequest;

import java.util.List;

public interface ICompanyService {
    PageResponse<CompanyOptionsResponse> getCompany(int page, int limit);

    CompanyRequestResponse submitCompanyRequest(CreateCompanyRequestRequest request);

    PageResponse<CompanyRequestResponse> getCompanyRequests(CompanyRequestStatus status, int page, int limit);

    CompanyRequestResponse reviewCompanyRequest(Long requestId, ReviewCompanyRequestRequest request);

    CompanyRequestDetailResponse getCompanyRequestDetail(Long requestId);

    List<CompanyOptionsResponse> searchCompanies(String search);
}
