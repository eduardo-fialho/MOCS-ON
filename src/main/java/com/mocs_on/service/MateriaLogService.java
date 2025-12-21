package com.mocs_on.service;

import com.mocs_on.domain.AcaoMateriaLog;
import com.mocs_on.domain.MateriaLog;
import org.springframework.stereotype.Service;

@Service
public class MateriaLogService {

    private final MateriaLogDAO materiaLogDAO;

    public MateriaLogService(MateriaLogDAO materiaLogDAO) {
        this.materiaLogDAO = materiaLogDAO;
    }

    public void registrar(Long materiaId, AcaoMateriaLog acao, String usuario, String descricao) {

        MateriaLog log = new MateriaLog();
        log.setMateriaId(materiaId);
        log.setAcao(acao);
        log.setUsuario(usuario);
        log.setDescricao(descricao);

        materiaLogDAO.registrar(log);
    }
}
