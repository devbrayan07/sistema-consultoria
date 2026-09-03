package com.ivig.sistemaconsultoria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/empresas")
    public String empresas() {
        return "forward:/empresas.html";
    }

    @GetMapping("/obrigacoes")
    public String obrigacoes() {
        return "forward:/obrigacoes.html";
    }

    @GetMapping("/documentos")
    public String documentos() {
        return "forward:/documentos.html";
    }

    @GetMapping("/usuarios")
    public String usuarios() {
        return "forward:/usuarios.html";
    }

    @GetMapping("/pagamentos")
    public String pagamentos() {
        return "forward:/pagamentos.html";
    }

    @GetMapping("/esqueci-senha")
    public String esqueciSenha() {
        return "forward:/esqueci-senha.html";
    }

    @GetMapping("/redefinir-senha")
    public String redefinirSenha() {
        return "forward:/redefinir-senha.html";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "forward:/cadastro.html";
    }
}