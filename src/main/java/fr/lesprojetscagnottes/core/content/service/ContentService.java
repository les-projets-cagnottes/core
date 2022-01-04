package fr.lesprojetscagnottes.core.content.service;

import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.content.repository.ContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    public ContentEntity findById(Long id) {
        return contentRepository.findById(id).orElse(null);
    }

}
