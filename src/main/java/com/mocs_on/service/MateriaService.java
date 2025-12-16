package com.mocs_on.service;

import com.mocs_on.domain.*;
import com.mocs_on.service.MateriaDAO;
import com.mocs_on.service.MateriaLogDAO;

import org.springframework.stereotype.Service;

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

    public void criar(Materia materia, String usuario) {
        materiaDAO.salvar(materia);
        registrarLog(materia.getId(), AcaoMateriaLog.CRIACAO, usuario, "Matéria criada");
    }

    public void atualizar(Materia materia, String usuario) {
        materiaDAO.atualizar(materia);
        registrarLog(materia.getId(), AcaoMateriaLog.EDICAO, usuario, "Matéria editada");
    }

    public void aprovar(Long id, String usuario) {
        materiaDAO.atualizarStatus(id, StatusMateria.APROVADA);
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
