package com.example.java.share.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.java.share.dto.CloudinaryUploadResult;
import com.example.java.share.entity.FileEntity;
import com.example.java.share.exception.CloudinaryStorageException;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final String FILE_FOLDER = "secure-file-sharing";

    private final Cloudinary cloudinary;

    public CloudinaryUploadResult uploadFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", FILE_FOLDER,
                            "resource_type", "auto",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );

            String publicId = String.valueOf(uploadResult.get("public_id"));
            String secureUrl = String.valueOf(uploadResult.get("secure_url"));
            if (publicId == null || secureUrl == null || "null".equals(publicId) || "null".equals(secureUrl)) {
                throw new CloudinaryStorageException("Cloudinary upload response was invalid");
            }
            return new CloudinaryUploadResult(publicId, secureUrl);
        } catch (IOException | RuntimeException ex) {
            throw new CloudinaryStorageException("Could not upload file to Cloudinary");
        }
    }

    public void deleteFile(String publicId) {
        try {
            Map<?, ?> deleteResult = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "auto", "invalidate", true)
            );
            String result = String.valueOf(deleteResult.get("result"));
            if (!"ok".equalsIgnoreCase(result) && !"not found".equalsIgnoreCase(result)) {
                throw new CloudinaryStorageException("Cloudinary could not delete the file");
            }
        } catch (IOException | RuntimeException ex) {
            throw new CloudinaryStorageException("Could not delete file from Cloudinary");
        }
    }

    public String getSecureUrl(FileEntity file) {
        return file.getCloudinarySecureUrl();
    }
}
