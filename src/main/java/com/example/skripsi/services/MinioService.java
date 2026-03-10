package com.example.skripsi.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
public class MinioService {

    private static final SecureRandom random = new SecureRandom();

    public String generateFileName(String extension) {
        // Generate UUID v7: timestamp (48 bits) + version (4 bits) + random (76 bits)
        long timestamp = Instant.now().toEpochMilli();
        long msb = (timestamp << 16) | (7L << 12) | (random.nextLong() & 0xFFF);
        long lsb = random.nextLong();
        UUID uuid = new UUID(msb, lsb);
        return uuid.toString() + "." + extension;
    }
}
