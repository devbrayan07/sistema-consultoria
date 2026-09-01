package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.MetodoPagamento;
import com.ivig.sistemaconsultoria.enums.StatusPagamento;
import com.ivig.sistemaconsultoria.enums.TipoPagamento;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PagamentoResponseDTO {

    private Integer id;

    private Integer idUsuario;
    private String nomeUsuario;

    private Integer idEmpresa;
    private String razaoSocialEmpresa;

    private Integer idObrigacao;

    private TipoPagamento tipoPagamento;

    private MetodoPagamento metodoPagamento;

    private StatusPagamento status;

    private BigDecimal valor;

    private String idPagamentoExterno;

    private String codigoPix;

    private String qrCodePix;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataPagamento;

    private LocalDateTime dataExpiracao;
}