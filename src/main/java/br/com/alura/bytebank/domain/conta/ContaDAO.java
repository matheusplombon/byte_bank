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
        var conta = new Conta(dadosDaConta.numero(), cliente);

        String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setInt(1, conta.getNumero()); // número da conta
            preparedStatement.setBigDecimal(2, BigDecimal.ZERO); // saldo
            preparedStatement.setString(3, dadosDaConta.dadosCliente().nome()); // nome do cliente
            preparedStatement.setString(4, dadosDaConta.dadosCliente().cpf()); // CPF do cliente
            preparedStatement.setString(5, dadosDaConta.dadosCliente().email()); // email do cliente

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

        String sql = "SELECT * FROM conta";

        try {
            ps = conn.prepareStatement(sql);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Integer numero = resultSet.getInt(2);
                BigDecimal saldo = resultSet.getBigDecimal(3);
                String nome = resultSet.getString(4);
                String cpf = resultSet.getString(5);
                String email = resultSet.getString(6);

                DadosCadastroCliente dadosCadastroCliente =
                        new DadosCadastroCliente(nome, cpf, email);
                Cliente cliente = new Cliente(dadosCadastroCliente);

                contas.add(new Conta(numero, cliente));
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return contas;
    }

    public void alterar(Integer numero, BigDecimal valor) {
        String sql = "UPDATE conta SET saldo = saldo + ? WHERE numero = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, valor);
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
}
