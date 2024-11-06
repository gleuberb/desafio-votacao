package com.desafio_votacao.api.utils;

import br.com.caelum.stella.validation.CPFValidator;

public class CpfValidator {
    public static boolean validarCPF(String cpf) {
        CPFValidator validator = new CPFValidator();
        return validator.invalidMessagesFor(cpf).isEmpty();
    }
}
