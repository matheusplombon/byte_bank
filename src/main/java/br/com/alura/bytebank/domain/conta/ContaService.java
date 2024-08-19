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
        // Busca a conta pelo número
        var conta = buscarContaPorNumero(numeroDaConta);

        // Verifica se o valor do saque é válido
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        // Verifica se a conta possui saldo suficiente
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para realizar o saque!");
        }

        if (!conta.getEstaAtiva()) {
            throw new RegraDeNegocioException("Conta não est[a ativa");
        }

        // Subtrai o valor do saldo da conta
        BigDecimal novoSaldo = conta.getSaldo().subtract(valor);

        // Atualiza o saldo da conta no banco de dados
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterar(numeroDaConta, novoSaldo);

        // Atualiza a lista de contas em memória
        atualizarContas();
    }




    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do depósito deve ser superior a zero!");
        }
        if (!conta.getEstaAtiva()) {
            throw new RegraDeNegocioException("Conta não est[a ativa");
        }

        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterar(numeroDaConta, valor);
    }

    public void realizarTransferencia(Integer numeroDaContaOrigem, Integer numeroDaContaDestino,
                                      BigDecimal valor){
        this.realizarSaque(numeroDaContaOrigem, valor);
        this.realizarDeposito(numeroDaContaDestino, valor);
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

    public void encerrarLogico(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.recuperarConexao();

        new ContaDAO(conn).alterarLogico(numeroDaConta);
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
