package com.ivig.sistemaconsultoria.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivig.sistemaconsultoria.enums.StatusPagamento;
import com.ivig.sistemaconsultoria.model.Pagamento;
import com.ivig.sistemaconsultoria.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MercadoPagoWebhookService {

    private final PagamentoRepository pagamentoRepository;

    private final MercadoPagoService mercadoPagoService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${mercadopago.webhook-secret}")
    private String webhookSecret;


    /*
     * ============================================================
     * VALIDAR ASSINATURA
     * ============================================================
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
                    extrairDadosAssinatura(
                            xSignature
                    );


            String timestamp =
                    assinatura.get(
                            "ts"
                    );


            String assinaturaRecebida =
                    assinatura.get(
                            "v1"
                    );


            if (
                    timestamp == null
                            ||
                            timestamp.isBlank()
                            ||
                            assinaturaRecebida == null
                            ||
                            assinaturaRecebida.isBlank()
            ) {

                return false;
            }


            /*
             * IMPORTANTE:
             *
             * O manifesto deve utilizar exatamente o data.id
             * recebido pelo Mercado Pago.
             */

            String manifesto =
                    "id:" + dataId
                            + ";request-id:" + xRequestId
                            + ";ts:" + timestamp
                            + ";";


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

        } catch (Exception erro) {

            throw new IllegalStateException(
                    "Não foi possível validar a assinatura do webhook do Mercado Pago.",
                    erro
            );
        }
    }


    /*
     * ============================================================
     * PROCESSAR ORDER
     * ============================================================
     */

    @Transactional
    public void processarOrder(
            String orderId
    ) throws JsonProcessingException {

        if (
                orderId == null
                        ||
                        orderId.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "O ID da order é obrigatório."
            );
        }


        /*
         * ========================================================
         * LOCALIZAR PAGAMENTO LOCAL
         * ========================================================
         */

        Pagamento pagamento =
                pagamentoRepository
                        .findByIdOrderExterno(
                                orderId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Nenhum pagamento local encontrado para a Order "
                                                + orderId
                                )
                        );


        /*
         * ========================================================
         * CONSULTAR MERCADO PAGO
         * ========================================================
         */

        String resposta =
                mercadoPagoService.buscarOrder(
                        orderId
                );


        try {

            JsonNode root =
                    objectMapper.readTree(
                            resposta
                    );


            String statusOrder =
                    obterTexto(
                            root,
                            "status"
                    );


            String statusDetailOrder =
                    obterTexto(
                            root,
                            "status_detail"
                    );


            /*
             * ====================================================
             * TRANSAÇÃO
             * ====================================================
             */

            JsonNode payments =
                    root
                            .path("transactions")
                            .path("payments");


            JsonNode paymentNode =
                    null;


            if (
                    payments.isArray()
                            &&
                            !payments.isEmpty()
            ) {

                paymentNode =
                        payments.get(0);
            }


            String idTransacao =
                    obterTexto(
                            paymentNode,
                            "id"
                    );


            String statusTransacao =
                    obterTexto(
                            paymentNode,
                            "status"
                    );


            String statusDetailTransacao =
                    obterTexto(
                            paymentNode,
                            "status_detail"
                    );


            /*
             * ====================================================
             * ATUALIZAR IDs
             * ====================================================
             */

            if (
                    idTransacao != null
                            &&
                            !idTransacao.isBlank()
            ) {

                pagamento.setIdTransacaoExterna(
                        idTransacao
                );
            }


            /*
             * ====================================================
             * CONVERTER STATUS
             * ====================================================
             */

            StatusPagamento novoStatus =
                    converterStatus(
                            statusOrder,
                            statusDetailOrder,
                            statusTransacao,
                            statusDetailTransacao
                    );


            StatusPagamento statusAnterior =
                    pagamento.getStatus();


            pagamento.setStatus(
                    novoStatus
            );


            pagamento.setDataAtualizacao(
                    LocalDateTime.now()
            );


            /*
             * ====================================================
             * DATAS POR STATUS
             * ====================================================
             */

            if (
                    novoStatus == StatusPagamento.PAGO
                            &&
                            pagamento.getDataPagamento() == null
            ) {

                pagamento.setDataPagamento(
                        LocalDateTime.now()
                );
            }


            if (
                    novoStatus == StatusPagamento.CANCELADO
                            &&
                            pagamento.getDataCancelamento() == null
            ) {

                pagamento.setDataCancelamento(
                        LocalDateTime.now()
                );
            }


            if (
                    novoStatus == StatusPagamento.EXPIRADO
                            &&
                            pagamento.getDataExpiracao() == null
            ) {

                pagamento.setDataExpiracao(
                        LocalDateTime.now()
                );
            }


            /*
             * ====================================================
             * MOTIVO DE RECUSA / FALHA
             * ====================================================
             */

            if (
                    novoStatus == StatusPagamento.RECUSADO
            ) {

                String motivo =
                        statusDetailTransacao != null
                                ? statusDetailTransacao
                                : statusDetailOrder;


                pagamento.setMotivoRecusa(
                        motivo
                );

            } else {

                pagamento.setMotivoRecusa(
                        null
                );
            }


            /*
             * ====================================================
             * SALVAR
             * ====================================================
             */

            pagamentoRepository.save(
                    pagamento
            );


            System.out.println(
                    "Pagamento "
                            + pagamento.getId()
                            + " atualizado: "
                            + statusAnterior
                            + " -> "
                            + novoStatus
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
                    "Não foi possível processar a Order recebida pelo webhook.",
                    erro
            );
        }
    }


    /*
     * ============================================================
     * CONVERTER STATUS MERCADO PAGO -> SISTEMA
     * ============================================================
     */

    private StatusPagamento converterStatus(
            String statusOrder,
            String statusDetailOrder,
            String statusTransacao,
            String statusDetailTransacao
    ) {

        /*
         * ========================================================
         * PRIORIDADE PARA STATUS FINAL POSITIVO
         * ========================================================
         */

        if (
                "processed".equalsIgnoreCase(
                        statusOrder
                )
                        &&
                        "accredited".equalsIgnoreCase(
                                statusDetailOrder
                        )
        ) {

            return StatusPagamento.PAGO;
        }


        if (
                "processed".equalsIgnoreCase(
                        statusTransacao
                )
                        &&
                        "accredited".equalsIgnoreCase(
                                statusDetailTransacao
                        )
        ) {

            return StatusPagamento.PAGO;
        }


        /*
         * ========================================================
         * ESTORNO
         * ========================================================
         */

        if (
                "refunded".equalsIgnoreCase(
                        statusOrder
                )
                        ||
                        "refunded".equalsIgnoreCase(
                                statusTransacao
                        )
        ) {

            return StatusPagamento.ESTORNADO;
        }


        /*
         * ========================================================
         * EXPIRADO
         * ========================================================
         */

        if (
                "expired".equalsIgnoreCase(
                        statusOrder
                )
                        ||
                        "expired".equalsIgnoreCase(
                                statusTransacao
                        )
        ) {

            return StatusPagamento.EXPIRADO;
        }


        /*
         * ========================================================
         * CANCELADO
         * ========================================================
         */

        if (
                "canceled".equalsIgnoreCase(
                        statusOrder
                )
                        ||
                        "canceled".equalsIgnoreCase(
                                statusTransacao
                        )
        ) {

            return StatusPagamento.CANCELADO;
        }


        /*
         * ========================================================
         * FALHOU / RECUSADO
         * ========================================================
         */

        if (
                "failed".equalsIgnoreCase(
                        statusOrder
                )
                        ||
                        "failed".equalsIgnoreCase(
                                statusTransacao
                        )
        ) {

            return StatusPagamento.RECUSADO;
        }


        /*
         * ========================================================
         * PROCESSANDO
         * ========================================================
         */

        if (
                "processing".equalsIgnoreCase(
                        statusOrder
                )
                        ||
                        "processing".equalsIgnoreCase(
                                statusTransacao
                        )
        ) {

            return StatusPagamento.PROCESSANDO;
        }


        /*
         * ========================================================
         * AGUARDANDO PAGAMENTO / PIX
         * ========================================================
         */

        if (
                "action_required".equalsIgnoreCase(
                        statusOrder
                )
                        ||
                        "action_required".equalsIgnoreCase(
                                statusTransacao
                        )
                        ||
                        "created".equalsIgnoreCase(
                                statusOrder
                        )
                        ||
                        "created".equalsIgnoreCase(
                                statusTransacao
                        )
        ) {

            return StatusPagamento.PENDENTE;
        }


        /*
         * Status desconhecido.
         *
         * Não assumimos que foi pago.
         */

        return StatusPagamento.PENDENTE;
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
     * EXTRAIR ASSINATURA
     * ============================================================
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
                    parte
                            .trim()
                            .split(
                                    "=",
                                    2
                            );


            if (
                    chaveValor.length == 2
            ) {

                String chave =
                        chaveValor[0]
                                .trim();


                String valor =
                        chaveValor[1]
                                .trim();


                if (
                        !chave.isBlank()
                                &&
                                !valor.isBlank()
                ) {

                    dados.put(
                            chave,
                            valor
                    );
                }
            }
        }


        return dados;
    }


    /*
     * ============================================================
     * HMAC SHA-256
     * ============================================================
     */

    private String gerarHmacSha256(
            String segredo,
            String mensagem
    ) throws Exception {

        Mac mac =
                Mac.getInstance(
                        "HmacSHA256"
                );


        SecretKeySpec secretKey =
                new SecretKeySpec(
                        segredo.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        "HmacSHA256"
                );


        mac.init(
                secretKey
        );


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


    /*
     * ============================================================
     * HEX
     * ============================================================
     */

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