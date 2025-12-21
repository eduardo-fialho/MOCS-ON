package com.mocs_on.controller;

import java.io.IOException;
<<<<<<< HEAD
import java.security.Principal;
import java.time.LocalDateTime;
=======
>>>>>>> origin/main
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

<<<<<<< HEAD
import com.mocs_on.domain.Comite;
import com.mocs_on.domain.Documento;
import com.mocs_on.domain.StatusDocumento;
import com.mocs_on.domain.Usuario;
import com.mocs_on.service.DocumentoDAO;
import com.mocs_on.service.LoginDAO;
=======
import com.mocs_on.domain.Documento;
import com.mocs_on.domain.StatusDocumento;
import com.mocs_on.service.DocumentoDAO;
>>>>>>> origin/main

@RestController
@RequestMapping("/documentos")
@CrossOrigin(origins = "*")
public class DocumentoController {

    @Autowired
    private DocumentoDAO documentoDAO;

<<<<<<< HEAD
    @Autowired
    private LoginDAO loginDAO;

=======
>>>>>>> origin/main
    @GetMapping
    public ResponseEntity<List<Documento>> recuperarDocumentos() {
        List<Documento> documentos = documentoDAO.recuperarTodos();
        return ResponseEntity.ok(documentos);
    }

    @PostMapping
    public ResponseEntity<String> uploadDocumento(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nome") String nome,
<<<<<<< HEAD
            @RequestParam("comite") String comiteSigla,
            Principal principal,
            @RequestParam(value = "avaliacao", required = false) String avaliacao) {

        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário não autenticado");
        }

        var userOpt = loginDAO.findByEmail(principal.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário inválido");
        Usuario usuario = userOpt.get();

        if (usuario.getTipo() == null || !usuario.getTipo().name().equalsIgnoreCase("DELEGADO")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas delegados podem submeter documentos");
        }

        boolean inCommittee = false;
        if (usuario.getComites() != null) {
            for (Comite c : usuario.getComites()) {
                if (c != null && comiteSigla != null && comiteSigla.equalsIgnoreCase(c.getSigla())) { inCommittee = true; break; }
            }
        }
        if (!inCommittee) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pertence a esse comitê");
        }

        if (file == null || file.isEmpty()) {
=======
            @RequestParam("autor") String autor,
            @RequestParam("avaliacao") String avaliacao) {

        if (file.isEmpty()) {
>>>>>>> origin/main
            return ResponseEntity.badRequest().body("Arquivo vazio");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("Arquivo maior que 10MB");
        }

        String nomeArquivoReal = file.getOriginalFilename();
<<<<<<< HEAD
=======

>>>>>>> origin/main
        if (nomeArquivoReal == null) {
            return ResponseEntity.badRequest().body("Nome do arquivo inválido");
        }

<<<<<<< HEAD
        String extensao = "";
        int idx = nomeArquivoReal.lastIndexOf('.');
        if (idx >= 0) {
            extensao = nomeArquivoReal.substring(idx + 1).toLowerCase();
        }

        List<String> extensoesPermitidas = List.of("pdf", "docx", "odt", "png");
        if (!extensoesPermitidas.contains(extensao)) {
            return ResponseEntity.badRequest().body("Tipo de arquivo não permitido. Somente PDF, DOCX, ODT e PNG são aceitos.");
        }

        String contentType = file.getContentType();
        boolean mimeOk = false;
        if (extensao.equals("pdf") && "application/pdf".equalsIgnoreCase(contentType)) mimeOk = true;
        if (extensao.equals("docx") && ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(contentType) || "application/octet-stream".equalsIgnoreCase(contentType))) mimeOk = true;
        if (extensao.equals("odt") && ("application/vnd.oasis.opendocument.text".equalsIgnoreCase(contentType) || "application/octet-stream".equalsIgnoreCase(contentType))) mimeOk = true;
        if (extensao.equals("png") && ("image/png".equalsIgnoreCase(contentType))) mimeOk = true;

        if (!mimeOk) {
            return ResponseEntity.badRequest().body("Tipo MIME do arquivo inválido para a extensão fornecida.");
        }

        Documento doc = new Documento();
        doc.setNome(nomeArquivoReal);
        doc.setAutor(usuario.getEmail());
        doc.setStatus(StatusDocumento.RECEBIDO);
        doc.setAtivo(true);
        doc.setData(LocalDateTime.now());
=======
        String extensao = nomeArquivoReal.substring(nomeArquivoReal.lastIndexOf('.') + 1).toLowerCase();
        List<String> extensoesPermitidas = List.of("pdf");

        if (!extensoesPermitidas.contains(extensao)) {
            return ResponseEntity.badRequest()
                    .body("Tipo de arquivo não permitido. Somente PDF, DOCX e PNG são aceitos.");
        }

        Documento doc = new Documento();
        doc.setNome(nome);
        doc.setAutor(autor);
        doc.setStatus(StatusDocumento.RECEBIDO);
        doc.setAtivo(true);
>>>>>>> origin/main

        try {
            doc.setArquivo(file.getBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo");
        }

        doc.setAvaliacao(avaliacao);

        int linhas = documentoDAO.inserirDocumento(doc);
        if (linhas == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Documento enviado com sucesso");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar documento");
        }
<<<<<<< HEAD
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> baixarDocumento(@PathVariable Long id) {
        Documento doc = documentoDAO.recuperarPorId(id);

        if (doc == null || doc.getArquivo() == null) {
            return ResponseEntity.notFound().build();
        }

        String nomeArquivo = doc.getNome();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(doc.getArquivo());
    }

    @GetMapping("/{id}/visualizar")
    public ResponseEntity<byte[]> visualizar(@PathVariable Long id) {
        Documento doc = documentoDAO.recuperarPorId(id);

        if (doc == null || doc.getArquivo() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"" + doc.getNome() + "\"")
                .body(doc.getArquivo());
    }

    @PostMapping("/{id}/avaliar")
    @ResponseBody
    public Documento avaliarDocumento(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Documento doc = documentoDAO.recuperarPorId(id);
        if (doc == null) {
            throw new RuntimeException("Documento não encontrado");
        }
        String statusStr = (String) body.get("status");
        if (statusStr != null) {
            doc.setStatus(StatusDocumento.valueOf(statusStr.toUpperCase()));
        }
        doc.setAvaliacao((String) body.get("comments"));
        documentoDAO.atualizarDocumento(doc);
        return doc;
    }

}
=======
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> baixarDocumento(@PathVariable Long id) {
        Documento doc = documentoDAO.recuperarPorId(id);

        if (doc == null || doc.getArquivo() == null) {
            return ResponseEntity.notFound().build();
        }

        String nomeArquivo = doc.getNome();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(doc.getArquivo());
    }

    @GetMapping("/{id}/visualizar")
    public ResponseEntity<byte[]> visualizar(@PathVariable Long id) {
        Documento doc = documentoDAO.recuperarPorId(id);

        if (doc == null || doc.getArquivo() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"" + doc.getNome() + "\"")
                .body(doc.getArquivo());
    }

    @PostMapping("/{id}/avaliar")
    @ResponseBody
    public Documento avaliarDocumento(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Documento doc = documentoDAO.recuperarPorId(id);
        if (doc == null) {
            throw new RuntimeException("Documento não encontrado");
        }
        String statusStr = (String) body.get("status");
        doc.setStatus(StatusDocumento.valueOf(statusStr.toUpperCase()));
        doc.setAvaliacao((String) body.get("comments"));
        documentoDAO.atualizarDocumento(doc);
        return doc;
    }
}
>>>>>>> origin/main
