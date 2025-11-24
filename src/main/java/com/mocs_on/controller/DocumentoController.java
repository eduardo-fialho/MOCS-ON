package com.mocs_on.controller;

import com.mocs_on.service.DocumentoService;
import com.mocs_on.service.FileValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/documentos")
public class DocumentoController {

    private final FileValidationService validationService;
    private final DocumentoService documentoService;

    @Autowired
    public DocumentoController(FileValidationService validationService, DocumentoService documentoService) {
        this.validationService = validationService;
        this.documentoService = documentoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocumento(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Arquivo vazio ou não fornecido");
        }

        String filename = file.getOriginalFilename();
        if (!validationService.isAllowedExtension(filename)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Extensão não permitida. Use: PDF, DOCS, OTD ou PNG");
        }

        try {
            String savedPath = documentoService.store(file);
            return ResponseEntity.ok("Arquivo salvo em: " + savedPath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar arquivo: " + e.getMessage());
        }
    }
}
