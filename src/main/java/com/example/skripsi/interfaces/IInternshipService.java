package com.example.skripsi.interfaces;

import com.example.skripsi.entities.InternshipHeader;
import com.example.skripsi.entities.InternshipDetail;

import java.util.List;
import java.util.Optional;

public interface IInternshipService {
    InternshipHeader createInternshipHeader(InternshipHeader header);

    InternshipHeader updateInternshipHeader(Long internshipHeaderId, InternshipHeader headerDetails);

    void deleteInternshipHeader(Long internshipHeaderId);

    InternshipHeader getInternshipHeaderById(Long internshipHeaderId);

    List<InternshipHeader> getInternshipsByUser(Long userId);

    List<InternshipHeader> getInternshipsByCompany(Long companyId);

    Optional<InternshipHeader> getInternshipByUserAndCompany(Long userId, Long companyId);

    InternshipDetail createInternshipDetail(InternshipDetail detail);

    InternshipDetail updateInternshipDetail(Long internshipDetailId, InternshipDetail detailsData);

    InternshipDetail getInternshipDetailById(Long internshipDetailId);

    InternshipDetail getInternshipDetailByHeaderId(Long internshipHeaderId);

    void deleteInternshipDetail(Long internshipDetailId);
}
