package com.ivig.sistemaconsultoria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
public class EmailService {

    private final RestClient restClient;
    private final String remetente;

    public EmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String remetente
    ) {

        this.remetente = remetente;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .build();
    }

    public void enviarRecuperacaoSenha(
            String destinatario,
            String nomeUsuario,
            String linkRecuperacao
    ) {

        String assunto =
                "Redefinição de senha - Mercury Consultoria";

        String conteudo =
                "Olá, " + nomeUsuario + ".\n\n"
                        + "Recebemos uma solicitação para redefinir a senha da sua conta.\n\n"
                        + "Acesse o link abaixo para criar uma nova senha:\n\n"
                        + linkRecuperacao
                        + "\n\n"
                        + "Este link é válido por 30 minutos e poderá ser utilizado apenas uma vez.\n\n"
                        + "Se você não solicitou esta alteração, ignore este e-mail.\n\n"
                        + "Mercury Consultoria";

        Map<String, Object> corpo = Map.of(
                "from",
                "Mercury Consultoria <" + remetente + ">",
                "to",
                new String[]{destinatario},
                "subject",
                assunto,
                "text",
                conteudo
        );

        try {

            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException erro) {

            throw new IllegalStateException(
                    "O serviço de e-mail recusou o envio. HTTP "
                            + erro.getStatusCode().value()
                            + ".",
                    erro
            );

        } catch (RestClientException erro) {

            throw new IllegalStateException(
                    "Não foi possível conectar ao serviço de e-mail.",
                    erro
            );
        }
    }
}