package com.ivig.sistemaconsultoria.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MercadoPagoService {

    private final RestClient restClient;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.sandbox:true}")
    private boolean sandbox;

    @Value("${mercadopago.sandbox.email:test_user_br@testuser.com}")
    private String sandboxEmail;

    @Value("${mercadopago.sandbox.nome:APRO}")
    private String sandboxNome;

    @Value("${mercadopago.sandbox.valor:50.00}")
    private BigDecimal sandboxValor;


    /*
     * ============================================================
     * CONSTRUTOR
     * ============================================================
     */

    public MercadoPagoService() {

        this.restClient =
                RestClient.builder()
                        .baseUrl(
                                "https://api.mercadopago.com"
                        )
                        .build();
    }


    /*
     * ============================================================
     * CRIAR PIX
     * ============================================================
     */

    public MercadoPagoPixResponse criarPagamentoPix(
            BigDecimal valor,
            String descricao,
            String emailPagador
    ) {

        try {

            /*
             * =====================================================
             * SANDBOX
             *
             * Durante os testes usamos o cenário controlado
             * do Mercado Pago.
             *
             * Depois podemos transformar isso em configuração:
             *
             * mercadopago.sandbox=true
             *
             * Em produção:
             * - valor real
             * - email real
             * =====================================================
             */

            BigDecimal valorMercadoPago;
            String emailMercadoPago;
            String nomePagador;

            if (sandbox) {

                valorMercadoPago =
                        sandboxValor;

                emailMercadoPago =
                        sandboxEmail;

                nomePagador =
                        sandboxNome;

            } else {

                valorMercadoPago =
                        valor;

                emailMercadoPago =
                        emailPagador;

                nomePagador =
                        null;
            }


            /*
             * =====================================================
             * PAYMENT METHOD
             * =====================================================
             */

            Map<String, Object> paymentMethod =
                    new LinkedHashMap<>();

            paymentMethod.put(
                    "id",
                    "pix"
            );

            paymentMethod.put(
                    "type",
                    "bank_transfer"
            );


            /*
             * =====================================================
             * PAYMENT
             * =====================================================
             */

            Map<String, Object> payment =
                    new LinkedHashMap<>();

            payment.put(
                    "amount",
                    valorMercadoPago.toPlainString()
            );

            payment.put(
                    "payment_method",
                    paymentMethod
            );


            /*
             * =====================================================
             * TRANSACTIONS
             * =====================================================
             */

            Map<String, Object> transactions =
                    new LinkedHashMap<>();

            transactions.put(
                    "payments",
                    List.of(payment)
            );


            /*
             * =====================================================
             * PAYER
             * =====================================================
             */

            Map<String, Object> payer =
                    new LinkedHashMap<>();

            payer.put(
                    "email",
                    emailMercadoPago
            );

            if (nomePagador != null
                    && !nomePagador.isBlank()) {

                payer.put(
                        "first_name",
                        nomePagador
                );
            }

            /*
             * =====================================================
             * EXTERNAL REFERENCE
             * =====================================================
             */

            String externalReference =
                    "sistema-consultoria-"
                            + UUID.randomUUID();


            /*
             * =====================================================
             * ORDER
             * =====================================================
             */

            Map<String, Object> body =
                    new LinkedHashMap<>();

            body.put(
                    "type",
                    "online"
            );

            body.put(
                    "total_amount",
                    valorMercadoPago.toPlainString()
            );

            body.put(
                    "external_reference",
                    externalReference
            );

            body.put(
                    "processing_mode",
                    "automatic"
            );

            body.put(
                    "transactions",
                    transactions
            );

            body.put(
                    "payer",
                    payer
            );


            /*
             * =====================================================
             * IDEMPOTENCY KEY
             * =====================================================
             */

            String idempotencyKey =
                    UUID.randomUUID()
                            .toString();


            /*
             * =====================================================
             * CHAMADA À ORDERS API
             * =====================================================
             */

            String resposta =
                    restClient
                            .post()
                            .uri(
                                    "/v1/orders"
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "Bearer " + accessToken
                            )
                            .header(
                                    "X-Idempotency-Key",
                                    idempotencyKey
                            )
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .accept(
                                    MediaType.APPLICATION_JSON
                            )
                            .body(
                                    body
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );


            /*
             * =====================================================
             * VALIDAR RESPOSTA
             * =====================================================
             */

            if (resposta == null
                    || resposta.isBlank()) {

                throw new IllegalStateException(
                        "Mercado Pago retornou uma resposta vazia."
                );
            }


            /*
             * =====================================================
             * CONVERTER JSON
             * =====================================================
             */

            JsonNode root =
                    objectMapper.readTree(
                            resposta
                    );


            /*
             * =====================================================
             * ORDER
             * =====================================================
             */

            String orderId =
                    obterTexto(
                            root,
                            "id"
                    );

            String status =
                    obterTexto(
                            root,
                            "status"
                    );

            String statusDetail =
                    obterTexto(
                            root,
                            "status_detail"
                    );


            /*
             * =====================================================
             * TRANSACTIONS > PAYMENTS
             * =====================================================
             */

            JsonNode payments =
                    root
                            .path(
                                    "transactions"
                            )
                            .path(
                                    "payments"
                            );


            if (!payments.isArray()
                    || payments.isEmpty()) {

                throw new IllegalStateException(
                        "Mercado Pago não retornou a transação PIX."
                );
            }


            JsonNode paymentResponse =
                    payments.get(0);


            String paymentId =
                    obterTexto(
                            paymentResponse,
                            "id"
                    );


            /*
             * =====================================================
             * PAYMENT METHOD
             * =====================================================
             */

            JsonNode paymentMethodResponse =
                    paymentResponse.path(
                            "payment_method"
                    );


            String codigoPix =
                    obterTexto(
                            paymentMethodResponse,
                            "qr_code"
                    );


            String qrCodeBase64 =
                    obterTexto(
                            paymentMethodResponse,
                            "qr_code_base64"
                    );


            String ticketUrl =
                    obterTexto(
                            paymentMethodResponse,
                            "ticket_url"
                    );


            /*
             * =====================================================
             * VALIDAÇÕES
             * =====================================================
             */

            if (orderId == null
                    || orderId.isBlank()) {

                throw new IllegalStateException(
                        "Mercado Pago não retornou o ID da order."
                );
            }


            if (paymentId == null
                    || paymentId.isBlank()) {

                throw new IllegalStateException(
                        "Mercado Pago não retornou o ID do pagamento."
                );
            }


            if (codigoPix == null
                    || codigoPix.isBlank()) {

                throw new IllegalStateException(
                        "Mercado Pago criou a order, mas não retornou o código PIX."
                );
            }


            /*
             * =====================================================
             * RETORNO
             * =====================================================
             */

            return new MercadoPagoPixResponse(
                    orderId,
                    paymentId,
                    status,
                    statusDetail,
                    codigoPix,
                    qrCodeBase64,
                    ticketUrl
            );

        }

        /*
         * =========================================================
         * ERROS 4XX DO MERCADO PAGO
         * =========================================================
         */

        catch (HttpClientErrorException erro) {

            String respostaMercadoPago =
                    erro.getResponseBodyAsString();


            throw new IllegalStateException(
                    "Erro retornado pelo Mercado Pago: "
                            + respostaMercadoPago,
                    erro
            );
        }

        /*
         * =========================================================
         * OUTROS ERROS
         * =========================================================
         */

        catch (Exception erro) {

            throw new IllegalStateException(
                    "Não foi possível gerar o PIX no Mercado Pago.",
                    erro
            );
        }
    }


    /*
     * ============================================================
     * OBTER ORDER
     *
     * Será útil depois para:
     * - webhook
     * - consulta de status
     * - confirmação do pagamento
     * ============================================================
     */

    public String buscarOrder(
            String orderId
    ) {

        if (orderId == null
                || orderId.isBlank()) {

            throw new IllegalArgumentException(
                    "O ID da order é obrigatório."
            );
        }


        try {

            String resposta =
                    restClient
                            .get()
                            .uri(
                                    "/v1/orders/{id}",
                                    orderId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "Bearer " + accessToken
                            )
                            .accept(
                                    MediaType.APPLICATION_JSON
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );


            if (resposta == null
                    || resposta.isBlank()) {

                throw new IllegalStateException(
                        "Mercado Pago retornou resposta vazia ao consultar a order."
                );
            }


            return resposta;

        }
        catch (HttpClientErrorException erro) {

            throw new IllegalStateException(
                    "Erro ao consultar order no Mercado Pago: "
                            + erro.getResponseBodyAsString(),
                    erro
            );

        }
        catch (Exception erro) {

            throw new IllegalStateException(
                    "Não foi possível consultar a order no Mercado Pago.",
                    erro
            );
        }
    }


    /*
     * ============================================================
     * MÉTODO AUXILIAR
     * ============================================================
     */

    private String obterTexto(
            JsonNode node,
            String campo
    ) {

        if (node == null
                || node.isMissingNode()
                || node.isNull()) {

            return null;
        }


        JsonNode valor =
                node.get(
                        campo
                );


        if (valor == null
                || valor.isNull()
                || valor.isMissingNode()) {

            return null;
        }


        return valor.asText();
    }


    /*
     * ============================================================
     * RESPONSE PIX
     * ============================================================
     */

    @Getter
    public static class MercadoPagoPixResponse {

        private final String orderId;

        private final String paymentId;

        private final String status;

        private final String statusDetail;

        private final String codigoPix;

        private final String qrCodeBase64;

        private final String ticketUrl;


        public MercadoPagoPixResponse(
                String orderId,
                String paymentId,
                String status,
                String statusDetail,
                String codigoPix,
                String qrCodeBase64,
                String ticketUrl
        ) {

            this.orderId =
                    orderId;

            this.paymentId =
                    paymentId;

            this.status =
                    status;

            this.statusDetail =
                    statusDetail;

            this.codigoPix =
                    codigoPix;

            this.qrCodeBase64 =
                    qrCodeBase64;

            this.ticketUrl =
                    ticketUrl;
        }
    }
}