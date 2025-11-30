package com.mocs_on.service;

import com.mocs_on.domain.Comite;
import com.mocs_on.domain.Login;
import com.mocs_on.domain.Usuario;
import com.mocs_on.security.CargoEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class LoginDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDAO.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Usuario autenticar(Login login) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try {
            Usuario usuario = jdbcTemplate.queryForObject(
                    sql,
                    (ResultSet rs, int rowNum) -> {
                        Usuario u = new Usuario();
                        u.setId(rs.getLong("id"));
                        u.setNome(rs.getString("nome"));
                        u.setEmail(rs.getString("email"));
                        u.setSenha(rs.getString("senha"));
                        return u;
                    },
                    login.getEmail());

            if (usuario != null && BCrypt.checkpw(login.getSenha(), usuario.getSenha())) {
                return usuario;
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao autenticar usuario", e);
        }
        return null;
    }

    public void salvarUsuario(Usuario usuario, String tipo) {
        if (emailExiste(usuario.getEmail())) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }
        String hash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
        String sql = "INSERT INTO usuarios (nome, email, senha, tipo) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, usuario.getNome(), usuario.getEmail(), hash, tipo);
    }

    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> rs.getInt(1),
                email);
        return count != null && count > 0;
    }

    public Optional<Usuario> findByEmail(String email) {
        final String SQL = "SELECT id, nome, email, senha, tipo FROM usuarios WHERE email = ?";

        List<Usuario> results = jdbcTemplate.query(
            SQL,
            this::mapRowToUsuario,
            email
        );

        Usuario usuario = DataAccessUtils.singleResult(results);

        if (usuario != null) {
            usuario.setComites(findComitesByUsuarioId(usuario.getId()));
        }

        return Optional.ofNullable(usuario);
    }

    private Usuario mapRowToUsuario(ResultSet rs, int rowNum) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));
        String rawTipo = rs.getString("tipo");
        if (rawTipo != null) {
            try {
                usuario.setTipo(rawTipo);
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Tipo '{}' invalido para usuario {}. Aplicando VISITANTE.", rawTipo, usuario.getEmail());
                usuario.setTipo(CargoEnum.VISITANTE.name());
            }
        } else {
            usuario.setTipo(CargoEnum.VISITANTE.name());
        }
        return usuario;
    }

    private List<Comite> findComitesByUsuarioId(Long usuarioId) {
        return List.of();
    }
}
