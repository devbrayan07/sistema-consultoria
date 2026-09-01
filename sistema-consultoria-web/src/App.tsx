import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Login } from './pages/Login';
import { Layout } from './components/Layout';
import { authService } from './services/authService';
import type {JSX} from "react";
import { Empresas } from './pages/Empresas';
import {ObrigacoesFiscais} from './pages/ObrigacoesFiscais';

const RotaProtegida = ({ children }: { children: JSX.Element }) => {
  return authService.isAutenticado() ? (
      <Layout>{children}</Layout>
  ) : (
      <Navigate to="/login" replace />
  );
};

// Páginas Provisórias para Teste de Navegação
const DashboardPage = () => <h2>📊 Dashboard - Visão Geral</h2>;
const DocumentosPage = () => <h2>📁 Documentos</h2>;
const RelatoriosPage = () => <h2>📈 Relatórios Financeiros</h2>;
const NotificacoesPage = () => <h2>🔔 Central de Notificações</h2>;

export function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />

          {/* Rotas Protegidas com o Layout */}
          <Route path="/dashboard" element={<RotaProtegida><DashboardPage /></RotaProtegida>} />
          <Route path="/empresas" element={<RotaProtegida><Empresas /></RotaProtegida>} />
          <Route path="/documentos" element={<RotaProtegida><DocumentosPage /></RotaProtegida>} />
          <Route path="/obrigacoes" element={<RotaProtegida><ObrigacoesFiscais /></RotaProtegida>} />
          <Route path="/relatorios" element={<RotaProtegida><RelatoriosPage /></RotaProtegida>} />
          <Route path="/notificacoes" element={<RotaProtegida><NotificacoesPage /></RotaProtegida>} />

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
  );
}

export default App;