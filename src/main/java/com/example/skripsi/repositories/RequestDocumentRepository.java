package com.example.skripsi.repositories;
import com.example.skripsi.entities.RequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RequestDocumentRepository extends JpaRepository<RequestDocument, Long> {
    List<RequestDocument> findByNotification_NotificationId(Long notificationId);
}
