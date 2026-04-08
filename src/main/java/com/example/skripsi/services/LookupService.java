package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LookupService implements ILookupService {
    private final LookupRepository lookupRepository;

    public LookupService(LookupRepository lookupRepository) {
        this.lookupRepository = lookupRepository;
    }

    public Lookup createLookup(Lookup lookup) {
        return lookupRepository.save(lookup);
    }

    public Lookup updateLookup(Long lookupId, Lookup lookupDetails) {
        Lookup lookup = lookupRepository.findById(lookupId)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup not found"));
        lookup.setLookupType(lookupDetails.getLookupType());
        lookup.setLookupCode(lookupDetails.getLookupCode());
        lookup.setLookupValue(lookupDetails.getLookupValue());
        lookup.setLookupDescription(lookupDetails.getLookupDescription());
        lookup.setUpdatedAt(lookupDetails.getUpdatedAt());
        lookup.setUpdatedBy(lookupDetails.getUpdatedBy());
        return lookupRepository.save(lookup);
    }

    public void deleteLookup(Long lookupId) {
        lookupRepository.deleteById(lookupId);
    }

    public Lookup getLookupById(Long lookupId) {
        return lookupRepository.findById(lookupId)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup not found"));
    }

    public List<Lookup> getAllLookups() {
        return lookupRepository.findAll();
    }

    public List<Lookup> getLookupsByType(String lookupType) {
        return lookupRepository.findByLookupType(lookupType);
    }

    public Optional<Lookup> getLookupByTypeAndCode(String lookupType, String lookupCode) {
        return lookupRepository.findFirstByLookupTypeAndLookupCode(lookupType, lookupCode);
    }
}
