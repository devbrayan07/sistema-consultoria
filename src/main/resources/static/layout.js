document.addEventListener("DOMContentLoaded", () => {

    const appLayout = document.getElementById("app-layout");

    if (!appLayout) {
        return;
    }

    /*
    ============================================================
    USUÁRIO LOGADO
    ============================================================
    */

    const usuario = obterUsuario();

    if (!usuario) {
        console.warn("Usuário não encontrado pelo layout.");
        return;
    }

    const tipoUsuario = normalizarTipo(usuario.tipo);
    const paginaAtual = obterPaginaAtual();

    /*
    ============================================================
    PRESERVA CONTEÚDO DA PÁGINA
    ============================================================
    */

    const conteudoPagina = appLayout.innerHTML;

    /*
    ============================================================
    CSS DO LAYOUT
    ============================================================
    */

    criarEstilosLayout();

    /*
    ============================================================
    MONTA LAYOUT
    ============================================================
    */

    appLayout.innerHTML = `
        <div class="sc-layout">

            <aside class="sc-sidebar" id="sc-sidebar">

                <div class="sc-sidebar-brand">

                    <a href="dashboard.html" class="sc-brand-link">

                        <div class="sc-brand-logo">
                            SC
                        </div>

                        <div class="sc-brand-info">
                            <strong>Consultoria</strong>
                            <span>Gestão empresarial</span>
                        </div>

                    </a>

                    <button
                        type="button"
                        id="sc-sidebar-close"
                        class="sc-sidebar-close"
                        aria-label="Fechar menu"
                    >
                        ×
                    </button>

                </div>


                <div class="sc-user-card">

                    <div class="sc-user-avatar">
                        ${escapeHtml(obterIniciais(usuario.nome))}
                    </div>

                    <div class="sc-user-info">
                        <strong>
                            ${escapeHtml(usuario.nome || "Usuário")}
                        </strong>

                        <span>
                            ${escapeHtml(nomePerfil(tipoUsuario))}
                        </span>
                    </div>

                </div>


                <div class="sc-menu-title">
                    Menu principal
                </div>


                <nav class="sc-menu">
                    ${montarMenu(tipoUsuario, paginaAtual)}
                </nav>


                <div class="sc-sidebar-footer">

                    <button
                        type="button"
                        id="sc-btn-sair"
                        class="sc-logout"
                    >
                        ${iconeLogout()}

                        <span>
                            Sair do sistema
                        </span>
                    </button>

                </div>

            </aside>


            <div
                id="sc-sidebar-overlay"
                class="sc-sidebar-overlay"
            ></div>


            <div class="sc-main">

                <header class="sc-topbar">

                    <div class="sc-topbar-left">

                        <button
                            type="button"
                            id="sc-menu-button"
                            class="sc-mobile-menu-button"
                            aria-label="Abrir menu"
                        >
                            ${iconeMenu()}
                        </button>


                        <div class="sc-page-info">

                            <span>
                                Sistema de Consultoria
                            </span>

                            <strong>
                                ${escapeHtml(obterTituloPagina(paginaAtual))}
                            </strong>

                        </div>

                    </div>


                    <div class="sc-topbar-right">

                        <div class="sc-current-date">
                            ${escapeHtml(formatarDataAtual())}
                        </div>


                        <button
                            type="button"
                            class="sc-notification-button"
                            title="Notificações"
                        >
                            ${iconeSino()}

                            <span
                                id="sc-notification-count"
                                class="sc-notification-count sc-hidden"
                            >
                                0
                            </span>
                        </button>


                        <div class="sc-account">

                            <button
                                type="button"
                                id="sc-account-button"
                                class="sc-account-button"
                            >

                                <div class="sc-account-avatar">
                                    ${escapeHtml(obterIniciais(usuario.nome))}
                                </div>

                                <div class="sc-account-text">

                                    <strong>
                                        ${escapeHtml(primeiroNome(usuario.nome))}
                                    </strong>

                                    <span>
                                        ${escapeHtml(nomePerfil(tipoUsuario))}
                                    </span>

                                </div>

                                ${iconeChevron()}

                            </button>


                            <div
                                id="sc-account-dropdown"
                                class="sc-account-dropdown sc-hidden"
                            >

                                <div class="sc-dropdown-profile">

                                    <div class="sc-dropdown-avatar">
                                        ${escapeHtml(obterIniciais(usuario.nome))}
                                    </div>

                                    <div>
                                        <strong>
                                            ${escapeHtml(usuario.nome || "Usuário")}
                                        </strong>

                                        <span>
                                            ${escapeHtml(usuario.email || "")}
                                        </span>
                                    </div>

                                </div>


                                <div class="sc-dropdown-separator"></div>


                                <a
                                    href="dashboard.html"
                                    class="sc-dropdown-item"
                                >
                                    ${iconeDashboard()}

                                    <span>
                                        Minha área
                                    </span>
                                </a>


                                <button
                                    type="button"
                                    id="sc-dropdown-sair"
                                    class="sc-dropdown-item sc-dropdown-logout"
                                >
                                    ${iconeLogout()}

                                    <span>
                                        Sair
                                    </span>
                                </button>

                            </div>

                        </div>

                    </div>

                </header>


                <main class="sc-content">
                    ${conteudoPagina}
                </main>

            </div>

        </div>
    `;


    /*
    ============================================================
    EVENTOS
    ============================================================
    */

    configurarSidebar();
    configurarDropdown();
    configurarLogout();


    /*
    ============================================================
    MENU
    ============================================================
    */

    function montarMenu(tipo, pagina) {

        let itens = [];


        if (tipo === "USUARIO") {

            itens = [
                {
                    nome: "Visão geral",
                    href: "dashboard.html",
                    pagina: "dashboard.html",
                    icone: iconeDashboard()
                },
                {
                    nome: "Minha empresa",
                    href: "empresas.html",
                    pagina: "empresas.html",
                    icone: iconeEmpresa()
                },
                {
                    nome: "Obrigações",
                    href: "obrigacoes.html",
                    pagina: "obrigacoes.html",
                    icone: iconeObrigacoes()
                },
                {
                    nome: "Documentos",
                    href: "documentos.html",
                    pagina: "documentos.html",
                    icone: iconeDocumentos()
                }
            ];

        } else if (tipo === "CONTADOR") {

            itens = [
                {
                    nome: "Visão geral",
                    href: "dashboard.html",
                    pagina: "dashboard.html",
                    icone: iconeDashboard()
                },
                {
                    nome: "Empresas",
                    href: "empresas.html",
                    pagina: "empresas.html",
                    icone: iconeEmpresa()
                },
                {
                    nome: "Obrigações",
                    href: "obrigacoes.html",
                    pagina: "obrigacoes.html",
                    icone: iconeObrigacoes()
                },
                {
                    nome: "Documentos",
                    href: "documentos.html",
                    pagina: "documentos.html",
                    icone: iconeDocumentos()
                }
            ];

        } else if (tipo === "ADMINISTRADOR") {

            itens = [
                {
                    nome: "Visão geral",
                    href: "dashboard.html",
                    pagina: "dashboard.html",
                    icone: iconeDashboard()
                },
                {
                    nome: "Empresas",
                    href: "empresas.html",
                    pagina: "empresas.html",
                    icone: iconeEmpresa()
                },
                {
                    nome: "Obrigações",
                    href: "obrigacoes.html",
                    pagina: "obrigacoes.html",
                    icone: iconeObrigacoes()
                },
                {
                    nome: "Documentos",
                    href: "documentos.html",
                    pagina: "documentos.html",
                    icone: iconeDocumentos()
                },
                {
                    separador: true,
                    nome: "Administração"
                },
                {
                    nome: "Usuários",
                    href: "usuarios.html",
                    pagina: "usuarios.html",
                    icone: iconeUsuarios()
                }
            ];

        } else {

            itens = [
                {
                    nome: "Visão geral",
                    href: "dashboard.html",
                    pagina: "dashboard.html",
                    icone: iconeDashboard()
                }
            ];
        }


        return itens
            .map(item => {

                if (item.separador) {

                    return `
                        <div class="sc-menu-section">
                            ${escapeHtml(item.nome)}
                        </div>
                    `;
                }


                const ativo =
                    pagina === item.pagina;


                return `
                    <a
                        href="${item.href}"
                        class="sc-menu-item ${ativo ? "active" : ""}"
                    >

                        <span class="sc-menu-icon">
                            ${item.icone}
                        </span>

                        <span>
                            ${escapeHtml(item.nome)}
                        </span>

                    </a>
                `;

            })
            .join("");
    }


    /*
    ============================================================
    SIDEBAR MOBILE
    ============================================================
    */

    function configurarSidebar() {

        const sidebar =
            document.getElementById("sc-sidebar");

        const overlay =
            document.getElementById("sc-sidebar-overlay");

        const btnAbrir =
            document.getElementById("sc-menu-button");

        const btnFechar =
            document.getElementById("sc-sidebar-close");


        function abrir() {

            sidebar.classList.add("open");
            overlay.classList.add("visible");

            document.body.classList.add(
                "sc-body-lock"
            );
        }


        function fechar() {

            sidebar.classList.remove("open");
            overlay.classList.remove("visible");

            document.body.classList.remove(
                "sc-body-lock"
            );
        }


        btnAbrir?.addEventListener(
            "click",
            abrir
        );

        btnFechar?.addEventListener(
            "click",
            fechar
        );

        overlay?.addEventListener(
            "click",
            fechar
        );


        window.addEventListener(
            "resize",
            () => {

                if (window.innerWidth > 900) {
                    fechar();
                }

            }
        );
    }


    /*
    ============================================================
    DROPDOWN USUÁRIO
    ============================================================
    */

    function configurarDropdown() {

        const button =
            document.getElementById(
                "sc-account-button"
            );

        const dropdown =
            document.getElementById(
                "sc-account-dropdown"
            );


        if (!button || !dropdown) {
            return;
        }


        button.addEventListener(
            "click",
            event => {

                event.stopPropagation();

                dropdown.classList.toggle(
                    "sc-hidden"
                );
            }
        );


        dropdown.addEventListener(
            "click",
            event => {
                event.stopPropagation();
            }
        );


        document.addEventListener(
            "click",
            () => {

                dropdown.classList.add(
                    "sc-hidden"
                );
            }
        );


        document.addEventListener(
            "keydown",
            event => {

                if (event.key === "Escape") {

                    dropdown.classList.add(
                        "sc-hidden"
                    );
                }
            }
        );
    }


    /*
    ============================================================
    LOGOUT
    ============================================================
    */

    function configurarLogout() {

        document
            .getElementById("sc-btn-sair")
            ?.addEventListener(
                "click",
                executarLogout
            );


        document
            .getElementById("sc-dropdown-sair")
            ?.addEventListener(
                "click",
                executarLogout
            );
    }


    function executarLogout() {

        if (
            window.auth &&
            typeof window.auth.logout === "function"
        ) {

            window.auth.logout();
            return;
        }


        if (
            window.auth &&
            typeof window.auth.sair === "function"
        ) {

            window.auth.sair();
            return;
        }


        localStorage.removeItem("token");
        localStorage.removeItem("usuario");
        localStorage.removeItem("user");

        sessionStorage.clear();

        window.location.href =
            "index.html";
    }


    /*
    ============================================================
    USUÁRIO
    ============================================================
    */

    function obterUsuario() {

        if (window.auth) {

            if (
                typeof window.auth.obterUsuario ===
                "function"
            ) {

                const usuario =
                    window.auth.obterUsuario();

                if (usuario) {
                    return usuario;
                }
            }


            if (
                typeof window.auth.getUsuario ===
                "function"
            ) {

                const usuario =
                    window.auth.getUsuario();

                if (usuario) {
                    return usuario;
                }
            }


            if (
                typeof window.auth.getUser ===
                "function"
            ) {

                const usuario =
                    window.auth.getUser();

                if (usuario) {
                    return usuario;
                }
            }
        }


        const possiveisChaves = [
            "usuario",
            "user"
        ];


        for (const chave of possiveisChaves) {

            const valor =
                localStorage.getItem(chave);

            if (!valor) {
                continue;
            }


            try {

                const objeto =
                    JSON.parse(valor);

                if (objeto) {
                    return objeto;
                }

            } catch {
                // ignora
            }
        }


        const nome =
            localStorage.getItem("nome");

        const email =
            localStorage.getItem("email");

        const tipo =
            localStorage.getItem("tipo");


        if (nome || email || tipo) {

            return {
                nome,
                email,
                tipo
            };
        }


        return null;
    }


    function normalizarTipo(tipo) {

        if (!tipo) {
            return "USUARIO";
        }


        let valor =
            String(tipo)
                .trim()
                .toUpperCase();


        if (valor.startsWith("ROLE_")) {

            valor =
                valor.substring(5);
        }


        if (valor === "CLIENTE") {
            return "USUARIO";
        }

        if (valor === "EQUIPE") {
            return "CONTADOR";
        }


        return valor;
    }


    function nomePerfil(tipo) {

        switch (tipo) {

            case "ADMINISTRADOR":
                return "Administrador";

            case "CONTADOR":
                return "Contador";

            case "USUARIO":
                return "Cliente";

            default:
                return "Usuário";
        }
    }


    /*
    ============================================================
    PÁGINAS
    ============================================================
    */

    function obterPaginaAtual() {

        let pagina =
            window.location.pathname
                .split("/")
                .pop();


        if (!pagina) {
            pagina = "dashboard.html";
        }


        return pagina.toLowerCase();
    }


    function obterTituloPagina(pagina) {

        const titulos = {

            "dashboard.html":
                "Visão geral",

            "empresas.html":
                tipoUsuario === "USUARIO"
                    ? "Minha empresa"
                    : "Empresas",

            "obrigacoes.html":
                "Obrigações",

            "documentos.html":
                "Documentos",

            "usuarios.html":
                "Usuários"

        };


        return titulos[pagina]
            || "Sistema";
    }


    /*
    ============================================================
    TEXTO / FORMATAÇÃO
    ============================================================
    */

    function primeiroNome(nome) {

        if (!nome) {
            return "Usuário";
        }


        return String(nome)
            .trim()
            .split(/\s+/)[0];
    }


    function obterIniciais(nome) {

        if (!nome) {
            return "US";
        }


        const partes =
            String(nome)
                .trim()
                .split(/\s+/)
                .filter(Boolean);


        if (!partes.length) {
            return "US";
        }


        if (partes.length === 1) {

            return partes[0]
                .substring(0, 2)
                .toUpperCase();
        }


        return (
            partes[0][0]
            +
            partes[partes.length - 1][0]
        ).toUpperCase();
    }


    function formatarDataAtual() {

        const data =
            new Date();


        const diaSemana =
            new Intl.DateTimeFormat(
                "pt-BR",
                {
                    weekday: "short"
                }
            ).format(data);


        const dia =
            String(
                data.getDate()
            ).padStart(2, "0");


        const mes =
            new Intl.DateTimeFormat(
                "pt-BR",
                {
                    month: "short"
                }
            ).format(data);


        return `${diaSemana}, ${dia} de ${mes}`;
    }


    function escapeHtml(valor) {

        if (
            valor === null ||
            valor === undefined
        ) {
            return "";
        }


        return String(valor)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }


    /*
    ============================================================
    ESTILOS
    ============================================================
    */

    function criarEstilosLayout() {

        if (
            document.getElementById(
                "sc-layout-styles"
            )
        ) {
            return;
        }


        const style =
            document.createElement("style");


        style.id =
            "sc-layout-styles";


        style.textContent = `

            * {
                box-sizing: border-box;
            }

            body {
                margin: 0;
            }

            .sc-layout {
                min-height: 100vh;
                display: flex;
                background: #f6f8fc;
            }


            /* ================================================
               SIDEBAR
            ================================================= */

            .sc-sidebar {
                width: 260px;
                min-width: 260px;
                height: 100vh;

                position: fixed;
                top: 0;
                left: 0;
                z-index: 1000;

                display: flex;
                flex-direction: column;

                padding: 24px 18px;

                background: #ffffff;

                border-right:
                    1px solid #e8edf5;
            }


            .sc-sidebar-brand {
                min-height: 54px;

                display: flex;
                align-items: center;
                justify-content: space-between;
            }


            .sc-brand-link {
                display: flex;
                align-items: center;
                gap: 12px;

                text-decoration: none;

                color: inherit;
            }


            .sc-brand-logo {
                width: 42px;
                height: 42px;

                display: flex;
                align-items: center;
                justify-content: center;

                border-radius: 12px;

                background:
                    linear-gradient(
                        135deg,
                        #2156d9,
                        #3478f6
                    );

                color: #ffffff;

                font-size: 13px;
                font-weight: 800;
            }


            .sc-brand-info {
                display: flex;
                flex-direction: column;
            }


            .sc-brand-info strong {
                color: #17233a;

                font-size: 15px;
                font-weight: 800;
            }


            .sc-brand-info span {
                margin-top: 2px;

                color: #98a3b5;

                font-size: 11px;
            }


            .sc-sidebar-close {
                display: none;

                width: 34px;
                height: 34px;

                border: 0;
                border-radius: 8px;

                background: transparent;

                color: #718096;

                cursor: pointer;

                font-size: 24px;
            }


            /* ================================================
               USUÁRIO SIDEBAR
            ================================================= */

            .sc-user-card {
                display: flex;
                align-items: center;
                gap: 11px;

                margin-top: 29px;
                padding: 12px;

                border-radius: 12px;

                background: #f7f9fd;
            }


            .sc-user-avatar,
            .sc-account-avatar,
            .sc-dropdown-avatar {
                display: flex;
                align-items: center;
                justify-content: center;

                flex-shrink: 0;

                color: #245bd8;

                background: #eaf1ff;

                font-weight: 800;
            }


            .sc-user-avatar {
                width: 38px;
                height: 38px;

                border-radius: 10px;

                font-size: 12px;
            }


            .sc-user-info {
                min-width: 0;

                display: flex;
                flex-direction: column;
            }


            .sc-user-info strong {
                overflow: hidden;

                color: #1a273c;

                font-size: 12px;
                font-weight: 800;

                text-overflow: ellipsis;
                white-space: nowrap;
            }


            .sc-user-info span {
                margin-top: 3px;

                color: #8b98ab;

                font-size: 10px;
            }


            /* ================================================
               MENU
            ================================================= */

            .sc-menu-title,
            .sc-menu-section {
                margin:
                    27px 10px 10px;

                color: #9aa6b7;

                font-size: 10px;
                font-weight: 800;

                letter-spacing: 0.8px;

                text-transform: uppercase;
            }


            .sc-menu {
                display: flex;
                flex-direction: column;
                gap: 4px;
            }


            .sc-menu-item {
                min-height: 43px;

                display: flex;
                align-items: center;
                gap: 12px;

                padding:
                    0 12px;

                border-radius: 10px;

                color: #64748b;

                text-decoration: none;

                font-size: 13px;
                font-weight: 600;

                transition:
                    background 0.18s ease,
                    color 0.18s ease;
            }


            .sc-menu-item:hover {
                color: #2358d4;

                background: #f4f7fd;
            }


            .sc-menu-item.active {
                color: #245cdb;

                background: #edf3ff;

                font-weight: 800;
            }


            .sc-menu-icon {
                width: 20px;
                height: 20px;

                display: flex;
                align-items: center;
                justify-content: center;
            }


            .sc-menu-item.active .sc-menu-icon {
                color: #245cdb;
            }


            /* ================================================
               LOGOUT
            ================================================= */

            .sc-sidebar-footer {
                margin-top: auto;

                padding-top: 20px;

                border-top:
                    1px solid #edf0f5;
            }


            .sc-logout {
                width: 100%;
                min-height: 42px;

                display: flex;
                align-items: center;
                gap: 11px;

                padding:
                    0 11px;

                border: 0;
                border-radius: 9px;

                color: #e04949;

                background: transparent;

                cursor: pointer;

                font-size: 12px;
                font-weight: 700;

                transition:
                    background 0.18s ease;
            }


            .sc-logout:hover {
                background: #fff4f4;
            }


            /* ================================================
               MAIN
            ================================================= */

            .sc-main {
                width: calc(100% - 260px);

                min-height: 100vh;

                margin-left: 260px;
            }


            /* ================================================
               TOPBAR
            ================================================= */

            .sc-topbar {
                height: 72px;

                position: sticky;
                top: 0;
                z-index: 900;

                display: flex;
                align-items: center;
                justify-content: space-between;

                padding:
                    0 32px;

                background:
                    rgba(255,255,255,0.95);

                border-bottom:
                    1px solid #e8edf4;

                backdrop-filter:
                    blur(12px);
            }


            .sc-topbar-left {
                display: flex;
                align-items: center;
                gap: 14px;
            }


            .sc-page-info {
                display: flex;
                flex-direction: column;
            }


            .sc-page-info span {
                margin-bottom: 2px;

                color: #98a4b5;

                font-size: 10px;
            }


            .sc-page-info strong {
                color: #162238;

                font-size: 14px;
                font-weight: 800;
            }


            .sc-mobile-menu-button {
                display: none;

                width: 38px;
                height: 38px;

                border:
                    1px solid #e5e9f0;

                border-radius: 9px;

                color: #536176;

                background: #ffffff;

                cursor: pointer;
            }


            .sc-topbar-right {
                display: flex;
                align-items: center;
                gap: 15px;
            }


            .sc-current-date {
                padding-right: 4px;

                color: #8995a7;

                font-size: 11px;
            }


            .sc-notification-button {
                width: 38px;
                height: 38px;

                position: relative;

                display: flex;
                align-items: center;
                justify-content: center;

                border:
                    1px solid #e7ebf1;

                border-radius: 10px;

                color: #64748b;

                background: #ffffff;

                cursor: pointer;
            }


            .sc-notification-count {
                min-width: 16px;
                height: 16px;

                position: absolute;
                top: -5px;
                right: -5px;

                display: flex;
                align-items: center;
                justify-content: center;

                padding: 0 4px;

                border:
                    2px solid #ffffff;

                border-radius: 999px;

                color: white;

                background: #ef4444;

                font-size: 8px;
                font-weight: 800;
            }


            /* ================================================
               CONTA
            ================================================= */

            .sc-account {
                position: relative;
            }


            .sc-account-button {
                min-height: 45px;

                display: flex;
                align-items: center;
                gap: 10px;

                padding:
                    5px 10px 5px 6px;

                border:
                    1px solid transparent;

                border-radius: 11px;

                color: inherit;

                background: transparent;

                cursor: pointer;

                transition:
                    background 0.18s ease;
            }


            .sc-account-button:hover {
                background: #f6f8fc;
            }


            .sc-account-avatar {
                width: 34px;
                height: 34px;

                border-radius: 9px;

                font-size: 10px;
            }


            .sc-account-text {
                min-width: 105px;

                display: flex;
                flex-direction: column;

                text-align: left;
            }


            .sc-account-text strong {
                overflow: hidden;

                color: #1e293b;

                font-size: 11px;
                font-weight: 800;

                text-overflow: ellipsis;
                white-space: nowrap;
            }


            .sc-account-text span {
                margin-top: 2px;

                color: #94a0b1;

                font-size: 9px;
            }


            .sc-account-button > svg {
                color: #9aa6b6;
            }


            /* ================================================
               DROPDOWN
            ================================================= */

            .sc-account-dropdown {
                width: 250px;

                position: absolute;
                top: calc(100% + 10px);
                right: 0;

                padding: 10px;

                border:
                    1px solid #e5eaf1;

                border-radius: 13px;

                background: #ffffff;

                box-shadow:
                    0 15px 40px rgba(15,23,42,0.12);
            }


            .sc-dropdown-profile {
                display: flex;
                align-items: center;
                gap: 10px;

                padding: 8px;
            }


            .sc-dropdown-avatar {
                width: 38px;
                height: 38px;

                border-radius: 10px;

                font-size: 11px;
            }


            .sc-dropdown-profile > div:last-child {
                min-width: 0;

                display: flex;
                flex-direction: column;
            }


            .sc-dropdown-profile strong {
                color: #1e293b;

                font-size: 11px;
            }


            .sc-dropdown-profile span {
                overflow: hidden;

                margin-top: 3px;

                color: #94a3b8;

                font-size: 9px;

                text-overflow: ellipsis;
                white-space: nowrap;
            }


            .sc-dropdown-separator {
                height: 1px;

                margin: 7px 4px;

                background: #edf0f5;
            }


            .sc-dropdown-item {
                width: 100%;
                min-height: 38px;

                display: flex;
                align-items: center;
                gap: 10px;

                padding:
                    0 10px;

                border: 0;
                border-radius: 8px;

                color: #556277;

                background: transparent;

                text-decoration: none;

                cursor: pointer;

                font-size: 11px;
                font-weight: 600;
            }


            .sc-dropdown-item:hover {
                background: #f6f8fc;
            }


            .sc-dropdown-logout {
                color: #dc4545;
            }


            /* ================================================
               CONTEÚDO
            ================================================= */

            .sc-content {
                width: 100%;

                padding:
                    28px 32px 50px;

                overflow-x: hidden;
            }


            .sc-hidden {
                display: none !important;
            }


            .sc-sidebar-overlay {
                display: none;
            }


            /* ================================================
               TABLET / MOBILE
            ================================================= */

            @media (max-width: 900px) {

                .sc-sidebar {
                    transform:
                        translateX(-100%);

                    transition:
                        transform 0.25s ease;

                    box-shadow:
                        15px 0 40px rgba(15,23,42,0.12);
                }


                .sc-sidebar.open {
                    transform:
                        translateX(0);
                }


                .sc-sidebar-close {
                    display: block;
                }


                .sc-sidebar-overlay {
                    position: fixed;
                    inset: 0;
                    z-index: 999;

                    background:
                        rgba(15,23,42,0.35);
                }


                .sc-sidebar-overlay.visible {
                    display: block;
                }


                .sc-main {
                    width: 100%;

                    margin-left: 0;
                }


                .sc-mobile-menu-button {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }


                .sc-topbar {
                    padding:
                        0 20px;
                }


                .sc-current-date {
                    display: none;
                }


                .sc-content {
                    padding:
                        24px 20px 45px;
                }


                .sc-body-lock {
                    overflow: hidden;
                }
            }


            @media (max-width: 600px) {

                .sc-topbar {
                    height: 66px;

                    padding:
                        0 14px;
                }


                .sc-page-info span {
                    display: none;
                }


                .sc-account-text {
                    display: none;
                }


                .sc-account-button {
                    min-width: auto;

                    padding:
                        5px;
                }


                .sc-account-button > svg {
                    display: none;
                }


                .sc-content {
                    padding:
                        18px 14px 40px;
                }
            }

        `;


        document.head.appendChild(style);
    }


    /*
    ============================================================
    ÍCONES
    ============================================================
    */

    function iconeMenu() {

        return `
            <svg
                width="19"
                height="19"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
            >
                <path d="M4 6h16"/>
                <path d="M4 12h16"/>
                <path d="M4 18h16"/>
            </svg>
        `;
    }


    function iconeDashboard() {

        return `
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
            >
                <rect x="3" y="3" width="7" height="7" rx="2"/>
                <rect x="14" y="3" width="7" height="7" rx="2"/>
                <rect x="3" y="14" width="7" height="7" rx="2"/>
                <rect x="14" y="14" width="7" height="7" rx="2"/>
            </svg>
        `;
    }


    function iconeEmpresa() {

        return `
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
            >
                <path d="M3 21h18"/>
                <path d="M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16"/>
                <path d="M9 7h2"/>
                <path d="M13 7h2"/>
                <path d="M9 11h2"/>
                <path d="M13 11h2"/>
            </svg>
        `;
    }


    function iconeObrigacoes() {

        return `
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
            >
                <rect
                    x="4"
                    y="3"
                    width="16"
                    height="18"
                    rx="2"
                />

                <path d="M8 8h8"/>
                <path d="M8 12h8"/>
                <path d="M8 16h5"/>
            </svg>
        `;
    }


    function iconeDocumentos() {

        return `
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linejoin="round"
            >
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <path d="M14 2v6h6"/>
                <path d="M8 13h8"/>
                <path d="M8 17h6"/>
            </svg>
        `;
    }


    function iconeUsuarios() {

        return `
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
            >
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
        `;
    }


    function iconeLogout() {

        return `
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
            >
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <path d="m16 17 5-5-5-5"/>
                <path d="M21 12H9"/>
            </svg>
        `;
    }


    function iconeSino() {

        return `
            <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
            >
                <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
        `;
    }


    function iconeChevron() {

        return `
            <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
            >
                <path d="m6 9 6 6 6-6"/>
            </svg>
        `;
    }

});