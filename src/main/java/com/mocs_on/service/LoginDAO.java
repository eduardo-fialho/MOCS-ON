package com.mocs_on.service;

import com.mocs_on.domain.Login;
import com.mocs_on.domain.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;

@Repository
public class LoginDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Usuario autenticar(Login login) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try {
            Usuario usuario = jdbcTemplate.queryForObject(
                    sql,
                    (ResultSet rs, int rowNum) -> {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
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
            throw new RuntimeException("Erro ao autenticar usuário", e);
        }
        return null;
    }

    public void salvarUsuario(Usuario usuario, String tipo) {
        if (emailExiste(usuario.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
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
}
