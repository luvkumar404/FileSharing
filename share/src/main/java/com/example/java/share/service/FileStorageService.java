package com.example.java.share.service;

import com.example.java.share.dto.FileResponse;
import com.example.java.share.dto.ShareFileRequest;
import com.example.java.share.dto.ShareFileResponse;
import com.example.java.share.entity.FileEntity;
import com.example.java.share.entity.SharedFileLink;
import com.example.java.share.entity.User;
import com.example.java.share.exception.BadRequestException;
import com.example.java.share.exception.ResourceNotFoundException;
import com.example.java.share.repository.FileRepository;
import com.example.java.share.repository.SharedFileLinkRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileRepository fileRepository;
    private final SharedFileLinkRepository sharedFileLinkRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() throws IOException {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
    }

    @Transactional
    public FileResponse upload(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "-" + originalFileName;
        Path targetPath = uploadPath.resolve(storedFileName).normalize();

        if (!targetPath.startsWith(uploadPath)) {
            throw new BadRequestException("Invalid file name");
        }

        try {
            file.transferTo(targetPath);
        } catch (IOException ex) {
            throw new BadRequestException("Could not store file");
        }

        FileEntity savedFile = fileRepository.save(FileEntity.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(targetPath.toString())
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
    public Resource downloadOwnFile(Long fileId, User user) {
        FileEntity file = getOwnFile(fileId, user);
        return loadResource(file);
    }

    @Transactional
    public void deleteOwnFile(Long fileId, User user) {
        FileEntity file = getOwnFile(fileId, user);
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
        } catch (IOException ex) {
            throw new BadRequestException("Could not delete file from storage");
        }
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
        return new ShareFileResponse(token, baseUrl + "/api/share/" + token, expiresAt);
    }

    @Transactional(readOnly = true)
    public Resource downloadSharedFile(String token) {
        SharedFileLink sharedLink = sharedFileLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found"));

        if (sharedLink.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Shared link has expired");
        }

        return loadResource(sharedLink.getFile());
    }

    @Transactional(readOnly = true)
    public FileEntity getOwnFile(Long fileId, User user) {
        return fileRepository.findByIdAndUploadedBy(fileId, user)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    @Transactional(readOnly = true)
    public FileEntity getSharedFile(String token) {
        SharedFileLink sharedLink = sharedFileLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found"));
        if (sharedLink.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Shared link has expired");
        }
        return sharedLink.getFile();
    }

    private Resource loadResource(FileEntity file) {
        try {
            Path path = Paths.get(file.getFilePath()).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Stored file not found");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Stored file not found");
        }
    }

    private FileResponse toFileResponse(FileEntity file) {
        return new FileResponse(
                file.getId(),
                file.getOriginalFileName(),
                file.getStoredFileName(),
                file.getFileType(),
                file.getFileSize(),
                file.getCreatedAt()
        );
    }
}
