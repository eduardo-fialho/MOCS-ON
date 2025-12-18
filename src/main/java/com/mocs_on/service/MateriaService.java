package com.mocs_on.service;

import com.mocs_on.domain.*;
import com.mocs_on.service.MateriaDAO;
import com.mocs_on.service.MateriaLogDAO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MateriaService {

    private final MateriaDAO materiaDAO;
    private final MateriaLogDAO materiaLogDAO;

    public MateriaService(MateriaDAO materiaDAO, MateriaLogDAO materiaLogDAO) {
        this.materiaDAO = materiaDAO;
        this.materiaLogDAO = materiaLogDAO;
    }

    public List<Materia> listarTodas() {
        return materiaDAO.listar();
    }

    public Materia buscarPorId(Long id) {
        return materiaDAO.buscarPorId(id);
    }

    @Transactional
    public void criar(Materia materia, String usuario) {

        Long materiaId = materiaDAO.salvar(materia);

        MateriaLog log = new MateriaLog();
        log.setMateriaId(materiaId);
        log.setAcao(AcaoMateriaLog.CRIACAO);
        log.setUsuario(usuario);
        log.setDescricao("Matéria criada");

        materiaLogDAO.registrar(log);
    }


    public void atualizar(Materia materiaEditada, String usuario) {
        Materia materia = materiaDAO.buscarPorId(materiaEditada.getId());

        materia.setTitulo(materiaEditada.getTitulo());
        materia.setLead(materiaEditada.getLead());
        materia.setTexto(materiaEditada.getTexto());

        if (materiaEditada.getImagem() != null) {
            materia.setImagem(materiaEditada.getImagem());
        }

        materiaDAO.atualizarStatus(materia.getId(), StatusMateria.PENDENTE);
        materiaDAO.atualizar(materia);

        registrarLog(
            materia.getId(),
            AcaoMateriaLog.EDICAO,
            usuario,
            "Matéria editada – voltou para PENDENTE"
        );
    }


    public void aprovar(Long id, String usuario) {
        materiaDAO.atualizarStatus(id, StatusMateria.APROVADA);
        materiaDAO.atualizarRevisor(id, usuario);
        registrarLog(id, AcaoMateriaLog.APROVACAO, usuario, "Matéria aprovada");
    }

    public void rejeitar(Long id, String motivo, String usuario) {
        materiaDAO.atualizarStatus(id, StatusMateria.REJEITADA);
        registrarLog(id, AcaoMateriaLog.REJEICAO, usuario, motivo);
    }

    private void registrarLog(Long materiaId, AcaoMateriaLog acao, String usuario, String descricao) {
        MateriaLog log = new MateriaLog();
        log.setMateriaId(materiaId);
        log.setAcao(acao);
        log.setUsuario(usuario);
        log.setDescricao(descricao);
        materiaLogDAO.registrar(log);
    }
}
