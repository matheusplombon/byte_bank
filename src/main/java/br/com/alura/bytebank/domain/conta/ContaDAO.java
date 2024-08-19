package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.RegraDeNegocioException;
import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {

    private Connection conn;

    ContaDAO(Connection connection) {
        this.conn = connection;
    }

    public void salvar(DadosAberturaConta dadosDaConta) {
        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), BigDecimal.ZERO, cliente, true);

        String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email, esta_ativa) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setInt(1, conta.getNumero()); // número da conta
            preparedStatement.setBigDecimal(2, BigDecimal.ZERO); // saldo
            preparedStatement.setString(3, dadosDaConta.dadosCliente().nome()); // nome do cliente
            preparedStatement.setString(4, dadosDaConta.dadosCliente().cpf()); // CPF do cliente
            preparedStatement.setString(5, dadosDaConta.dadosCliente().email()); // email do cliente
            preparedStatement.setBoolean(6, true); // conta ativa

            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir dados na tabela: " + e.getMessage(), e);
        }
    }

    public Set<Conta> listar() {
        PreparedStatement ps;
        ResultSet resultSet;
        Set<Conta> contas = new HashSet<>();

        String sql = "SELECT * FROM conta WHERE esta_ativa = true";

        try {
            ps = conn.prepareStatement(sql);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Integer numero = resultSet.getInt("numero");
                BigDecimal saldo = resultSet.getBigDecimal("saldo");
                String nome = resultSet.getString("cliente_nome");
                String cpf = resultSet.getString("cliente_cpf");
                String email = resultSet.getString("cliente_email");
                Boolean estaAtiva = resultSet.getBoolean("esta_ativa");

                DadosCadastroCliente dadosCadastroCliente =
                        new DadosCadastroCliente(nome, cpf, email);
                Cliente cliente = new Cliente(dadosCadastroCliente);

                contas.add(new Conta(numero, saldo, cliente, estaAtiva));
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return contas;
    }

    public void alterar(Integer numero, BigDecimal novoSaldo) {
        String sql = "UPDATE conta SET saldo = ? WHERE numero = ? and esta_ativa = true";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, novoSaldo);
            ps.setInt(2, numero);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RegraDeNegocioException("Não foi possível atualizar a conta. Verifique se a conta existe.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados", e);
        }
    }

    public void remover(Integer numero) {
        String sql = "DELETE FROM conta WHERE numero = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numero);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RegraDeNegocioException("Não foi possível remover a conta. Verifique se a conta existe.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados", e);
        }
    }

    public void alterarLogico(Integer numeroDaConta){
        String sql = "UPDATE conta SET esta_ativa = false WHERE numero = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numeroDaConta);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RegraDeNegocioException("Não foi possível atualizar a conta. Verifique se a conta existe.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados", e);
        }
    }
}
