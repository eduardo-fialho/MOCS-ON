package com.mocs_on.controller;

import com.mocs_on.auth.UserAccountService;
import com.mocs_on.domain.Post;
import com.mocs_on.domain.Post;
import com.mocs_on.service.PostDAO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserAccountService userAccountService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PostDAO postDAO;
    private static final long MAX_PHOTO_SIZE = 2 * 1024 * 1024; // 2MB (galeria/arquivos)
    // Para o BLOB no banco (max_allowed_packet ~1MB), mantemos um limite seguro.
    private static final int PROFILE_DB_MAX_BYTES = 900 * 1024; // 900KB
    private static final String PHOTO_PREFIX = "PHOTO|";
    private static final String SETTINGS_REDIRECT = "redirect:/profile/settings";

    /** Ajuste solicitado pelo usuario: armazenar as fotos publicadas no feed separado da foto de perfil. */
    @Value("${app.gallery.storage:db/gallery}")
    private String galleryStorageDir;

    public ProfileController(UserAccountService userAccountService,
                             BCryptPasswordEncoder passwordEncoder,
                             PostDAO postDAO) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
        this.postDAO = postDAO;
    }

    @GetMapping
    public String showProfile(Model model,
                              HttpSession session) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        UserAccountService.UserRecord user = userOpt.get();

        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/settings")
    public String showSettings(Model model,
                               HttpSession session) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        UserAccountService.UserRecord user = userOpt.get();

        ensureForms(model, user);

        List<UserAccountService.UserChangeLogRecord> changes = userAccountService.findChangeLogsByUserId(userId);
        int limit = Math.min(changes.size(), 10);
        model.addAttribute("changes", changes.subList(0, limit));
        model.addAttribute("user", user);
        return "profile_settings";
    }

    @PostMapping("/details")
    public String updateDetails(@ModelAttribute("profileForm") ProfileForm form,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        UserAccountService.UserRecord existing = userOpt.get();

        String name = form.getName() == null ? "" : form.getName().trim();
        String email = userAccountService.normalizeEmail(form.getEmail());

        if (name.isBlank()) {
            redirectAttributes.addFlashAttribute("profileError", "Informe o seu nome.");
            redirectAttributes.addFlashAttribute("profileForm", form);
            return SETTINGS_REDIRECT;
        }
        if (!userAccountService.isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("profileError", "Informe um e-mail válido.");
            redirectAttributes.addFlashAttribute("profileForm", form);
            return SETTINGS_REDIRECT;
        }

        try {
            userAccountService.updateUser(
                    userId,
                    name,
                    email,
                    existing.type(),
                    null,
                    existing.email()
            );
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("profileError", ex.getMessage());
            redirectAttributes.addFlashAttribute("profileForm", form);
            return SETTINGS_REDIRECT;
        }

        session.setAttribute(AuthController.SESSION_USER_NAME, name);
        session.setAttribute(AuthController.SESSION_USER_ATTRIBUTE, email);
        redirectAttributes.addFlashAttribute("profileSuccess", "Dados atualizados com sucesso.");
        return SETTINGS_REDIRECT;
    }

    @PostMapping("/password")
    public String updatePassword(@ModelAttribute("passwordForm") PasswordForm form,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        UserAccountService.UserRecord existing = userOpt.get();

        if (form.getCurrentPassword() == null || form.getCurrentPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("passwordError", "Informe a senha atual.");
            redirectAttributes.addFlashAttribute("passwordForm", form);
            return SETTINGS_REDIRECT;
        }
        if (!passwordEncoder.matches(form.getCurrentPassword(), existing.passwordHash())) {
            redirectAttributes.addFlashAttribute("passwordError", "Senha atual incorreta.");
            redirectAttributes.addFlashAttribute("passwordForm", form);
            return SETTINGS_REDIRECT;
        }
        if (form.getNewPassword() == null || form.getNewPassword().length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "A nova senha deve ter pelo menos 8 caracteres.");
            redirectAttributes.addFlashAttribute("passwordForm", form);
            return SETTINGS_REDIRECT;
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "As novas senhas precisam ser iguais.");
            redirectAttributes.addFlashAttribute("passwordForm", form);
            return SETTINGS_REDIRECT;
        }

        String hash = passwordEncoder.encode(form.getNewPassword());
        userAccountService.updateUser(
                userId,
                existing.name(),
                existing.email(),
                existing.type(),
                hash,
                existing.email()
        );
        redirectAttributes.addFlashAttribute("passwordSuccess", "Senha atualizada com sucesso.");
        return SETTINGS_REDIRECT;
    }

        @PostMapping("/photo/upload")
    public String uploadPhoto(@RequestParam("photoFile") MultipartFile photoFile,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (photoFile == null || photoFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("photoError", "Selecione uma imagem para enviar.");
            return SETTINGS_REDIRECT;
        }
        if (photoFile.getSize() > MAX_PHOTO_SIZE) {
            redirectAttributes.addFlashAttribute("photoError", "A foto deve ter no maximo 2MB.");
            return SETTINGS_REDIRECT;
        }
        try {
            byte[] bytes = photoFile.getBytes();
            byte[] prepared = prepareProfilePhoto(bytes);
            userAccountService.updateProfilePhoto(userId, prepared, MediaType.IMAGE_JPEG_VALUE);
            redirectAttributes.addFlashAttribute("photoSuccess", "Foto atualizada com sucesso.");
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("photoError", "Não foi possível processar a imagem enviada.");
        }
        return SETTINGS_REDIRECT;
    }

    @PostMapping("/gallery/upload")
    public String uploadGalleryPhoto(@RequestParam("photoFile") MultipartFile photoFile,
                                     @RequestParam(value = "caption", required = false) String caption,
                                     @RequestParam(value = "anonymous", defaultValue = "false") boolean anonymous,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (photoFile == null || photoFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("galleryError", "Escolha uma imagem para publicar na galeria.");
            return "redirect:/profile";
        }
        if (photoFile.getSize() > MAX_PHOTO_SIZE) {
            redirectAttributes.addFlashAttribute("galleryError", "A foto deve ter no maximo 2MB.");
            return "redirect:/profile";
        }
        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("galleryError", "Usuário não encontrado.");
            return "redirect:/profile";
        }
        try {
            byte[] bytes = photoFile.getBytes();
            String contentType = StringUtils.hasText(photoFile.getContentType())
                    ? photoFile.getContentType()
                    : MediaType.IMAGE_JPEG_VALUE;
            String filename = storeGalleryMedia(bytes, contentType);
            publishGalleryPost(userOpt.get(), filename, caption, anonymous);
            redirectAttributes.addFlashAttribute("gallerySuccess", "Foto publicada na galeria.");
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("galleryError", "Não foi possível publicar esta imagem.");
        }
        return "redirect:/profile";
    }

    @PostMapping("/gallery/capture")
    public String captureGalleryPhoto(@RequestParam("capturedImage") String capturedImage,
                                      @RequestParam(value = "caption", required = false) String caption,
                                      @RequestParam(value = "anonymous", defaultValue = "false") boolean anonymous,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (!StringUtils.hasText(capturedImage)) {
            redirectAttributes.addFlashAttribute("galleryError", "Nenhuma imagem capturada.");
            return "redirect:/profile";
        }
        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("galleryError", "Usuário não encontrado.");
            return "redirect:/profile";
        }
        String data = capturedImage.trim();
        String contentType = MediaType.IMAGE_PNG_VALUE;
        if (data.startsWith("data:")) {
            int semi = data.indexOf(';');
            int comma = data.indexOf(',');
            if (semi > 5) {
                contentType = data.substring(5, semi);
            }
            if (comma > semi) {
                data = data.substring(comma + 1);
            }
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            if (bytes.length > MAX_PHOTO_SIZE) {
                redirectAttributes.addFlashAttribute("galleryError", "A foto capturada ficou acima do limite de 2MB.");
                return "redirect:/profile";
            }
            String filename = storeGalleryMedia(bytes, contentType);
            publishGalleryPost(userOpt.get(), filename, caption, anonymous);
            redirectAttributes.addFlashAttribute("gallerySuccess", "Foto capturada e publicada na galeria.");
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("galleryError", "Não foi possível ler a imagem capturada.");
        }
        return "redirect:/profile";
    }

    @PostMapping("/photo/capture")
    public String capturePhoto(@RequestParam("capturedImage") String capturedImage,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (!StringUtils.hasText(capturedImage)) {
            redirectAttributes.addFlashAttribute("photoError", "Nenhuma imagem capturada.");
            return SETTINGS_REDIRECT;
        }
        String data = capturedImage.trim();
        if (data.startsWith("data:")) {
            int semi = data.indexOf(';');
            int comma = data.indexOf(',');
            if (semi > 5 && comma > semi) {
                data = data.substring(comma + 1);
            }
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            if (bytes.length > MAX_PHOTO_SIZE) {
                redirectAttributes.addFlashAttribute("photoError", "A foto capturada ficou muito grande. Tente novamente (limite 2MB antes da compressao).");
                return SETTINGS_REDIRECT;
            }
            byte[] prepared = prepareProfilePhoto(bytes);
            userAccountService.updateProfilePhoto(userId, prepared, MediaType.IMAGE_JPEG_VALUE);
            redirectAttributes.addFlashAttribute("photoSuccess", "Foto capturada com sucesso.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("photoError", "Não foi possível ler a imagem capturada.");
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("photoError", "Não foi possível processar a imagem capturada.");
        }
        return SETTINGS_REDIRECT;
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> showPhoto(@RequestParam(value = "userId", required = false) Long requestedUserId,
                                            HttpSession session) {
        Long userId = requestedUserId != null ? requestedUserId : currentUserId(session);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        Optional<UserAccountService.UserPhoto> photoOpt = userAccountService.findProfilePhoto(userId);
        if (photoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(photoOpt.get().contentType())) {
            try {
                mediaType = MediaType.parseMediaType(photoOpt.get().contentType());
            } catch (Exception ignored) {
            }
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(mediaType)
                .body(photoOpt.get().data());
    }

    @GetMapping("/gallery/media/{filename:.+}")
    public ResponseEntity<Resource> serveGalleryMedia(@PathVariable String filename) {
        try {
            Path root = galleryRoot();
            Path requested = root.resolve(filename).normalize();
            if (!requested.startsWith(root) || !Files.exists(requested)) {
                return ResponseEntity.notFound().build();
            }
            MediaType mediaType = resolveMediaType(filename);
            byte[] data = Files.readAllBytes(requested);
            Resource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(mediaType)
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/gallery/{postId}/delete")
    public ResponseEntity<Void> deleteGalleryPost(@PathVariable Long postId, HttpSession session) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Optional<Post> postOpt = postDAO.findById(postId);
        if (postOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserAccountService.UserRecord user = userOpt.get();
        Post post = postOpt.get();

        boolean canModerate = user.type() != null && user.type().equalsIgnoreCase("SECRETARIADO");
        boolean isOwner = post.getAutorRaw() != null && user.name() != null
                && user.name().equalsIgnoreCase(post.getAutorRaw());
        if (!canModerate && !isOwner) {
            return ResponseEntity.status(403).build();
        }

        String filename = extractFilenameFromMessage(post.getMensagem());
        try {
            if (filename != null) {
                Path root = galleryRoot();
                Path target = root.resolve(filename).normalize();
                if (target.startsWith(root)) {
                    Files.deleteIfExists(target);
                }
            }
        } catch (IOException ignored) {
        }

        postDAO.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object idAttr = session.getAttribute(AuthController.SESSION_USER_ID);
        if (idAttr instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Path galleryRoot() throws IOException {
        Path dir = Paths.get(galleryStorageDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        return dir;
    }

    private String storeGalleryMedia(byte[] data, String contentType) throws IOException {
        Path root = galleryRoot();
        String extension = ".png";
        if (contentType != null) {
            if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                extension = ".jpg";
            } else if (contentType.contains("webp")) {
                extension = ".webp";
            }
        }
        String filename = "gallery_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;
        Path target = root.resolve(filename);
        Files.write(target, data, StandardOpenOption.CREATE_NEW);
        return filename;
    }

    private void publishGalleryPost(UserAccountService.UserRecord user,
                                    String filename,
                                    String caption,
                                    boolean anonymous) {
        Post post = new Post();
        post.setAutor(user.name());
        post.setStatus(anonymous ? Post.TipoPost.ANONIMO : Post.TipoPost.PUBLICO);
        post.setMensagem(PHOTO_PREFIX + filename + "|" + sanitizeCaption(caption));
        post.setData(LocalDateTime.now());
        postDAO.inserirPost(post);
    }

    private String sanitizeCaption(String caption) {
        if (caption == null) {
            return "";
        }
        String trimmed = caption.trim();
        if (trimmed.length() > 280) {
            trimmed = trimmed.substring(0, 280);
        }
        return trimmed.replace("|", "/");
    }

    private MediaType resolveMediaType(String filename) {
        String lower = filename == null ? "" : filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.valueOf("image/webp");
        }
        return MediaType.IMAGE_PNG;
    }

    private String extractFilenameFromMessage(String mensagem) {
        if (mensagem == null || !mensagem.startsWith(PHOTO_PREFIX)) {
            return null;
        }
        String remainder = mensagem.substring(PHOTO_PREFIX.length());
        String[] parts = remainder.split("\\|", 2);
        if (parts.length > 0 && StringUtils.hasText(parts[0])) {
            return parts[0].trim();
        }
        return null;
    }

    private byte[] prepareProfilePhoto(byte[] input) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(input));
        if (original == null) {
            throw new IOException("Imagem inválida.");
        }
        int maxDimension = 900;
        int ow = original.getWidth();
        int oh = original.getHeight();
        double scale = 1.0;
        if (ow > maxDimension || oh > maxDimension) {
            scale = Math.min((double) maxDimension / ow, (double) maxDimension / oh);
        }
        int nw = Math.max(1, (int) Math.round(ow * scale));
        int nh = Math.max(1, (int) Math.round(oh * scale));

        BufferedImage resized = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, nw, nh);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(original, 0, 0, nw, nh, null);
        g2d.dispose();

        float quality = 0.85f;
        byte[] result = compressJpeg(resized, quality);
        while (result.length > PROFILE_DB_MAX_BYTES && quality > 0.35f) {
            quality -= 0.1f;
            result = compressJpeg(resized, quality);
        }

        if (result.length > PROFILE_DB_MAX_BYTES) {
            double factor = Math.sqrt((double) PROFILE_DB_MAX_BYTES / result.length);
            int sw = Math.max(1, (int) Math.round(nw * factor));
            int sh = Math.max(1, (int) Math.round(nh * factor));
            BufferedImage smaller = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_RGB);
            Graphics2D gSmall = smaller.createGraphics();
            gSmall.setColor(Color.WHITE);
            gSmall.fillRect(0, 0, sw, sh);
            gSmall.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gSmall.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            gSmall.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gSmall.drawImage(resized, 0, 0, sw, sh, null);
            gSmall.dispose();
            result = compressJpeg(smaller, 0.75f);
        }

        if (result.length > PROFILE_DB_MAX_BYTES) {
            throw new IOException("Imagem muito grande mesmo após compressão.");
        }
        return result;
    }

    private byte[] compressJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(Math.max(0.1f, Math.min(1f, quality)));
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), param);
        ios.close();
        writer.dispose();
        return baos.toByteArray();
    }

    private void ensureForms(Model model, UserAccountService.UserRecord user) {
        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setName(user.name());
            form.setEmail(user.email());
            model.addAttribute("profileForm", form);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordForm());
        }
    }

    public static class ProfileForm {
        private String name;
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class PasswordForm {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
























