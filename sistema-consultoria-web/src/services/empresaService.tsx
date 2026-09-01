import { api } from './api';

export interface EmpresaRequest {
    razaoSocial: string;
    cnpj: string;
    regimeTributario: string;
    idUsuarioCliente: number;
    telefone?: string;
    email?: string;
    endereco?: string;
}

export interface EmpresaResponse {
    idEmpresa: number; // 👈 Alterado para bater com o Java
    razaoSocial: string;
    nomeFantasia?: string | null;
    cnpj: string;
    regimeTributario: string;
    idUsuarioCliente: number;
    nomeCliente?: string;
    criadoEm?: string;
    // Mantemos como opcionais caso adicione no backend depois:
    email?: string;
    telefone?: string;
    endereco?: string;
}

export const empresaService = {
    async listarTodas(): Promise<EmpresaResponse[]> {
        const response = await api.get<EmpresaResponse[]>('/empresas');
        return response.data;
    },

    async criar(empresa: EmpresaRequest): Promise<EmpresaResponse> {
        const response = await api.post<EmpresaResponse>('/empresas', empresa);
        return response.data;
    },
};