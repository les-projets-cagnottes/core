package fr.lesprojetscagnottes.core.file.service;

import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.common.strings.MimeTypes;
import fr.lesprojetscagnottes.core.file.entity.FileEntity;
import fr.lesprojetscagnottes.core.file.repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.regex.Matcher;

@Slf4j
@Service
public class FileService {

    @Value("${fr.lesprojetscagnottes.core.storage}")
    private String storageFolder;

    @Value("${fr.lesprojetscagnottes.core.url}")
    private String coreUrl;

    @Autowired
    private FileRepository fileRepository;

    public byte[] readOnFilesystem(HttpServletRequest request) throws IOException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String matchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String pathFile = new AntPathMatcher().extractPathWithinPattern(matchPattern, path);
        pathFile = pathFile.replaceAll("/", Matcher.quoteReplacement(File.separator));
        InputStream in = new FileInputStream(storageFolder + File.separator + pathFile);
        log.debug("Getting image {}", storageFolder + File.separator + pathFile);
        return IOUtils.toByteArray(in);
    }

    public FileEntity saveOnFilesystem(MultipartFile multipartFile, String directory, String name) throws IOException {

        String fullPath;
        String finalFileName;

        InputStream inputStream = multipartFile.getInputStream();
        log.debug("inputStream: " + inputStream);
        String originalName = multipartFile.getOriginalFilename();
        log.debug("originalName: " + originalName);
        String contentType = multipartFile.getContentType();
        log.debug("contentType: " + contentType);
        long size = multipartFile.getSize();
        log.debug("size: " + size);
        String format = MimeTypes.getDefaultExt(contentType);
        log.debug("format: " + format);
        finalFileName = name + "." + format;
        log.debug("saved filename: " + finalFileName);

        prepareDirectories(directory);

        fullPath = storageFolder + java.io.File.separator + directory + java.io.File.separator + finalFileName;
        java.io.File file = new java.io.File(fullPath);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        }

        FileEntity entity = new FileEntity();
        entity.setDirectory(directory);
        entity.setName(name);
        entity.setFormat(format);
        entity.setUrl(coreUrl + "/files/" + directory + "/" + finalFileName);

        return entity;
    }

    public FileEntity saveInDb(FileEntity newEntity) {
        FileEntity entity = null;
        if(newEntity.getId() > 0) {
            entity = fileRepository.findById(newEntity.getId()).orElse(null);
        }
        if(entity == null) {
            entity = new FileEntity();
        }
        entity.setDirectory(newEntity.getDirectory());
        entity.setName(newEntity.getName());
        entity.setFormat(newEntity.getFormat());
        entity.setUrl(newEntity.getUrl());
        return fileRepository.save(entity);
    }

    public Boolean delete(Long fileId) {

        // Check if ID is correct
        if(fileId <= 0) {
            log.error("Impossible to delete file : ID is missing");
            throw new BadRequestException();
        }

        // Check if file is registered in DB
        FileEntity entity = fileRepository.findById(fileId).orElse(null);
        if(entity == null) {
            log.error("Impossible to delete file : file {} does not exist in DB", fileId);
            throw new NotFoundException();
        }

        // Delete file in filesystem
        java.io.File file = new java.io.File(getPath(entity));
        if(file.exists()) {
            if(!file.delete()) {
                log.error("Impossible to delete file : unknown error");
            }
        } else {
            log.error("Impossible to delete file : file {} does not exist in filesystem", fileId);
        }

        // Delete file in DB
        fileRepository.delete(entity);

        return Boolean.TRUE;
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
            directory = new File(storageFolder + File.separator + directoryPath.replaceAll("//", File.separator));
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

    public String getPath(FileEntity entity) {
        return storageFolder + java.io.File.separator + entity.getDirectory() + java.io.File.separator + entity.getFullname();
    }

}
