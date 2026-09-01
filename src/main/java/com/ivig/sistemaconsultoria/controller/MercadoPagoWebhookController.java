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
            ) String dataId,
            @RequestParam(
                    name = "type",
                    required = false
            ) String type
    ) {

        String xSignature =
                request.getHeader(
                        "x-signature"
                );

        String xRequestId =
                request.getHeader(
                        "x-request-id"
                );


        System.out.println(
                "=========================================="
        );

        System.out.println(
                "WEBHOOK MERCADO PAGO RECEBIDO"
        );

        System.out.println(
                "Type: " + type
        );

        System.out.println(
                "Data ID: " + dataId
        );

        System.out.println(
                "Request ID: " + xRequestId
        );

        System.out.println(
                "=========================================="
        );


        /*
         * =========================================================
         * DADOS OBRIGATÓRIOS PARA VALIDAÇÃO
         * =========================================================
         */

        if (xSignature == null
                || xSignature.isBlank()
                || xRequestId == null
                || xRequestId.isBlank()
                || dataId == null
                || dataId.isBlank()) {

            System.out.println(
                    "Webhook rejeitado: dados de assinatura ausentes."
            );


            return ResponseEntity
                    .badRequest()
                    .build();
        }


        /*
         * =========================================================
         * VALIDAÇÃO DA ASSINATURA
         * =========================================================
         */

        boolean assinaturaValida =
                webhookService.validarAssinatura(
                        xSignature,
                        xRequestId,
                        dataId
                );


        if (!assinaturaValida) {

            System.out.println(
                    "Webhook rejeitado: assinatura inválida."
            );


            return ResponseEntity
                    .status(401)
                    .build();
        }


        /*
         * =========================================================
         * FILTRAGEM DO TIPO
         * =========================================================
         */

        if (type != null
                && !type.isBlank()
                && !type.equalsIgnoreCase("order")) {

            System.out.println(
                    "Webhook ignorado: tipo diferente de order."
            );


            /*
             * Retornamos 200 porque a notificação foi recebida
             * corretamente, apenas não é relevante para este fluxo.
             */
            return ResponseEntity
                    .ok()
                    .build();
        }


        /*
         * =========================================================
         * ASSINATURA VÁLIDA
         * =========================================================
         */

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "WEBHOOK MERCADO PAGO AUTÊNTICO"
        );

        System.out.println(
                "Order ID: " + dataId
        );

        System.out.println(
                "=========================================="
        );


        /*
         * Próxima etapa:
         *
         * mercadoPagoService.buscarOrder(dataId);
         *
         * Depois:
         *
         * localizar Pagamento por idPagamentoExterno
         * verificar status real da Order
         * atualizar Pagamento
         */


        return ResponseEntity
                .ok()
                .build();
    }
}