package br.com.zup.desafio1Modulo5.conta.controllers;

import br.com.zup.desafio1Modulo5.conta.configs.exceptions.DadosDigitadosIncorretamente;
import br.com.zup.desafio1Modulo5.conta.configs.exceptions.SolicitacaoNaoEncontrada;
import br.com.zup.desafio1Modulo5.conta.models.Conta;
import br.com.zup.desafio1Modulo5.conta.models.dtos.CadastroDeContaDTO;
import br.com.zup.desafio1Modulo5.conta.models.dtos.ExibicaoDeContasDTO;
import br.com.zup.desafio1Modulo5.conta.models.dtos.PagamentoDeContaDTO;
import br.com.zup.desafio1Modulo5.conta.models.enums.Status;
import br.com.zup.desafio1Modulo5.conta.models.enums.Tipo;
import br.com.zup.desafio1Modulo5.conta.services.ContaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/contas")
public class ContaController {

    @Autowired
    private ContaService contaService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Conta cadastrarConta(@RequestBody @Valid CadastroDeContaDTO cadastroDeContaDTO) {
        Conta conta = modelMapper.map(cadastroDeContaDTO, Conta.class);
        contaService.CadastrarConta(conta);
        return contaService.retornarContaPorID(conta.getId());
    }

    @GetMapping
    public List<ExibicaoDeContasDTO> exibirContasCadastradas(@RequestParam(required = false) Status status,
                                                             @RequestParam(required = false) Tipo tipo,
                                                             @RequestParam(required = false) Double valor) {
        List<ExibicaoDeContasDTO> contasDTOS = new ArrayList<>();
        for (Conta referencia : contaService.retornarContasPorFiltro(status, tipo, valor)) {
            contasDTOS.add(modelMapper.map(referencia, ExibicaoDeContasDTO.class));
        }
        return contasDTOS;
    }

    @GetMapping("/{id}")
    public Conta exibirContaPorId(@PathVariable Integer id) {
        for (Conta referencia : contaService.retornarTodasContasCadastradas()) {
            if (Objects.equals(referencia.getId(), id)) {
                return contaService.retornarContaPorID(id);
            }
        }
        throw new SolicitacaoNaoEncontrada("Conta não encontrada");
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Conta pagarConta(@PathVariable Integer id, @RequestBody PagamentoDeContaDTO pagamentoDeContaDTO) {
        Conta contaId = contaService.retornarContaPorID(id);
        for (Conta referencia : contaService.retornarTodasContasCadastradas()) {
            if (Objects.equals(referencia.getId(), id)) {
                if (pagamentoDeContaDTO.getStatus() == Status.PAGO) {
                    if (contaId.getStatus() == Status.PAGO) {
                        throw new DadosDigitadosIncorretamente("Esta conta já foi paga");
                    }
                    contaService.pagarConta(id);
                    return contaId;
                }
                throw new DadosDigitadosIncorretamente("Status de pagamento inválido");
            }
        }
        throw new SolicitacaoNaoEncontrada("Conta não encontrada");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarConta(@PathVariable Integer id) {
        contaService.deletarConta(id);
    }

}