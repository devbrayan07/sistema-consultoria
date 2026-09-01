import React, { useEffect, useState } from 'react';
import { Calendar, Plus, CheckCircle2, Clock, AlertTriangle, Building } from 'lucide-react';
import { obrigacaoService, type ObrigacaoFiscalResponse, type StatusObrigacao } from '../services/obrigacaoService.ts';
import { empresaService, type EmpresaResponse } from '../services/empresaService.tsx';

export const ObrigacoesFiscais: React.FC = () => {
    const [obrigacoes, setObrigacoes] = useState<ObrigacaoFiscalResponse[]>([]);
    const [empresas, setEmpresas] = useState<EmpresaResponse[]>([]);
    const [carregando, setCarregando] = useState(true);
    const [exibirForm, setExibirForm] = useState(false);

    // Estados do formulário
    const [idEmpresa, setIdEmpresa] = useState<number | ''>('');
    const [titulo, setTitulo] = useState('');
    const [dataVencimento, setDataVencimento] = useState('');
    const [descricao, setDescricao] = useState('');
    const [status, setStatus] = useState<StatusObrigacao>('PENDENTE');
    const [mensagemErro, setMensagemErro] = useState('');

    const carregarDados = async () => {
        try {
            setCarregando(true);
            const [listaObrigacoes, listaEmpresas] = await Promise.all([
                obrigacaoService.listarTodas().catch(() => []), // Retorna vazio se endpoint ainda não existir
                empresaService.listarTodas(),
            ]);
            setObrigacoes(listaObrigacoes);
            setEmpresas(listaEmpresas);
            if (listaEmpresas.length > 0 && !idEmpresa) {
                setIdEmpresa(listaEmpresas[0].idEmpresa);
            }
        } catch (err) {
            console.error('Erro ao carregar dados:', err);
        } finally {
            setCarregando(false);
        }
    };

    useEffect(() => {
        carregarDados();
    }, []);

    const handleSalvar = async (e: React.FormEvent) => {
        e.preventDefault();
        setMensagemErro('');

        if (!idEmpresa) {
            setMensagemErro('Selecione uma empresa válida.');
            return;
        }

        try {
            await obrigacaoService.criar({
                idEmpresa: Number(idEmpresa),
                titulo,
                dataVencimento,
                descricao,
                status,
            });

            // Reset dos campos
            setTitulo('');
            setDataVencimento('');
            setDescricao('');
            setStatus('PENDENTE');
            setExibirForm(false);
            carregarDados();
        } catch (err: any) {
            setMensagemErro(err.response?.data?.message || 'Erro ao cadastrar obrigação fiscal.');
        }
    };

    const getStatusBadge = (st: StatusObrigacao) => {
        switch (st) {
            case 'CONCLUIDO':
                return (
                    <span style={{ ...styles.badge, backgroundColor: '#dcfce7', color: '#15803d' }}>
            <CheckCircle2 size={12} /> Concluído
          </span>
                );
            case 'ATRASADO':
                return (
                    <span style={{ ...styles.badge, backgroundColor: '#fee2e2', color: '#b91c1c' }}>
            <AlertTriangle size={12} /> Atrasado
          </span>
                );
            default:
                return (
                    <span style={{ ...styles.badge, backgroundColor: '#fef3c7', color: '#b45309' }}>
            <Clock size={12} /> Pendente
          </span>
                );
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <div>
                    <h1 style={styles.title}>Obrigações Fiscais</h1>
                    <p style={styles.subtitle}>Acompanhe o calendário de impostos e guias das empresas</p>
                </div>
                <button onClick={() => setExibirForm(!exibirForm)} style={styles.btnPrimary}>
                    <Plus size={18} />
                    {exibirForm ? 'Cancelar' : 'Nova Obrigação'}
                </button>
            </div>

            {mensagemErro && <div style={styles.errorBox}>{mensagemErro}</div>}

            {/* Formulário de Cadastro */}
            {exibirForm && (
                <form onSubmit={handleSalvar} style={styles.cardForm}>
                    <h3 style={styles.cardTitle}>Cadastrar Obrigação Fiscal</h3>
                    <div style={styles.gridForm}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Empresa</label>
                            <select
                                style={styles.input}
                                value={idEmpresa}
                                onChange={(e) => setIdEmpresa(Number(e.target.value))}
                                required
                            >
                                <option value="">Selecione uma empresa...</option>
                                {empresas.map((emp) => (
                                    <option key={emp.idEmpresa} value={emp.idEmpresa}>
                                        {emp.razaoSocial}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Título da Guia/Obrigação</label>
                            <input
                                type="text"
                                required
                                style={styles.input}
                                value={titulo}
                                onChange={(e) => setTitulo(e.target.value)}
                                placeholder="Ex: DAS - Simples Nacional, SPED, DCTF"
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Data de Vencimento</label>
                            <input
                                type="date"
                                required
                                style={styles.input}
                                value={dataVencimento}
                                onChange={(e) => setDataVencimento(e.target.value)}
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Status Inicial</label>
                            <select
                                style={styles.input}
                                value={status}
                                onChange={(e) => setStatus(e.target.value as StatusObrigacao)}
                            >
                                <option value="PENDENTE">Pendente</option>
                                <option value="CONCLUIDO">Concluído</option>
                                <option value="ATRASADO">Atrasado</option>
                            </select>
                        </div>

                        <div style={{ ...styles.inputGroup, gridColumn: 'span 2' }}>
                            <label style={styles.label}>Observações / Descrição</label>
                            <input
                                type="text"
                                style={styles.input}
                                value={descricao}
                                onChange={(e) => setDescricao(e.target.value)}
                                placeholder="Ex: Guia referente à competência do mês anterior"
                            />
                        </div>
                    </div>

                    <div style={styles.formActions}>
                        <button type="submit" style={styles.btnPrimary}>
                            Salvar Obrigação
                        </button>
                    </div>
                </form>
            )}

            {/* Tabela de Obrigações */}
            <div style={styles.cardTable}>
                {carregando ? (
                    <p style={styles.loadingText}>Carregando obrigações...</p>
                ) : obrigacoes.length === 0 ? (
                    <div style={styles.emptyState}>
                        <Calendar size={48} color="#94a3b8" />
                        <p>Nenhuma obrigação fiscal cadastrada ainda.</p>
                    </div>
                ) : (
                    <table style={styles.table}>
                        <thead>
                        <tr>
                            <th style={styles.th}>ID</th>
                            <th style={styles.th}>Obrigação</th>
                            <th style={styles.th}>Empresa</th>
                            <th style={styles.th}>Vencimento</th>
                            <th style={styles.th}>Status</th>
                            <th style={styles.th}>Descrição</th>
                        </tr>
                        </thead>
                        <tbody>
                        {obrigacoes.map((ob) => (
                            <tr key={ob.idObrigacao} style={styles.tr}>
                                <td style={styles.td}>#{ob.idObrigacao}</td>
                                <td style={styles.tdBold}>{ob.titulo}</td>
                                <td style={styles.td}>
                                    <Building size={12} /> {ob.razaoSocialEmpresa || `Empresa #${ob.idEmpresa}`}
                                </td>
                                <td style={styles.td}>{ob.dataVencimento}</td>
                                <td style={styles.td}>{getStatusBadge(ob.status)}</td>
                                <td style={styles.td}>{ob.descricao || '-'}</td>
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
    badge: { display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 600 },
    loadingText: { padding: '2rem', textAlign: 'center', color: '#64748b' },
    emptyState: { padding: '3rem', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem', color: '#64748b' },
    errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '0.75rem', borderRadius: '6px', fontSize: '0.9rem' },
};