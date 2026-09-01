import axios from 'axios';

export const api = axios.create({
    baseURL: 'http://localhost:8082/api',
});

api.interceptors.request.use((config) => {
    // NUNCA envia o cabeçalho Authorization se a rota for de login/autenticação
    if (!config.url?.includes('/auth/')) {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
    }
    return config;
});