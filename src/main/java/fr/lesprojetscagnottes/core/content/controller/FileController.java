package fr.lesprojetscagnottes.core.content.controller;

import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.strings.MimeTypes;
import fr.lesprojetscagnottes.core.content.entity.FileEntity;
import fr.lesprojetscagnottes.core.content.model.FileModel;
import fr.lesprojetscagnottes.core.content.repository.FileRepository;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequestMapping("/api")
@Tag(name = "Files", description = "The Files API")
@RestController
public class FileController {

    @Value("${fr.lesprojetscagnottes.core.storage}")
    private String storageFolder;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/files", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileEntity> list() {
        return fileRepository.findAll();
    }
    
    @RequestMapping(value = "/files/image", method = RequestMethod.POST)
    public FileModel uploadImage(@RequestParam("image") MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new RuntimeException("You must select a file for uploading");
        }

        FileModel fileModel = null;
        String finalFileName = multipartFile.getOriginalFilename();
        String fullPath;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            log.debug("inputStream: " + inputStream);
            String originalName = multipartFile.getOriginalFilename();
            log.debug("originalName: " + originalName);
            String contentType = multipartFile.getContentType();
            log.debug("contentType: " + contentType);
            long size = multipartFile.getSize();
            log.debug("size: " + size);
            String extension = MimeTypes.getDefaultExt(contentType);
            log.debug("extension: " + extension);

            if(!isValidExtension(extension)) {
                throw new BadRequestException();
            }

            LocalDate now = LocalDate.now();
            String directoryPath = String.format("img" + File.separator + "%d" + File.separator + "%d", now.getYear(), now.getMonth().getValue());
            prepareDirectories(directoryPath);

            String directoryWithFile = directoryPath + File.separator + UUID.randomUUID() + "." + extension;
            fullPath = storageFolder + File.separator + directoryWithFile;

            FileEntity entity = new FileEntity();
            entity.setFilename(UUID.randomUUID().toString());
            entity.setOriginalName(originalName);
            entity.setExtension(extension);
            entity.setDirectory(directoryPath);
            entity.setPath(directoryWithFile);
            entity = fileRepository.save(entity);
            fileModel = FileModel.fromEntity(entity);

            File file = new File(fullPath);
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(multipartFile.getBytes());
            }

        } catch (IOException e) {
            log.error("Cannot save file {}", finalFileName, e);
        }

        return fileModel;
    }

    @RequestMapping(value = "/files/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") String id) {
        Optional<FileEntity> optionalFile = fileRepository.findByFilename(id);
        optionalFile.ifPresent(fileEntity -> {
            File file = new File(storageFolder + File.separator + fileEntity.getDirectory() + File.separator + fileEntity.getFilename() + "." + fileEntity.getExtension());
            if(file.delete()) {
                fileRepository.delete(fileEntity);
            }
        });
    }

    private boolean isValidExtension(String extension) {
        return switch (extension) {
            case "jpg", "gif", "png" -> true;
            default -> false;
        };
    }

    private void prepareDirectories(String directoryPath) {
        File directory = new File(storageFolder);
        if (!directory.exists()) {
            log.info("Creating path {}", directory.getPath());
            if (!directory.isDirectory()) {
                log.error("The path {} is not a directory", directory.getPath());
            }
        }
        if (!directory.isDirectory()) {
            log.error("The path {} is not a directory", directory.getPath());
        }

        if (directoryPath != null && !directoryPath.isEmpty()) {
            directory = new File(storageFolder + File.separator + directoryPath.replaceAll("/", File.separator));
            log.debug("Prepare directory {}", directory.getAbsolutePath());
            if (!directory.exists()) {
                log.info("Creating path {}", directory.getPath());
                if(!directory.mkdirs()) {
                    log.error("Cannot create directory {}", directory.getAbsolutePath());
                }
            }
            if (!directory.isDirectory()) {
                log.error("The path {} is not a directory", directory.getPath());
            }
        }
    }

}
