import { api } from './api';

// Atualizado para bater com o enum do Java: [PAGA, PENDENTE, ATRASADA]
export type StatusObrigacao = 'PENDENTE' | 'PAGA' | 'ATRASADA';

export interface ObrigacaoFiscalRequest {
    titulo: string;
    descricao?: string;
    dataVencimento: string; // Formato "YYYY-MM-DD"
    status: StatusObrigacao;
    idEmpresa: number;
}

export interface ObrigacaoFiscalResponse {
    idObrigacao: number;
    titulo: string;
    descricao?: string;
    dataVencimento: string;
    status: StatusObrigacao;
    idEmpresa: number;
    razaoSocialEmpresa?: string;
}

export const obrigacaoService = {
    async listarTodas(): Promise<ObrigacaoFiscalResponse[]> {
        const response = await api.get<ObrigacaoFiscalResponse[]>('/obrigacoes');
        return response.data;
    },

    async criar(dados: ObrigacaoFiscalRequest): Promise<ObrigacaoFiscalResponse> {
        const response = await api.post<ObrigacaoFiscalResponse>('/obrigacoes', dados);
        return response.data;
    },

    async atualizarStatus(idObrigacao: number, status: StatusObrigacao): Promise<void> {
        await api.patch(`/obrigacoes/${idObrigacao}/status`, { status });
    },
};