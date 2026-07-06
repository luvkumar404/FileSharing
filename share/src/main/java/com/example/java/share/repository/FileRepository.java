package com.example.java.share.repository;

import com.example.java.share.entity.FileEntity;
import com.example.java.share.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByUploadedByOrderByCreatedAtDesc(User uploadedBy);

    Optional<FileEntity> findByIdAndUploadedBy(Long id, User uploadedBy);
}
