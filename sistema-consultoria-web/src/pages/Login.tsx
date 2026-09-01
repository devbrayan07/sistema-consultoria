import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';

export const Login: React.FC = () => {
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [erro, setErro] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setErro('');

        try {
            // Garante que estamos passando textos limpos para a API
            await authService.login(String(email).trim(), String(senha).trim());
            navigate('/dashboard');
        } catch (err: any) {
            setErro(err.response?.data?.message || 'E-mail ou senha inválidos.');
        }
    };

    return (
        <div style={styles.container}>
            <form onSubmit={handleLogin} style={styles.card}>
                <h2>Acesso ao Sistema</h2>
                {erro && <div style={styles.error}>{erro}</div>}

                <div style={styles.inputGroup}>
                    <label>E-mail</label>
                    <input
                        type="email"
                        required
                        value={email}
                        onChange={(e) => setEmail(e.target.value)} // 👈 AQUI: e.target.value
                        placeholder="dev@gmail.com"
                    />
                </div>

                <div style={styles.inputGroup}>
                    <label>Senha</label>
                    <input
                        type="password"
                        required
                        value={senha}
                        onChange={(e) => setSenha(e.target.value)} // 👈 AQUI: e.target.value
                        placeholder="••••••••"
                    />
                </div>

                <button type="submit">Entrar</button>
            </form>
        </div>
    );
};

const styles = {
    container: { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f8fafc' },
    card: { padding: '2rem', backgroundColor: '#ffffff', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column' as const, gap: '1rem', width: '320px' },
    inputGroup: { display: 'flex', flexDirection: 'column' as const, gap: '0.25rem' },
    error: { color: '#991b1b', backgroundColor: '#fee2e2', padding: '0.5rem', borderRadius: '4px', fontSize: '0.85rem' }
};