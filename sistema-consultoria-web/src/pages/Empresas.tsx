import React, { useEffect, useState } from 'react';
import { Building2, Plus, Mail, Phone, MapPin, FileText } from 'lucide-react';
import { empresaService, type EmpresaResponse } from '../services/empresaService';
import { authService } from '../services/authService';

export const Empresas: React.FC = () => {
    const [empresas, setEmpresas] = useState<EmpresaResponse[]>([]);
    const [carregando, setCarregando] = useState(true);
    const [exibirForm, setExibirForm] = useState(false);

    // Estados do formulário
    const [razaoSocial, setRazaoSocial] = useState('');
    const [cnpj, setCnpj] = useState('');
    const [regimeTributario, setRegimeTributario] = useState('SIMPLES_NACIONAL');
    const [email, setEmail] = useState('');
    const [telefone, setTelefone] = useState('');
    const [endereco, setEndereco] = useState('');
    const [mensagemErro, setMensagemErro] = useState('');

    const usuarioAtual = authService.getUsuarioAtual();

    const carregarEmpresas = async () => {
        try {
            setCarregando(true);
            const dados = await empresaService.listarTodas();
            setEmpresas(dados);
        } catch (err) {
            console.error('Erro ao carregar empresas', err);
        } finally {
            setCarregando(false);
        }
    };

    useEffect(() => {
        carregarEmpresas();
    }, []);

    const handleSalvar = async (e: React.FormEvent) => {
        e.preventDefault();
        setMensagemErro('');

        // Remove pontos, traços e barras do CNPJ (deixa apenas números)
        const cnpjSomenteNumeros = cnpj.replace(/\D/g, '');

        if (cnpjSomenteNumeros.length !== 14) {
            setMensagemErro('O CNPJ deve conter exatamente 14 números.');
            return;
        }

        // Tenta capturar o ID de diferentes formatos possíveis
        const idUsuario = usuarioAtual?.id || usuarioAtual?.idUsuario || usuarioAtual?.userId;

        if (!idUsuario) {
            setMensagemErro('Sessão sem ID de usuário. Clique em "Sair" e faça login novamente.');
            return;
        }

        try {
            await empresaService.criar({
                razaoSocial,
                cnpj: cnpjSomenteNumeros,
                regimeTributario,
                idUsuarioCliente: idUsuario,
                email,
                telefone,
                endereco,
            });

            // Limpa os campos após salvar
            setRazaoSocial('');
            setCnpj('');
            setEmail('');
            setTelefone('');
            setEndereco('');
            setRegimeTributario('SIMPLES_NACIONAL');
            setExibirForm(false);
            carregarEmpresas();
        } catch (err: any) {
            const dadosErro = err.response?.data;
            if (dadosErro?.messages && typeof dadosErro.messages === 'object') {
                const detalhes = Object.values(dadosErro.messages).join(' | ');
                setMensagemErro(`Erro de Validação: ${detalhes}`);
            } else {
                setMensagemErro(dadosErro?.message || 'Erro ao cadastrar empresa.');
            }
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <div>
                    <h1 style={styles.title}>Empresas</h1>
                    <p style={styles.subtitle}>Gerencie os clientes e empresas cadastradas no sistema</p>
                </div>
                <button
                    onClick={() => setExibirForm(!exibirForm)}
                    style={styles.btnPrimary}
                >
                    <Plus size={18} />
                    {exibirForm ? 'Cancelar' : 'Nova Empresa'}
                </button>
            </div>

            {mensagemErro && <div style={styles.errorBox}>{mensagemErro}</div>}

            {/* Formulário de Cadastro */}
            {exibirForm && (
                <form onSubmit={handleSalvar} style={styles.cardForm}>
                    <h3 style={styles.cardTitle}>Cadastrar Nova Empresa</h3>
                    <div style={styles.gridForm}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Razão Social</label>
                            <input
                                type="text"
                                required
                                style={styles.input}
                                value={razaoSocial}
                                onChange={(e) => setRazaoSocial(e.target.value)}
                                placeholder="Ex: Tech Solutions LTDA"
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>CNPJ (somente números)</label>
                            <input
                                type="text"
                                required
                                maxLength={18}
                                style={styles.input}
                                value={cnpj}
                                onChange={(e) => setCnpj(e.target.value)}
                                placeholder="00000000000191"
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Regime Tributário</label>
                            <select
                                style={styles.input}
                                value={regimeTributario}
                                onChange={(e) => setRegimeTributario(e.target.value)}
                            >
                                <option value="SIMPLES_NACIONAL">Simples Nacional</option>
                                <option value="LUCRO_PRESUMIDO">Lucro Presumido</option>
                                <option value="LUCRO_REAL">Lucro Real</option>
                                <option value="MEI">MEI</option>
                            </select>
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>E-mail de Contato</label>
                            <input
                                type="email"
                                style={styles.input}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="contato@empresa.com"
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Telefone</label>
                            <input
                                type="text"
                                style={styles.input}
                                value={telefone}
                                onChange={(e) => setTelefone(e.target.value)}
                                placeholder="(11) 99999-9999"
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Endereço</label>
                            <input
                                type="text"
                                style={styles.input}
                                value={endereco}
                                onChange={(e) => setEndereco(e.target.value)}
                                placeholder="Av. Paulista, 1000 - SP"
                            />
                        </div>
                    </div>

                    <div style={styles.formActions}>
                        <button type="submit" style={styles.btnPrimary}>
                            Salvar Empresa
                        </button>
                    </div>
                </form>
            )}

            {/* Tabela de Empresas */}
            <div style={styles.cardTable}>
                {carregando ? (
                    <p style={styles.loadingText}>Carregando empresas...</p>
                ) : empresas.length === 0 ? (
                    <div style={styles.emptyState}>
                        <Building2 size={48} color="#94a3b8" />
                        <p>Nenhuma empresa cadastrada ainda.</p>
                    </div>
                ) : (
                    <table style={styles.table}>
                        <thead>
                        <tr>
                            <th style={styles.th}>ID</th>
                            <th style={styles.th}>Razão Social</th>
                            <th style={styles.th}>CNPJ</th>
                            <th style={styles.th}>Regime</th>
                            <th style={styles.th}>Contato</th>
                            <th style={styles.th}>Endereço</th>
                        </tr>
                        </thead>
                        <tbody>
                        {empresas.map((emp) => (
                            <tr key={emp.id} style={styles.tr}>
                                <td style={styles.td}>#{emp.id}</td>
                                <td style={styles.tdBold}>{emp.razaoSocial}</td>
                                <td style={styles.td}>
                                    <span style={styles.badge}><FileText size={12} /> {emp.cnpj}</span>
                                </td>
                                <td style={styles.td}>{emp.regimeTributario}</td>
                                <td style={styles.td}>
                                    {emp.email && <div><Mail size={12} /> {emp.email}</div>}
                                    {emp.telefone && <div><Phone size={12} /> {emp.telefone}</div>}
                                </td>
                                <td style={styles.td}>
                                    {emp.endereco ? <div><MapPin size={12} /> {emp.endereco}</div> : '-'}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

const styles: { [key: string]: React.CSSProperties } = {
    container: { display: 'flex', flexDirection: 'column', gap: '1.5rem' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
    title: { fontSize: '1.75rem', margin: 0 },
    subtitle: { color: '#64748b', marginTop: '0.25rem' },
    btnPrimary: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        backgroundColor: '#0284c7',
        color: '#ffffff',
        border: 'none',
        padding: '0.6rem 1.2rem',
        borderRadius: '6px',
        fontWeight: 'bold',
        cursor: 'pointer',
    },
    cardForm: {
        backgroundColor: '#ffffff',
        padding: '1.5rem',
        borderRadius: '8px',
        border: '1px solid #e2e8f0',
        boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
    },
    cardTitle: { marginBottom: '1rem', fontSize: '1.1rem' },
    gridForm: { display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '0.3rem' },
    label: { fontSize: '0.85rem', fontWeight: 600, color: '#475569' },
    input: { padding: '0.6rem', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '0.95rem', outline: 'none' },
    formActions: { marginTop: '1rem', display: 'flex', justifyContent: 'flex-end' },
    cardTable: { backgroundColor: '#ffffff', borderRadius: '8px', border: '1px solid #e2e8f0', overflow: 'hidden' },
    table: { width: '100%', borderCollapse: 'collapse', textAlign: 'left' },
    th: { backgroundColor: '#f1f5f9', padding: '0.8rem 1rem', color: '#475569', fontSize: '0.85rem', textTransform: 'uppercase', borderBottom: '1px solid #e2e8f0' },
    tr: { borderBottom: '1px solid #f1f5f9' },
    td: { padding: '0.8rem 1rem', fontSize: '0.9rem', color: '#334155' },
    tdBold: { padding: '0.8rem 1rem', fontSize: '0.9rem', fontWeight: 600, color: '#0f172a' },
    badge: { display: 'inline-flex', alignItems: 'center', gap: '0.3rem', backgroundColor: '#f1f5f9', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.8rem' },
    loadingText: { padding: '2rem', textAlign: 'center', color: '#64748b' },
    emptyState: { padding: '3rem', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem', color: '#64748b' },
    errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '0.75rem', borderRadius: '6px', fontSize: '0.9rem' },
};