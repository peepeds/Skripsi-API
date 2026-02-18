package com.example.skripsi.interfaces;

import com.example.skripsi.models.PageResponse;
import com.example.skripsi.models.company.CompanyOptionsResponse;

public interface ICompanyService {
    PageResponse<CompanyOptionsResponse> getCompany(int page, int limit);
}
