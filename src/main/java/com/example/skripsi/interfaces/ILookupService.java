package com.example.skripsi.interfaces;

import com.example.skripsi.entities.*;

import java.util.List;
import java.util.Optional;

public interface ILookupService {
    Lookup createLookup(Lookup lookup);
    Lookup updateLookup(Long lookupId, Lookup lookupDetails);
    void deleteLookup(Long lookupId);
    Lookup getLookupById(Long lookupId);
    List<Lookup> getAllLookups();
    List<Lookup> getLookupsByType(String lookupType);
    Optional<Lookup> getLookupByTypeAndCode(String lookupType, String lookupCode);
}
