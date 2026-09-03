package com.ivig.sistemaconsultoria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String remetente;

    public void enviarRecuperacaoSenha(
            String destinatario,
            String nomeUsuario,
            String linkRecuperacao
    ) {

        SimpleMailMessage mensagem =
                new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject(
                "Redefinição de senha - Mercury Consultoria"
        );

        mensagem.setText(
                "Olá, " + nomeUsuario + ".\n\n"
                        + "Recebemos uma solicitação para redefinir a senha da sua conta.\n\n"
                        + "Acesse o link abaixo para criar uma nova senha:\n\n"
                        + linkRecuperacao
                        + "\n\n"
                        + "Este link é válido por 30 minutos e poderá ser utilizado apenas uma vez.\n\n"
                        + "Se você não solicitou esta alteração, ignore este e-mail.\n\n"
                        + "Mercury Consultoria"
        );

        System.out.println("Enviando e-mail para: " + destinatario);

        mailSender.send(mensagem);

        System.out.println("E-mail enviado pelo Spring sem exceção.");
    }
}