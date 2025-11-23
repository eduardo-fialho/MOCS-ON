package com.mocs_on.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mocs_on.domain.Documento;
import com.mocs_on.domain.StatusDocumento;
import com.mocs_on.service.DocumentoDAO;

@RestController
@RequestMapping("/documentos")
@CrossOrigin(origins = "*")
public class DocumentoController {

    @Autowired
    private DocumentoDAO documentoDAO;

    @GetMapping
    public ResponseEntity<List<Documento>> recuperarDocumentos() {
        List<Documento> documentos = documentoDAO.recuperarTodos();
        return ResponseEntity.ok(documentos);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocumento(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nome") String nome,
            @RequestParam("autor") String autor,
            @RequestParam("status") String status) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo vazio");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body("Arquivo maior que 10MB");
        }

        Documento doc = new Documento();
        doc.setNome(nome);
        doc.setAutor(autor);
        doc.setStatus(StatusDocumento.valueOf(status));
        doc.setAtivo(true);
        try {
            doc.setArquivo(file.getBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo");
        }

        int linhas = documentoDAO.inserirDocumento(doc);
        if (linhas == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Documento enviado com sucesso"); 
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar documento");
        }
    }

    @GetMapping("/quantidade")
    public ResponseEntity<Integer> quantidadeDocumentos() {
        return ResponseEntity.ok(documentoDAO.quantidadeDocumentos());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> baixarDocumento(@PathVariable Long id) {
        Documento doc = documentoDAO.recuperarPorId(id);

        if (doc == null || doc.getArquivo() == null) {
            return ResponseEntity.notFound().build();
        }

        String nomeArquivo = doc.getNome().replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".pdf";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(doc.getArquivo());
    }
}
