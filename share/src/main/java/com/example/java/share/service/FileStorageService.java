package com.example.java.share.service;

import com.example.java.share.dto.FileResponse;
import com.example.java.share.dto.FileUrlResponse;
import com.example.java.share.dto.CloudinaryUploadResult;
import com.example.java.share.dto.ShareFileRequest;
import com.example.java.share.dto.ShareFileResponse;
import com.example.java.share.entity.FileEntity;
import com.example.java.share.entity.SharedFileLink;
import com.example.java.share.entity.User;
import com.example.java.share.exception.BadRequestException;
import com.example.java.share.exception.ResourceNotFoundException;
import com.example.java.share.repository.FileRepository;
import com.example.java.share.repository.SharedFileLinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileRepository fileRepository;
    private final SharedFileLinkRepository sharedFileLinkRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public FileResponse upload(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (!StringUtils.hasText(originalFileName) || originalFileName.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }
        CloudinaryUploadResult cloudinaryFile = cloudinaryService.uploadFile(file);

        FileEntity savedFile = fileRepository.save(FileEntity.builder()
                .originalFileName(originalFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .cloudinaryPublicId(cloudinaryFile.publicId())
                .cloudinarySecureUrl(cloudinaryFile.secureUrl())
                .uploadedBy(user)
                .build());

        return toFileResponse(savedFile);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listUserFiles(User user) {
        return fileRepository.findByUploadedByOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toFileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FileUrlResponse downloadOwnFile(Long fileId, User user) {
        FileEntity file = getOwnFile(fileId, user);
        return toFileUrlResponse(file);
    }

    @Transactional
    public void deleteOwnFile(Long fileId, User user) {
        FileEntity file = getOwnFile(fileId, user);
        cloudinaryService.deleteFile(file.getCloudinaryPublicId());
        fileRepository.delete(file);
    }

    @Transactional
    public ShareFileResponse shareFile(Long fileId, ShareFileRequest request, User user, HttpServletRequest servletRequest) {
        FileEntity file = getOwnFile(fileId, user);
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(request.expiresInMinutes());

        sharedFileLinkRepository.save(SharedFileLink.builder()
                .token(token)
                .file(file)
                .createdBy(user)
                .expiresAt(expiresAt)
                .build());

        String baseUrl = servletRequest.getRequestURL()
                .toString()
                .replace(servletRequest.getRequestURI(), "");
        return new ShareFileResponse(
                token,
                baseUrl + "/api/share/" + token,
                cloudinaryService.getSecureUrl(file),
                expiresAt
        );
    }

    @Transactional(readOnly = true)
    public FileUrlResponse downloadSharedFile(String token) {
        SharedFileLink sharedLink = sharedFileLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found"));

        if (sharedLink.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Shared link has expired");
        }

        return toFileUrlResponse(sharedLink.getFile());
    }

    @Transactional(readOnly = true)
    public FileEntity getOwnFile(Long fileId, User user) {
        return fileRepository.findByIdAndUploadedBy(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    private FileResponse toFileResponse(FileEntity file) {
        return new FileResponse(
                file.getId(),
                file.getOriginalFileName(),
                file.getFileType(),
                file.getFileSize(),
                file.getCloudinaryPublicId(),
                cloudinaryService.getSecureUrl(file),
                file.getCreatedAt()
        );
    }

    private FileUrlResponse toFileUrlResponse(FileEntity file) {
        return new FileUrlResponse(
                file.getId(),
                file.getOriginalFileName(),
                cloudinaryService.getSecureUrl(file)
        );
    }
}
