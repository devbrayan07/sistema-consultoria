package com.ivig.sistemaconsultoria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class MercadoPagoWebhookService {

    @Value("${mercadopago.webhook-secret}")
    private String webhookSecret;


    /**
     * Valida a assinatura recebida no webhook do Mercado Pago.
     *
     * Formato esperado do header:
     *
     * x-signature:
     * ts=1234567890,v1=abcdef...
     *
     * Manifesto:
     *
     * id:<data.id>;request-id:<x-request-id>;ts:<ts>;
     */
    public boolean validarAssinatura(
            String xSignature,
            String xRequestId,
            String dataId
    ) {

        if (xSignature == null || xSignature.isBlank()) {
            return false;
        }

        if (xRequestId == null || xRequestId.isBlank()) {
            return false;
        }

        if (dataId == null || dataId.isBlank()) {
            return false;
        }

        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException(
                    "A chave secreta do webhook do Mercado Pago não foi configurada."
            );
        }


        try {

            Map<String, String> assinatura =
                    extrairDadosAssinatura(xSignature);


            String timestamp =
                    assinatura.get("ts");

            String assinaturaRecebida =
                    assinatura.get("v1");


            if (timestamp == null
                    || timestamp.isBlank()
                    || assinaturaRecebida == null
                    || assinaturaRecebida.isBlank()) {

                return false;
            }


            /*
             * O Mercado Pago usa o data.id no manifesto.
             *
             * Orders normalmente possuem IDs alfanuméricos,
             * por exemplo ORD....
             *
             * A normalização para lowercase evita divergências
             * para identificadores alfanuméricos.
             */
            String dataIdNormalizado =
                    dataId.toLowerCase(Locale.ROOT);


            String manifesto =
                    "id:" + dataIdNormalizado +
                            ";request-id:" + xRequestId +
                            ";ts:" + timestamp +
                            ";";


            String assinaturaCalculada =
                    gerarHmacSha256(
                            webhookSecret,
                            manifesto
                    );


            return MessageDigest.isEqual(
                    assinaturaCalculada
                            .getBytes(StandardCharsets.UTF_8),

                    assinaturaRecebida
                            .toLowerCase(Locale.ROOT)
                            .getBytes(StandardCharsets.UTF_8)
            );


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Não foi possível validar a assinatura do webhook do Mercado Pago.",
                    e
            );
        }
    }


    /**
     * Converte:
     *
     * ts=123,v1=abc
     *
     * em:
     *
     * ts -> 123
     * v1 -> abc
     */
    private Map<String, String> extrairDadosAssinatura(
            String xSignature
    ) {

        Map<String, String> dados =
                new HashMap<>();


        String[] partes =
                xSignature.split(",");


        for (String parte : partes) {

            String[] chaveValor =
                    parte.trim().split("=", 2);


            if (chaveValor.length == 2) {

                String chave =
                        chaveValor[0].trim();

                String valor =
                        chaveValor[1].trim();


                if (!chave.isBlank()
                        && !valor.isBlank()) {

                    dados.put(
                            chave,
                            valor
                    );
                }
            }
        }


        return dados;
    }


    /**
     * Calcula HMAC-SHA256 e devolve em hexadecimal.
     */
    private String gerarHmacSha256(
            String segredo,
            String mensagem
    ) throws Exception {

        Mac mac =
                Mac.getInstance("HmacSHA256");


        SecretKeySpec secretKey =
                new SecretKeySpec(
                        segredo.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        "HmacSHA256"
                );


        mac.init(secretKey);


        byte[] resultado =
                mac.doFinal(
                        mensagem.getBytes(
                                StandardCharsets.UTF_8
                        )
                );


        return converterParaHexadecimal(
                resultado
        );
    }


    private String converterParaHexadecimal(
            byte[] bytes
    ) {

        StringBuilder hexadecimal =
                new StringBuilder(
                        bytes.length * 2
                );


        for (byte b : bytes) {

            hexadecimal.append(
                    String.format(
                            "%02x",
                            b & 0xff
                    )
            );
        }


        return hexadecimal.toString();
    }
}