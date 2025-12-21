package com.mocs_on.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import com.mocs_on.domain.Consulta;
import com.mocs_on.domain.StatusConsulta;
import com.mocs_on.domain.TipoVoto;

@Service
public class ConsultaService {

    private final ConsultaDAO consultaDAO;
    private final ConsultaVotoDAO consultaVotoDAO;

    public ConsultaService(
            ConsultaDAO consultaDAO,
            ConsultaVotoDAO consultaVotoDAO
    ) {
        this.consultaDAO = consultaDAO;
        this.consultaVotoDAO = consultaVotoDAO;
    }

    public List<Consulta> listarTodas() {
        return consultaDAO.listarTodas();
    }


    public List<Consulta> listarAtivas() {
        return consultaDAO.listarAtivas();
    }

    public List<Consulta> listarPorStatus(StatusConsulta status) {
        return consultaDAO.listarPorStatus(status);
    }

    public Consulta buscarPorId(Long id) {
        return consultaDAO.buscarPorId(id);
    }

    @Transactional
    public void criar(Consulta consulta) {
        consulta.setStatus(StatusConsulta.PENDENTE);
        consulta.setAtivo(true);
        consultaDAO.salvar(consulta);
    }

    public void aprovar(Long id) {
        Consulta consulta = consultaDAO.buscarPorId(id);

        if (consulta.getStatus() != StatusConsulta.PENDENTE) {
            throw new IllegalStateException("Consulta não está pendente");
        }

        consultaDAO.atualizarStatus(id, StatusConsulta.APROVADA);
    }

    public void rejeitar(Long id) {
        Consulta consulta = consultaDAO.buscarPorId(id);

        if (consulta.getStatus() != StatusConsulta.PENDENTE) {
            throw new IllegalStateException("Consulta não está pendente");
        }

        consultaDAO.atualizarStatus(id, StatusConsulta.REJEITADA);
    }

    public void arquivar(Long id) {
        Consulta consulta = consultaDAO.buscarPorId(id);

        if (!consulta.isAtivo()) {
            throw new IllegalStateException("Consulta já está arquivada");
        }

        consultaDAO.atualizarStatus(id, StatusConsulta.ARQUIVADA);
        consultaDAO.atualizarAtivo(id, false);
    }

    @Transactional
    public void votar(Long consultaId, String usuarioUsername, TipoVoto voto) {

        Consulta consulta = consultaDAO.buscarPorId(consultaId);

        if (consulta.getStatus() != StatusConsulta.APROVADA) {
            throw new IllegalStateException("Consulta não está aprovada");
        }

        if (!consulta.isAtivo()) {
            throw new IllegalStateException("Consulta arquivada");
        }

        if (consultaVotoDAO.existeVoto(consultaId, usuarioUsername)) {
            consultaVotoDAO.atualizarVoto(consultaId, usuarioUsername, voto);
        } else {
            consultaVotoDAO.registrarVoto(consultaId, usuarioUsername, voto);
        }
    }

    public Map<String, Integer> contarVotosTotais(Long consultaId) {
        int favor = consultaVotoDAO.contarVotos(consultaId, TipoVoto.SIM);
        int contra = consultaVotoDAO.contarVotos(consultaId, TipoVoto.NAO);

        Map<String, Integer> resultado = new HashMap<>();
        resultado.put("favor", favor);
        resultado.put("contra", contra);

        return resultado;
    }
}
