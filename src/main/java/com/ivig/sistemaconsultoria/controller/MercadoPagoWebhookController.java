package com.ivig.sistemaconsultoria.controller;

import com.ivig.sistemaconsultoria.service.MercadoPagoWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {

    private final MercadoPagoWebhookService webhookService;


    @PostMapping
    public ResponseEntity<Void> receberWebhook(
            HttpServletRequest request,

            @RequestParam(
                    name = "data.id",
                    required = false
            )
            String dataId,

            @RequestParam(
                    name = "type",
                    required = false
            )
            String type
    ) {

        String xSignature =
                request.getHeader(
                        "x-signature"
                );


        String xRequestId =
                request.getHeader(
                        "x-request-id"
                );


        /*
         * ========================================================
         * VALIDAR DADOS BÁSICOS
         * ========================================================
         */

        if (
                xSignature == null
                        ||
                        xSignature.isBlank()
                        ||
                        xRequestId == null
                        ||
                        xRequestId.isBlank()
                        ||
                        dataId == null
                        ||
                        dataId.isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }


        /*
         * ========================================================
         * VALIDAR ASSINATURA
         * ========================================================
         */

        boolean assinaturaValida =
                webhookService.validarAssinatura(
                        xSignature,
                        xRequestId,
                        dataId
                );


        if (!assinaturaValida) {

            return ResponseEntity
                    .status(401)
                    .build();
        }


        /*
         * ========================================================
         * FILTRAR TIPO
         * ========================================================
         */

        if (
                type == null
                        ||
                        !type.equalsIgnoreCase(
                                "order"
                        )
        ) {

            /*
             * Evento válido, mas não pertence ao fluxo de Orders.
             */

            return ResponseEntity
                    .ok()
                    .build();
        }


        /*
         * ========================================================
         * PROCESSAR ORDER
         * ========================================================
         */

        try {

            webhookService.processarOrder(
                    dataId
            );


            return ResponseEntity
                    .ok()
                    .build();

        } catch (IllegalArgumentException erro) {

            /*
             * A notificação pode ser legítima, mas eventualmente
             * pertencer a uma Order que não existe no nosso banco.
             *
             * Retornamos 200 para impedir retries infinitos de um
             * evento que não conseguimos relacionar.
             */

            System.out.println(
                    "Webhook ignorado: "
                            + erro.getMessage()
            );


            return ResponseEntity
                    .ok()
                    .build();

        } catch (Exception erro) {

            /*
             * Falha temporária:
             * banco indisponível, Mercado Pago indisponível etc.
             *
             * Não devolvemos 200 porque queremos permitir nova
             * tentativa do provedor.
             */

            System.err.println(
                    "Erro ao processar webhook Mercado Pago: "
                            + erro.getMessage()
            );


            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}