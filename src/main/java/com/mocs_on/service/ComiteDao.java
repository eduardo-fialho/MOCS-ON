package com.mocs_on.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


import com.mocs_on.domain.Comite;
import com.mocs_on.domain.Comite.StatusComite;

public class ComiteDao {
    private final static String listAlunoProjetosSql = "select p.* from aluno_has_projeto ap, projeto p  where ap.projeto_id = p.id and ap.aluno_id = ? ";
    private final static String listAlunoEstagiosSql = "select p.* from aluno_has_estagio ap, estagio p  where ap.estagio_id = p.id and ap.aluno_id = ? ";

    private final static String getsql = "SELECT * FROM comite  WHERE id = ?";
    private final static String getByCpfSql = "SELECT * FROM aluno  WHERE cpf = ?";
    private final static String getByEmailSql = "SELECT * FROM aluno  WHERE email = ?";
    private final static String getByUsuario_idSql = "SELECT * FROM aluno  WHERE usuario_id = ?";
    private final static String listsql = "SELECT * FROM aluno";
    private final static String listByNomeSql = "SELECT * FROM aluno WHERE nome like ? ";
    private final static String listByCursoSql = "SELECT * FROM aluno WHERE curso = ? ";
    private final static String listByCampusSql = "SELECT * FROM aluno WHERE campus = ? ";
    private final static String listByPeriodoSql = "SELECT * FROM aluno WHERE periodo = ? ";
    private final static String listCursosSql = "SELECT DISTINCT curso FROM aluno ORDER BY curso";
    private final static String listCampusSql = "SELECT DISTINCT campus FROM aluno ORDER BY campus";
    private final static String listPeriodosSql = "SELECT DISTINCT periodo FROM aluno ORDER BY periodo";
    private final static String insertsql = "INSERT INTO aluno (cpf, nome, curso, campus, email, periodo, usuario_id, telefone, fotoPerfil, bannerPerfil, descricaoPerfil) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
    private final static String updatesql = "UPDATE aluno SET cpf = ?, nome = ?, curso = ?, campus = ?, email = ?, periodo = ?, usuario_id = ?, telefone = ?, fotoPerfil = ?, bannerPerfil = ?, descricaoPerfil = ? WHERE id = ? ";
    private final static String updateForCpfSql = "UPDATE aluno SET cpf = ?  WHERE id = ? ";
    private final static String updateForNomeSql = "UPDATE aluno SET nome = ?  WHERE id = ? ";
    private final static String updateForCursoSql = "UPDATE aluno SET curso = ?  WHERE id = ? ";
    private final static String updateForCampusSql = "UPDATE aluno SET campus = ?  WHERE id = ? ";
    private final static String updateForEmailSql = "UPDATE aluno SET email = ?  WHERE id = ? ";
    private final static String updateForPeriodoSql = "UPDATE aluno SET periodo = ?  WHERE id = ? ";
    private final static String updateForTelefoneSql = "UPDATE aluno SET telefone = ?  WHERE id = ? ";
    private final static String updateForFotoPerfilSql = "UPDATE aluno SET fotoPerfil = ?  WHERE id = ? ";
    private final static String updateForBannerPerfilSql = "UPDATE aluno SET bannerPerfil = ?  WHERE id = ? ";
    private final static String updateForDescricaoPerfilSql = "UPDATE aluno SET descricaoPerfil = ?  WHERE id = ? ";
    private final static String updateForUsuario_idSql = "UPDATE aluno SET usuario_id = ?  WHERE id = ? ";
    private final static String getProgressoEstagioSql = "SELECT progresso FROM aluno_has_estagio WHERE aluno_id = ? AND estagio_id = ?";
    private final static String setProgressoEstagioSql = "INSERT INTO aluno_has_estagio (aluno_id, estagio_id, progresso) VALUES (?, ?, ?) AS upd_row ON DUPLICATE KEY UPDATE progresso = upd_row.progresso;";
    private final static String deleteHasEstagioSql = "DELETE FROM aluno_has_estagio WHERE aluno_id = ? AND estagio_id = ?";

