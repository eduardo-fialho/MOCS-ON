package com.mocs_on.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.Comite;
import com.mocs_on.domain.Comite.StatusComite;
import com.mocs_on.dto.InformacoesComiteDTO;

@Repository
public class ComiteDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final static String getsql = "SELECT * FROM comites  WHERE id = ?";
    private final static String listsql = "SELECT * FROM comites";
    private final static String insertsql = "INSERT INTO comites (sigla, nome, status, num_delegados, descricao) VALUES( ?, ?, ?, ?, ?) ";
    private final static String updatesql = "UPDATE comites SET sigla = ?, nome = ?, status = ?, num_delegados = ?, descricao = ? WHERE id = ? ";
    private final static String deletesql = "DELETE FROM comites WHERE id = ?";

    private static void closeResource(Statement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            ps = null;
        }
    }

    private static void closeResource(Statement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            rs = null;
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            ps = null;
        }
    }

    static Comite set(ResultSet rs) throws SQLException {
        Comite vo = new Comite();
        vo.setId(rs.getLong("id"));
        vo.setNome(rs.getString("nome"));
        vo.setDescricao(rs.getString("descricao"));
        vo.setNumeroDelegados(rs.getInt("num_delegados"));
        vo.setSigla(rs.getString("sigla"));
        vo.setStatus(StatusComite.valueOf(rs.getString("status")));

        return vo;
    }

    public static ArrayList<Comite> listComites(Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(listsql);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return new ArrayList<Comite>();
            }
            ArrayList<Comite> list = new ArrayList<Comite>();
            do {
                String nome = rs.getString("nome");
                String descricao = rs.getString("descricao");
                String sigla = rs.getString("sigla");
                int numeroDelegados = rs.getInt("num_delegados");
                StatusComite status = StatusComite.valueOf(rs.getString("status"));
                long id = rs.getLong("id");

                Comite comite = new Comite(id, sigla, nome, status, numeroDelegados, descricao);

                list.add(comite);

            } while (rs.next());

            return list;
        } catch (SQLException e) {
            throw e;
        } finally {
            closeResource(ps, rs);
            ps = null;
            rs = null;
        }
    }

    public static Comite get(Connection conn, long id) throws Exception, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(getsql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("Object not found [" + id + "]");
            }
            Comite b = set(rs);
            return b;
        } catch (SQLException e) {
            throw e;
        } finally {
            closeResource(ps, rs);
            ps = null;
            rs = null;
        }
    }

    public static void insert(Connection conn, Comite vo) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(insertsql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, vo.getSigla());
            ps.setString(2, vo.getNome());
            ps.setString(3, vo.getStatus().name());
            ps.setInt(4, vo.getNumeroDelegados());
            ps.setString(5, vo.getDescricao());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                vo.setId(id);
                System.out.println("Chave gerada: " + id);
            } else {
                throw new SQLException(
                        "Nao foi possivel recuperar a CHAVE gerada na criacao do registro no banco de dados");
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
            }
            ;
            throw e;
        } finally {
            closeResource(ps, rs);
            ps = null;
            rs = null;
        }
    }

    public static void update(Connection conn, Comite vo)
            throws Exception, SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updatesql);
            ps.setString(1, vo.getSigla());
            ps.setString(2, vo.getNome());
            ps.setString(3, vo.getStatus().name());
            ps.setInt(4, vo.getNumeroDelegados());
            ps.setString(5, vo.getDescricao());
            ps.setLong(6, vo.getId());
            int count = ps.executeUpdate();
            if (count == 0) {
                throw new Exception("Object not found [" + vo.getId() + "] .");
            }
            // SEM COMMIT
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
            }
            ;
            throw e;
        } finally {
            closeResource(ps);
            ps = null;
        }
    }

    public static void delete(Connection conn, long id)
            throws Exception, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(deletesql);
            ps.setLong(1, id);
            int count = ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
            }
            ;
            throw e;
        } finally {
            closeResource(ps, rs);
            ps = null;
            rs = null;
        }
    }
    public List<InformacoesComiteDTO> informacoesComites() {
        String sql = """
            SELECT id, nome, sigla
            FROM comites
            WHERE status = 'EM_ANDAMENTO'
            ORDER BY id
        """;

        List<InformacoesComiteDTO> comites = jdbcTemplate.query(sql, (rs, rowNum) -> {
            InformacoesComiteDTO informacoes = new InformacoesComiteDTO(rs.getLong("id"), rs.getString("nome"), rs.getString("sigla"));
            return informacoes;
        });

        return comites;
    }

    public InformacoesComiteDTO informacoesComitePorId(Long id) {
        String sql = """
        SELECT nome, sigla
        FROM comites
        WHERE id = ? 
    """;

        List<InformacoesComiteDTO> lista = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new InformacoesComiteDTO(id, rs.getString("nome"), rs.getString("sigla")),
                id
        );

        return lista.isEmpty() ? null : lista.get(0);
    }
}
