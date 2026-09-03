package com.ivig.sistemaconsultoria.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
            BigDecimal valorOriginal,
            String descricao,
            String emailPagador,
            String externalReference,
            String idempotencyKey
    ) throws JsonProcessingException {

        /*
         * ========================================================
         * VALIDAÇÕES INTERNAS
         * ========================================================
         */

        if (
                valorOriginal == null
                        ||
                        valorOriginal.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {

            throw new IllegalArgumentException(
                    "O valor do pagamento deve ser maior que zero."
            );
        }


        if (
                externalReference == null
                        ||
                        externalReference.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "A referência externa do pagamento é obrigatória."
            );
        }


        if (
                idempotencyKey == null
                        ||
                        idempotencyKey.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "A chave de idempotência é obrigatória."
            );
        }


        try {

            /*
             * =====================================================
             * DEFINIR VALORES DO AMBIENTE
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
                        valorOriginal;

                emailMercadoPago =
                        emailPagador;

                nomePagador =
                        null;
            }


            /*
             * =====================================================
             * VALIDAR VALOR DO SANDBOX
             * =====================================================
             */

            if (
                    valorMercadoPago == null
                            ||
                            valorMercadoPago.compareTo(
                                    BigDecimal.ZERO
                            ) <= 0
            ) {

                throw new IllegalStateException(
                        "O valor configurado para o Mercado Pago é inválido."
                );
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
                    List.of(
                            payment
                    )
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


            if (
                    nomePagador != null
                            &&
                            !nomePagador.isBlank()
            ) {

                payer.put(
                        "first_name",
                        nomePagador
                );
            }


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

            if (
                    resposta == null
                            ||
                            resposta.isBlank()
            ) {

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


            String orderStatus =
                    obterTexto(
                            root,
                            "status"
                    );


            String orderStatusDetail =
                    obterTexto(
                            root,
                            "status_detail"
                    );


            String externalReferenceRetornada =
                    obterTexto(
                            root,
                            "external_reference"
                    );


            BigDecimal totalAmount =
                    obterBigDecimal(
                            root,
                            "total_amount"
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


            if (
                    !payments.isArray()
                            ||
                            payments.isEmpty()
            ) {

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


            String paymentStatus =
                    obterTexto(
                            paymentResponse,
                            "status"
                    );


            String paymentStatusDetail =
                    obterTexto(
                            paymentResponse,
                            "status_detail"
                    );


            BigDecimal paymentAmount =
                    obterBigDecimal(
                            paymentResponse,
                            "amount"
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
             * VALOR EFETIVAMENTE COBRADO
             * =====================================================
             */

            BigDecimal valorCobrado;


            if (
                    paymentAmount != null
                            &&
                            paymentAmount.compareTo(
                                    BigDecimal.ZERO
                            ) > 0
            ) {

                valorCobrado =
                        paymentAmount;

            } else if (
                    totalAmount != null
                            &&
                            totalAmount.compareTo(
                                    BigDecimal.ZERO
                            ) > 0
            ) {

                valorCobrado =
                        totalAmount;

            } else {

                valorCobrado =
                        valorMercadoPago;
            }


            /*
             * =====================================================
             * VALIDAÇÕES
             * =====================================================
             */

            if (
                    orderId == null
                            ||
                            orderId.isBlank()
            ) {

                throw new IllegalStateException(
                        "Mercado Pago não retornou o ID da order."
                );
            }


            if (
                    paymentId == null
                            ||
                            paymentId.isBlank()
            ) {

                throw new IllegalStateException(
                        "Mercado Pago não retornou o ID do pagamento."
                );
            }


            if (
                    codigoPix == null
                            ||
                            codigoPix.isBlank()
            ) {

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

                    orderStatus,

                    orderStatusDetail,

                    paymentStatus,

                    paymentStatusDetail,

                    codigoPix,

                    qrCodeBase64,

                    ticketUrl,

                    valorCobrado,

                    externalReferenceRetornada
            );

        }

        /*
         * =========================================================
         * ERROS 4XX
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

            if (
                    erro instanceof IllegalArgumentException
                            ||
                            erro instanceof IllegalStateException
            ) {

                throw erro;
            }


            throw new IllegalStateException(
                    "Não foi possível gerar o PIX no Mercado Pago.",
                    erro
            );
        }
    }


    /*
     * ============================================================
     * BUSCAR ORDER
     * ============================================================
     */

    public String buscarOrder(
            String orderId
    ) {

        if (
                orderId == null
                        ||
                        orderId.isBlank()
        ) {

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


            if (
                    resposta == null
                            ||
                            resposta.isBlank()
            ) {

                throw new IllegalStateException(
                        "Mercado Pago retornou resposta vazia ao consultar a order."
                );
            }


            return resposta;

        } catch (HttpClientErrorException erro) {

            throw new IllegalStateException(
                    "Erro ao consultar order no Mercado Pago: "
                            + erro.getResponseBodyAsString(),
                    erro
            );

        } catch (Exception erro) {

            if (
                    erro instanceof IllegalArgumentException
                            ||
                            erro instanceof IllegalStateException
            ) {

                throw erro;
            }


            throw new IllegalStateException(
                    "Não foi possível consultar a order no Mercado Pago.",
                    erro
            );
        }
    }


    /*
     * ============================================================
     * OBTER TEXTO
     * ============================================================
     */

    private String obterTexto(
            JsonNode node,
            String campo
    ) {

        if (
                node == null
                        ||
                        node.isMissingNode()
                        ||
                        node.isNull()
        ) {

            return null;
        }


        JsonNode valor =
                node.get(
                        campo
                );


        if (
                valor == null
                        ||
                        valor.isNull()
                        ||
                        valor.isMissingNode()
        ) {

            return null;
        }


        return valor.asText();
    }


    /*
     * ============================================================
     * OBTER BIGDECIMAL
     * ============================================================
     */

    private BigDecimal obterBigDecimal(
            JsonNode node,
            String campo
    ) {

        String valor =
                obterTexto(
                        node,
                        campo
                );


        if (
                valor == null
                        ||
                        valor.isBlank()
        ) {

            return null;
        }


        try {

            return new BigDecimal(
                    valor
            );

        } catch (NumberFormatException erro) {

            return null;
        }
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

        private final String orderStatus;

        private final String orderStatusDetail;

        private final String paymentStatus;

        private final String paymentStatusDetail;

        private final String codigoPix;

        private final String qrCodeBase64;

        private final String ticketUrl;

        private final BigDecimal valorCobrado;

        private final String externalReference;


        public MercadoPagoPixResponse(
                String orderId,
                String paymentId,
                String orderStatus,
                String orderStatusDetail,
                String paymentStatus,
                String paymentStatusDetail,
                String codigoPix,
                String qrCodeBase64,
                String ticketUrl,
                BigDecimal valorCobrado,
                String externalReference
        ) {

            this.orderId =
                    orderId;

            this.paymentId =
                    paymentId;

            this.orderStatus =
                    orderStatus;

            this.orderStatusDetail =
                    orderStatusDetail;

            this.paymentStatus =
                    paymentStatus;

            this.paymentStatusDetail =
                    paymentStatusDetail;

            this.codigoPix =
                    codigoPix;

            this.qrCodeBase64 =
                    qrCodeBase64;

            this.ticketUrl =
                    ticketUrl;

            this.valorCobrado =
                    valorCobrado;

            this.externalReference =
                    externalReference;
        }
    }
}