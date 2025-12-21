package com.mocs_on.controller;

import com.mocs_on.auth.UserAccountService;
import com.mocs_on.domain.Comite;
import com.mocs_on.domain.Delegacao;
import com.mocs_on.domain.Usuario;
import com.mocs_on.dto.DelegadoDelegacaoDTO;
import com.mocs_on.dto.DelegacaoNotificacaoDTO;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.HDataSource;
import com.mocs_on.service.AlunoDao;
import com.mocs_on.service.ComiteDao;
import com.mocs_on.service.ComiteUsuarioDao;
import com.mocs_on.service.DelegacaoDao;
import com.mocs_on.service.DelegacaoNotificacaoDao;
import com.mocs_on.service.DelegacaoUsuarioDao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.SQLException;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("/comite")
public class ComiteController {

        
    @Autowired
	private HDataSource ds;

    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/criar")
    public String criarComite(Model model, HttpSession session) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof SecaoUsuario) {
            SecaoUsuario secaoUsuario = (SecaoUsuario) principal;
            String cargoUsuario = secaoUsuario.getCargo().name();
        
            if (cargoUsuario.equals("VISITANTE") || cargoUsuario.equals("IMPRENSA") || cargoUsuario.equals("APOIADOR") || cargoUsuario.equals("EQUIPE")) {
                throw new Exception("Acesso negado");
            }

            //else {
            //    throw new Exception("Usuário não autenticado ou tipo inválido.");
            //}
        }

        return "criar_comite";
    }

    @PostMapping("/salvar")
    public String salvarComite(@ModelAttribute Comite comite,
                               @RequestParam(name = "delegacoes", required = false) List<String> delegacoes)
            throws Exception {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            ComiteDao.insert(conn, comite);
            DelegacaoDao.insertAll(conn, comite.getId(), delegacoes);
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
            throw new Exception("Erro ao salvar comite: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }

        return "redirect:/comite/listar";
    }

    @RequestMapping("/listar")
    public String listarComites(Model model, HttpSession session) throws Exception {
        try (Connection conn = ds.getConnection()) {
			List<Comite> comites = ComiteDao.listComites(conn);
			model.addAttribute("comites", comites);
		}

        catch (Exception e) {
            throw new Exception("Erro ao listar comitês: " + e.getMessage());
        }

        return "ver_comites";
    }

    @RequestMapping("/editar/{id}")
    public String editarComite(@PathVariable("id") Long idComite, Model model, HttpSession session) throws Exception, SQLException {
        try (Connection conn = ds.getConnection()) {
			Comite comite = ComiteDao.get(conn, idComite);

            model.addAttribute("comite", comite);
            List<Delegacao> delegacoes = DelegacaoDao.listByComite(conn, idComite);
            List<DelegadoDelegacaoDTO> delegados = DelegacaoUsuarioDao.listarDelegadosPorComite(conn, idComite);
            model.addAttribute("delegacoes", delegacoes);
            model.addAttribute("delegados", delegados);

            List<UserAccountService.UserRecord> usuarios = userAccountService.findAllUsers();
            Set<Long> usuariosNoComite = new HashSet<>();
            for (DelegadoDelegacaoDTO delegado : delegados) {
                usuariosNoComite.add(delegado.getUsuarioId());
            }
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("usuariosNoComite", usuariosNoComite);
		}

        catch (SQLException e) {
            throw new SQLException("Sql Exception: " + e.getMessage());
        }

        catch (Exception e) {
            throw new Exception("Erro ao listar comitês: " + e.getMessage());
        }

        return "editar_comite";
    }

    @RequestMapping("/detalhes/{id}")
    public String detalhesComite(@PathVariable("id") Long idComite, Model model, HttpSession session) throws Exception, SQLException {
        try (Connection conn = ds.getConnection()) {
            Comite comite = ComiteDao.get(conn, idComite);
            List<Delegacao> delegacoes = DelegacaoDao.listByComite(conn, idComite);
            List<DelegadoDelegacaoDTO> delegados = DelegacaoUsuarioDao.listarDelegadosPorComite(conn, idComite);

            Map<Long, List<DelegadoDelegacaoDTO>> delegadosPorDelegacao = new LinkedHashMap<>();
            for (Delegacao delegacao : delegacoes) {
                delegadosPorDelegacao.put(delegacao.getId(), new ArrayList<>());
            }
            List<DelegadoDelegacaoDTO> delegadosSemDelegacao = new ArrayList<>();
            for (DelegadoDelegacaoDTO delegado : delegados) {
                Long delegacaoId = delegado.getDelegacaoId();
                if (delegacaoId == null) {
                    delegadosSemDelegacao.add(delegado);
                    continue;
                }
                List<DelegadoDelegacaoDTO> lista = delegadosPorDelegacao.get(delegacaoId);
                if (lista != null) {
                    lista.add(delegado);
                } else {
                    delegadosSemDelegacao.add(delegado);
                }
            }

            model.addAttribute("comite", comite);
            model.addAttribute("delegacoes", delegacoes);
            model.addAttribute("delegadosPorDelegacao", delegadosPorDelegacao);
            model.addAttribute("delegadosSemDelegacao", delegadosSemDelegacao);
        }

        return "comite_detalhes";
    }

    @PostMapping("/editar")
    public String salvarEditarComite(@ModelAttribute Comite comite) throws Exception, SQLException {
        try (Connection conn = ds.getConnection()) {
            ComiteDao.update(conn, comite);
            conn.commit();
		}

        catch (SQLException e) {
            throw new SQLException("Sql Exception: " + e.getMessage());
        }

        catch (Exception e) {
            throw new Exception("Erro: " + e.getMessage());
        }

        return "redirect:/comite/listar";
    }

    @PostMapping("/editar-delegacoes")
    public String salvarDelegacoes(@RequestParam("comiteId") Long comiteId,
                                   @RequestParam(name = "delegacoes", required = false) List<String> novasDelegacoes,
                                   @RequestParam(name = "removerDelegacaoIds", required = false) List<Long> removerDelegacaoIds,
                                   @RequestParam(name = "delegacaoEditId", required = false) List<Long> delegacaoEditIds,
                                   @RequestParam(name = "delegacaoEditNome", required = false) List<String> delegacaoEditNomes,
                                   @RequestParam(name = "membroUsuarioId", required = false) List<Long> membroUsuarioIds,
                                   @RequestParam(name = "usuarioId", required = false) List<Long> usuarioIds,
                                   @RequestParam(name = "delegacaoId", required = false) List<String> delegacaoIds)
            throws Exception {
        Connection conn = null;
        try {
            conn = ds.getConnection();

            Comite comite = ComiteDao.get(conn, comiteId);
            List<Delegacao> delegacoes = DelegacaoDao.listByComite(conn, comiteId);
            Map<Long, String> delegacaoNomePorId = new LinkedHashMap<>();
            for (Delegacao delegacao : delegacoes) {
                delegacaoNomePorId.put(delegacao.getId(), delegacao.getNome());
            }

            List<DelegadoDelegacaoDTO> delegados = DelegacaoUsuarioDao.listarDelegadosPorComite(conn, comiteId);
            Map<Long, Long> anterior = new LinkedHashMap<>();
            for (DelegadoDelegacaoDTO delegado : delegados) {
                anterior.put(delegado.getUsuarioId(), delegado.getDelegacaoId());
            }

            Set<Long> removidas = new HashSet<>();
            if (removerDelegacaoIds != null) {
                removidas.addAll(removerDelegacaoIds);
            }

            Map<Long, String> edits = parseDelegacaoEdits(delegacaoEditIds, delegacaoEditNomes, removidas);
            DelegacaoDao.updateNames(conn, comiteId, edits);
            for (Map.Entry<Long, String> entry : edits.entrySet()) {
                delegacaoNomePorId.put(entry.getKey(), entry.getValue());
            }

            DelegacaoDao.deleteByIds(conn, comiteId, removerDelegacaoIds);
            DelegacaoDao.insertAll(conn, comiteId, novasDelegacoes);

            List<Long> membrosSelecionados = normalizeIds(membroUsuarioIds);
            ComiteUsuarioDao.substituirUsuariosNoComite(conn, comiteId, membrosSelecionados);
            Set<Long> membrosSet = new HashSet<>(membrosSelecionados);

            Map<Long, Long> atual = parseAssignments(usuarioIds, delegacaoIds, removerDelegacaoIds, membrosSet);
            DelegacaoUsuarioDao.upsertDelegacoes(conn, comiteId, atual);

            List<DelegacaoNotificacaoDTO> notificacoes = buildNotificacoes(
                    comite.getNome(),
                    comiteId,
                    anterior,
                    atual,
                    delegacaoNomePorId
            );
            DelegacaoNotificacaoDao.insertAll(conn, notificacoes);

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
            throw new Exception("Erro ao salvar delegacoes: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }

        return "redirect:/comite/editar/" + comiteId;
    }

    private Map<Long, Long> parseAssignments(List<Long> usuarioIds,
                                             List<String> delegacaoIds,
                                             List<Long> removerDelegacaoIds,
                                             Set<Long> membrosSet) {
        Map<Long, Long> assignments = new LinkedHashMap<>();
        if (usuarioIds == null || delegacaoIds == null) {
            return assignments;
        }
        int limit = Math.min(usuarioIds.size(), delegacaoIds.size());
        Set<Long> removidas = new HashSet<>();
        if (removerDelegacaoIds != null) {
            removidas.addAll(removerDelegacaoIds);
        }
        for (int i = 0; i < limit; i++) {
            Long usuarioId = usuarioIds.get(i);
            Long delegacaoId = parseDelegacaoId(delegacaoIds.get(i));
            if (delegacaoId != null && removidas.contains(delegacaoId)) {
                delegacaoId = null;
            }
            if (usuarioId != null && (membrosSet == null || membrosSet.contains(usuarioId))) {
                assignments.put(usuarioId, delegacaoId);
            }
        }
        return assignments;
    }

    private Map<Long, String> parseDelegacaoEdits(List<Long> ids,
                                                  List<String> nomes,
                                                  Set<Long> removidas) {
        Map<Long, String> edits = new LinkedHashMap<>();
        if (ids == null || nomes == null) {
            return edits;
        }
        int limit = Math.min(ids.size(), nomes.size());
        for (int i = 0; i < limit; i++) {
            Long id = ids.get(i);
            String nome = nomes.get(i);
            if (id == null || nome == null) {
                continue;
            }
            if (removidas != null && removidas.contains(id)) {
                continue;
            }
            String trimmed = nome.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            edits.put(id, trimmed);
        }
        return edits;
    }

    private Long parseDelegacaoId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null && id > 0) {
                unique.add(id);
            }
        }
        return new ArrayList<>(unique);
    }

    private List<DelegacaoNotificacaoDTO> buildNotificacoes(String comiteNome,
                                                            long comiteId,
                                                            Map<Long, Long> anterior,
                                                            Map<Long, Long> atual,
                                                            Map<Long, String> delegacaoNomePorId) {
        List<DelegacaoNotificacaoDTO> notificacoes = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : atual.entrySet()) {
            Long usuarioId = entry.getKey();
            Long novaDelegacao = entry.getValue();
            Long antigaDelegacao = anterior.get(usuarioId);
            if (antigaDelegacao == null && novaDelegacao == null) {
                continue;
            }
            if (antigaDelegacao != null && antigaDelegacao.equals(novaDelegacao)) {
                continue;
            }
            String antigaNome = antigaDelegacao == null ? null : delegacaoNomePorId.get(antigaDelegacao);
            String novaNome = novaDelegacao == null ? null : delegacaoNomePorId.get(novaDelegacao);
            String mensagem = buildMensagemNotificacao(comiteNome, antigaNome, novaNome);
            if (mensagem != null) {
                notificacoes.add(new DelegacaoNotificacaoDTO(usuarioId, comiteId, mensagem));
            }
        }
        for (Map.Entry<Long, Long> entry : anterior.entrySet()) {
            Long usuarioId = entry.getKey();
            if (atual.containsKey(usuarioId)) {
                continue;
            }
            Long delegacaoAnterior = entry.getValue();
            if (delegacaoAnterior == null) {
                continue;
            }
            String antigaNome = delegacaoNomePorId.get(delegacaoAnterior);
            String mensagem = buildMensagemNotificacao(comiteNome, antigaNome, null);
            if (mensagem != null) {
                notificacoes.add(new DelegacaoNotificacaoDTO(usuarioId, comiteId, mensagem));
            }
        }
        return notificacoes;
    }

    private String buildMensagemNotificacao(String comiteNome, String antigaNome, String novaNome) {
        if (antigaNome != null && novaNome == null) {
            return String.format("Voce foi removido da delegacao %s no comite %s.", antigaNome, comiteNome);
        }
        if (antigaNome == null && novaNome != null) {
            return String.format("Voce foi alocado na delegacao %s no comite %s.", novaNome, comiteNome);
        }
        if (antigaNome != null && novaNome != null) {
            return String.format("Voce foi transferido da delegacao %s para %s no comite %s.", antigaNome, novaNome, comiteNome);
        }
        return null;
    }

    @RequestMapping("/editar/deletar-comite/{id}")
    public String deletarComite(@PathVariable("id") Long comiteId) throws Exception, SQLException {
        try (Connection conn = ds.getConnection()) {
            Comite comite = ComiteDao.get(conn, comiteId);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof SecaoUsuario) {
                SecaoUsuario secaoUsuario = (SecaoUsuario) principal;
                String cargoUsuario = secaoUsuario.getCargo().name();
            
                if (cargoUsuario.equals("VISITANTE") || cargoUsuario.equals("IMPRENSA") || cargoUsuario.equals("APOIADOR") || cargoUsuario.equals("EQUIPE")) {
                    throw new Exception("Acesso negado");
                }
            }            

            ComiteDao.delete(conn, comiteId);
            conn.commit();
		}

        catch (SQLException e) {
            throw new SQLException("Sql Exception: " + e.getMessage());
        }

        catch (Exception e) {
            throw new Exception("Erro: " + e.getMessage());
        }

        return "redirect:/comite/listar";
    }
}