    private static void closeResource(Statement ps) {
        try {
            if (ps != null)
                ps.close();
        } catch (Exception e) {
            ps = null;
        }
    }

    private static void closeResource(Statement ps, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (Exception e) {
            rs = null;
        }
        try {
            if (ps != null)
                ps.close();
        } catch (Exception e) {
            ps = null;
        }
    }

    static Comite set(ResultSet rs) throws SQLException {
        Comite vo = new Comite();
        vo.setId(rs.getLong("id"));
        vo.setNome(rs.getString("nome"));
        vo.setDescricao(rs.getString("descricao"));
        vo.setNumeroDelegados(rs.getInt("numeroDelegados"));
        vo.setSigla(rs.getString("sigla"));
        vo.setStatus(StatusComite.valueOf(rs.getString("status")));
        
        return vo;
    }

    public static ArrayList<Comite> listComites(Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(listCursosSql);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return new ArrayList<Comite>();
            }
            ArrayList<Comite> list = new ArrayList<Comite>();
            do {
                String nome = rs.getString("nome");
                String descricao = rs.getString("descricao");
                String sigla = rs.getString("sigla");
                int numeroDelegados = rs.getInt("numeroDelegados");
                StatusComite status = StatusComite.valueOf(rs.getString("status"));
                long id = rs.getLong("id");
                
                Comite comite = new Comite(sigla, nome, status, numeroDelegados, descricao);
                
                list.add(comite);

            } 
            while (rs.next());
            
            return list;
        } catch (SQLException e) {
            throw e;
        } finally {
            closeResource(ps, rs);
            ps = null;
            rs = null;
        }
    }


    public static Comite get(Connection conn, long id) throws NotFoundException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(getsql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new NotFoundException("Object not found [" + id + "]");
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
            ps = conn.prepareStatement(insertsql);
            ps.setString(1, vo.getSigla());
            ps.setString(2, vo.getNome());
            ps.setString(3, vo.getStatusString());
            ps.setInt(4, vo.getNumeroDelegados());
            ps.setString(5, vo.getDescricao());
            ps.executeUpdate();
            conn.commit();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                vo.setId(id);
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

    public static void update(Connection conn, Aluno vo)
            throws NotFoundException, SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updatesql);
            ps.setString(1, vo.getCpf());
            ps.setString(2, vo.getNome());
            ps.setString(3, vo.getCurso());
            ps.setString(4, vo.getCampus());
            ps.setString(5, vo.getEmail());
            ps.setString(6, vo.getPeriodo());
            ps.setLong(7, vo.getUsuario_id());
            ps.setString(8, vo.getTelefone());
            ps.setBytes(9, vo.getFotoPerfil());
            ps.setBytes(10, vo.getBannerPerfil());
            ps.setString(11, vo.getDescricaoPerfil());
            ps.setLong(12, vo.getId());
            int count = ps.executeUpdate();
            if (count == 0) {
                throw new NotFoundException("Object not found [" + vo.getId() + "] .");
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
            throws NotFoundException, SQLException {
        Aluno a = new Aluno();
        Usuario u = new Usuario();
        String sql1 = "delete from aluno_has_projeto where aluno_id = ? ";
        String sql2 = "delete from aluno_has_estagio where aluno_id = ? ";
        String sql3 = "delete from candidatura where candidato_id = ? ";
        String sql4 = "delete from aluno where id = ? ";
        String sql5 = "delete from seguidores where seguidor_id = ? ";
        String sql6 = "delete from seguidores where seguindo_id = ? ";
        String sql7 = "delete from usuario where id = ? ";

        a = AlunoDao.get(conn, id);
        u = UsuarioDao.get(conn, a.getUsuario_id());

        deleteRelation(conn, sql1, id);
        deleteRelation(conn, sql2, id);
        deleteRelation(conn, sql3, id);
        deleteRelation(conn, sql4, id);
        deleteRelation(conn, sql5, u.getId());
        deleteRelation(conn, sql6, u.getId());
        deleteRelation(conn, sql7, u.getId());
    }
}