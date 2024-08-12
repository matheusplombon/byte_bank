package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;
import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

public class ContaService {

    private ConnectionFactory connection;
    private Set<Conta> contas = new HashSet<>();

    public ContaService() {
        this.connection = new ConnectionFactory();
        atualizarContas(); // Atualiza a lista ao iniciar o serviço
    }

    private void atualizarContas() {
        Connection conn = connection.recuperarConexao();
        contas = new ContaDAO(conn).listar();
    }

    public Set<Conta> listarContasAbertas() {
        atualizarContas(); // Atualiza a lista antes de listar
        return contas;
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).salvar(dadosDaConta);
        atualizarContas(); // Atualiza a lista após abrir a conta
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        conta.sacar(valor);
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do depósito deve ser superior a zero!");
        }

        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterar(numeroDaConta, valor);
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).remover(numeroDaConta); // Remove a conta do banco de dados
        atualizarContas(); // Atualiza a lista após encerrar a conta
    }

    private Conta buscarContaPorNumero(Integer numero) {
        atualizarContas(); // Atualiza a lista antes de buscar
        return contas
                .stream()
                .filter(c -> c.getNumero().equals(numero))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Não existe conta cadastrada com esse número!"));
    }
}
