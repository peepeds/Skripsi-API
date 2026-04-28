package com.example.skripsi.services;

import com.example.skripsi.entities.CompanyRequestStatus;
import com.example.skripsi.interfaces.IDashboardService;
import com.example.skripsi.models.dashboard.DashboardStatisticsResponse;
import com.example.skripsi.models.dashboard.TopReviewCompanyResponse;
import com.example.skripsi.models.dashboard.TrendMonthResponse;
import com.example.skripsi.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class DashboardService implements IDashboardService {

    private final CompanyRepository companyRepository;
    private final CompanyRequestRepository companyRequestRepository;
    private final UserRepository userRepository;
    private final UserCertificateRequestRepository userCertificateRequestRepository;
    private final InternshipDetailRepository internshipDetailRepository;

    public DashboardService(CompanyRepository companyRepository,
                            CompanyRequestRepository companyRequestRepository,
                            UserRepository userRepository,
                            UserCertificateRequestRepository userCertificateRequestRepository,
                            InternshipDetailRepository internshipDetailRepository) {
        this.companyRepository = companyRepository;
        this.companyRequestRepository = companyRequestRepository;
        this.userRepository = userRepository;
        this.userCertificateRequestRepository = userCertificateRequestRepository;
        this.internshipDetailRepository = internshipDetailRepository;
    }

    @Override
    public DashboardStatisticsResponse getStatistics() {
        long totalCompanies = companyRepository.count();
        long pendingCompanyRequests = companyRequestRepository.countByStatus(CompanyRequestStatus.PENDING);
        long pendingCertificateRequests = userCertificateRequestRepository.countByStatus("PENDING");
        long totalUsers = userRepository.count();
        long totalReviews = internshipDetailRepository.count();

        return DashboardStatisticsResponse.builder()
                .totalCompanies(totalCompanies)
                .pendingVerifications(pendingCompanyRequests + pendingCertificateRequests)
                .totalUsers(totalUsers)
                .totalReviews(totalReviews)
                .build();
    }

    @Override
    public List<TopReviewCompanyResponse> getTopReviews() {
        return internshipDetailRepository.findTop10CompaniesWithReviewStats().stream()
                .map(p -> TopReviewCompanyResponse.builder()
                        .companyName(p.getCompanyName())
                        .totalReviews(p.getTotalReviews())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TrendMonthResponse> getTrends() {
        Map<String, Long> reviewsByMonth = internshipDetailRepository.countReviewsPerMonthLast6Months()
                .stream()
                .collect(Collectors.toMap(p -> p.getMonth(), p -> p.getCount()));

        Map<String, Long> companiesByMonth = companyRepository.countNewCompaniesPerMonthLast6Months()
                .stream()
                .collect(Collectors.toMap(p -> p.getMonth(), p -> p.getCount()));

        YearMonth current = YearMonth.now();
        YearMonth start = current.minusMonths(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<TrendMonthResponse> result = new ArrayList<>();
        for (YearMonth ym = start; !ym.isAfter(current); ym = ym.plusMonths(1)) {
            String key = ym.format(formatter);
            result.add(TrendMonthResponse.builder()
                    .month(key)
                    .reviewCount(reviewsByMonth.getOrDefault(key, 0L))
                    .newCompanyCount(companiesByMonth.getOrDefault(key, 0L))
                    .build());
        }
        return result;
    }
}
