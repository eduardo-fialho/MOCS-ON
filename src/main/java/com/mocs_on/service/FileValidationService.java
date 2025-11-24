package com.mocs_on.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class FileValidationService {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "docs", "otd", "png"
    ));

    public boolean isAllowedExtension(String filename) {
        if (filename == null) return false;
        int idx = filename.lastIndexOf('.');
        if (idx == -1) return false;
        String ext = filename.substring(idx + 1).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(ext);
    }
}
