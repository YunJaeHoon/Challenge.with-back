package Challenge.with_back.domain.evidence_photo;

import Challenge.with_back.entity.rdbms.EvidencePhoto;
import Challenge.with_back.response.exception.CustomException;
import Challenge.with_back.response.exception.CustomExceptionCode;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class S3EvidencePhotoManager
{
    private final S3Template s3Template;

    // 가능한 확장자
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");

    @Value("${EVIDENCE_PHOTO_BUCKET_NAME}")
    private String bucketName;

    // S3 이미지 업로드
    public S3EvidencePhoto upload(MultipartFile file, String filename)
    {
        // 파일이 비어있는지 확인
        if(file.isEmpty())
            throw new CustomException(CustomExceptionCode.FILE_IS_EMPTY, null);

        // 이미지 파일인지 확인
        String contentType = file.getContentType();
        if(contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(CustomExceptionCode.INVALID_FILE_TYPE, contentType);
        }

        // 파일 확장자 확인
        String extension = getExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension))
            throw new CustomException(CustomExceptionCode.INVALID_FILE_EXTENSION, extension);

        // 업로드
        try(InputStream inputStream = file.getInputStream()) {
            S3Resource s3Resource = s3Template.upload(bucketName, filename + "." + extension, inputStream);

            return S3EvidencePhoto.builder()
                    .photoUrl(s3Resource.getURL().toString())
                    .filename(s3Resource.getFilename())
                    .build();
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.S3_UPLOAD_ERROR, e.getMessage());
        }
    }

    // S3 이미지 삭제
    public void delete(String filename)
    {
        try {
            s3Template.deleteObject(bucketName, filename);
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.S3_DELETE_ERROR, e.getMessage());
        }
    }

    // 파일로부터 확장자 추출
    private static String getExtension(MultipartFile file)
    {
        // 파일 이름
        String originalFilename = file.getOriginalFilename();

        // 파일 이름 존재 확인
        if(originalFilename == null || originalFilename.isEmpty()) {
            throw new CustomException(CustomExceptionCode.FILE_NAME_NOT_FOUND, null);
        }

        // 파일 확장자 존재 확인
        int extensionIndex = originalFilename.lastIndexOf('.');
        if (extensionIndex == -1)
            throw new CustomException(CustomExceptionCode.FILE_EXTENSION_NOT_FOUND, null);

        return originalFilename.substring(extensionIndex + 1).toLowerCase();
    }
}
