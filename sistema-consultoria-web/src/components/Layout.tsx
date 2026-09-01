import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import {
    Building2,
    FileText,
    Receipt,
    BarChart3,
    Bell,
    LogOut,
    LayoutDashboard
} from 'lucide-react';
import { authService } from '../services/authService';

interface LayoutProps {
    children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
    const navigate = useNavigate();
    const location = useLocation();
    const usuario = authService.getUsuarioAtual();

    const menuItems = [
        { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
        { label: 'Empresas', path: '/empresas', icon: Building2 },
        { label: 'Documentos', path: '/documentos', icon: FileText },
        { label: 'Obrigações Fiscais', path: '/obrigacoes', icon: Receipt },
        { label: 'Relatórios', path: '/relatorios', icon: BarChart3 },
        { label: 'Notificações', path: '/notificacoes', icon: Bell },
    ];

    const handleLogout = () => {
        authService.logout();
        navigate('/login');
    };

    return (
        <div style={styles.container}>
            {/* Sidebar Lateral */}
            <aside style={styles.sidebar}>
                <div style={styles.logoArea}>
                    <h2 style={styles.logoTitle}>Consultoria</h2>
                </div>

                <nav style={styles.nav}>
                    {menuItems.map((item) => {
                        const Icon = item.icon;
                        const isActive = location.pathname === item.path;

                        return (
                            <Link
                                key={item.path}
                                to={item.path}
                                style={{
                                    ...styles.navLink,
                                    ...(isActive ? styles.navLinkActive : {}),
                                }}
                            >
                                <Icon size={20} />
                                <span>{item.label}</span>
                            </Link>
                        );
                    })}
                </nav>

                <div style={styles.userArea}>
                    <div style={styles.userInfo}>
                        <p style={styles.userName}>{usuario?.email || 'Usuário'}</p>
                        <span style={styles.userRole}>{usuario?.tipo?.toUpperCase() || 'ACESSO'}</span>
                    </div>
                    <button onClick={handleLogout} style={styles.logoutBtn} title="Sair">
                        <LogOut size={18} />
                    </button>
                </div>
            </aside>

            {/* Conteúdo Principal */}
            <main style={styles.mainContent}>
                {children}
            </main>
        </div>
    );
};

const styles: { [key: string]: React.CSSProperties } = {
    container: {
        display: 'flex',
        minHeight: '100vh',
        backgroundColor: '#ffffff',
        fontFamily: 'sans-serif',
    },
    sidebar: {
        width: '260px',
        backgroundColor: '#1e293b',
        color: '#ffffff',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        padding: '1.5rem 1rem',
    },
    logoArea: {
        paddingBottom: '1.5rem',
        borderBottom: '1px solid #334155',
        marginBottom: '1.5rem',
    },
    logoTitle: {
        margin: 0,
        fontSize: '1.25rem',
        fontWeight: 'bold',
        color: '#38bdf8',
    },
    nav: {
        display: 'flex',
        flexDirection: 'column',
        gap: '0.5rem',
        flex: 1,
    },
    navLink: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem',
        padding: '0.75rem 1rem',
        color: '#ffffff',
        textDecoration: 'none',
        borderRadius: '8px',
        fontSize: '0.95rem',
        fontWeight: 500,
    },
    navLinkActive: {
        backgroundColor: '#0284c7',
        color: '#ffffff',
    },
    userArea: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingTop: '1rem',
        borderTop: '1px solid #334155',
    },
    userInfo: {
        overflow: 'hidden',
    },
    userName: {
        margin: 0,
        fontSize: '0.85rem',
        fontWeight: 'bold',
        color: '#f8fafc',
        whiteSpace: 'nowrap',
        textOverflow: 'ellipsis',
    },
    userRole: {
        fontSize: '0.7rem',
        color: '#38bdf8',
    },
    logoutBtn: {
        background: 'transparent',
        border: 'none',
        color: '#ef4444',
        cursor: 'pointer',
        padding: '0.4rem',
    },
    mainContent: {
        flex: 1,
        padding: '2rem',
        overflowY: 'auto',
    },
};