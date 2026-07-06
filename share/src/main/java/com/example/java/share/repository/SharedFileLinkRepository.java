package com.example.java.share.repository;

import com.example.java.share.entity.SharedFileLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedFileLinkRepository extends JpaRepository<SharedFileLink, Long> {
    Optional<SharedFileLink> findByToken(String token);
}
