package com.example.skripsi.services;

import com.example.skripsi.entities.Company;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.interfaces.ICompanyService;
import com.example.skripsi.models.PageResponse;
import com.example.skripsi.models.company.CompanyOptionsResponse;
import com.example.skripsi.repositories.CompanyRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CompanyService implements ICompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public PageResponse<CompanyOptionsResponse> getCompany(int page, int limit) {

        final int MAX_TOTAL_ELEMENTS = 1000;

        int requestedOffset = page * limit;
        if (requestedOffset >= MAX_TOTAL_ELEMENTS) {
            throw new BadRequestExceptions("limit exceeded");
        }

        Pageable pageable = PageRequest.of(page, limit);
        Page<CompanyOptionsResponse> pageResult =
                companyRepository.findAll(pageable)
                        .map(this::toOptionsResponse);

        long actualTotalElements = pageResult.getTotalElements();
        long cappedTotalElements = Math.min(actualTotalElements, MAX_TOTAL_ELEMENTS);

        int totalPages = calculateTotalPages(cappedTotalElements, limit);
        boolean hasNext = hasNextPage(page, limit, cappedTotalElements);

        return PageResponse.<CompanyOptionsResponse>builder()
                .result(pageResult.getContent())
                .meta(PageResponse.Meta.builder()
                        .page(page)
                        .size(limit)
                        .totalElements(cappedTotalElements)
                        .totalPages(totalPages)
                        .hasNext(hasNext)
                        .hasPrevious(page > 0)
                        .build())
                .build();
    }

    private int calculateTotalPages(long totalElements, int limit) {
        return (int) Math.ceil((double) totalElements / limit);
    }

    private boolean hasNextPage(int page, int limit, long totalElements) {
        return (long) (page + 1) * limit < totalElements;
    }

    private CompanyOptionsResponse toOptionsResponse(Company company){
        return CompanyOptionsResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .build();

    }
}
