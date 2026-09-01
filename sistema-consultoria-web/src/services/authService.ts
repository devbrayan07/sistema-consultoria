import { api } from './api';

export interface UsuarioLogado {
    id?: number;
    email: string;
    tipo?: string;
    token?: string;
}

// Função auxiliar para decodificar o Payload do Token JWT
function extrairDadosDoToken(token: string): { id?: number } | null {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('Erro ao decodificar token JWT', error);
        return null;
    }
}

export const authService = {
    async login(email: string, senha: string): Promise<UsuarioLogado> {
        const response = await api.post('/auth/login', {
            email: String(email).trim(),
            senha: String(senha).trim(),
        });

        const data = response.data;

        if (data.token) {
            localStorage.setItem('token', data.token);

            // Decodifica o Token para extrair o ID (ex: id: 32)
            const tokenDecodificado = extrairDadosDoToken(data.token);
            if (tokenDecodificado?.id) {
                data.id = tokenDecodificado.id; // Anexa o ID ao objeto do usuário!
            }
        }

        // Salva o usuário no localStorage já contendo o ID
        localStorage.setItem('usuario', JSON.stringify(data));

        return data;
    },

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('usuario');
    },

    getUsuarioAtual(): UsuarioLogado | null {
        const usuarioStr = localStorage.getItem('usuario');
        if (!usuarioStr) return null;
        try {
            return JSON.parse(usuarioStr);
        } catch {
            return null;
        }
    },

    isAutenticado(): boolean {
        return !!localStorage.getItem('token');
    },
};