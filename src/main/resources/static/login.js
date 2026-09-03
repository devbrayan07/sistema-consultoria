document.addEventListener(
    'DOMContentLoaded',
    () => {

        /*
         * =====================================================
         * ELEMENTOS
         * =====================================================
         */

        const form =
            document.getElementById(
                'form-login'
            );

        const inputEmail =
            document.getElementById(
                'email'
            );

        const inputSenha =
            document.getElementById(
                'senha'
            );

        const alertError =
            document.getElementById(
                'error-alert'
            );

        const btnLogin =
            document.getElementById(
                'btn-login'
            );

        const btnLoginText =
            document.getElementById(
                'btn-login-text'
            );

        const btnToggleSenha =
            document.getElementById(
                'btn-toggle-senha'
            );


        if (!form) {
            return;
        }


        /*
         * =====================================================
         * LOGIN
         * =====================================================
         */

        form.addEventListener(
            'submit',
            async event => {

                event.preventDefault();

                ocultarErro();


                const email =
                    inputEmail
                        ?.value
                        .trim();


                const senha =
                    inputSenha
                        ?.value;


                if (
                    !email
                    ||
                    !senha
                ) {

                    mostrarErro(
                        'Informe o e-mail e a senha.'
                    );

                    return;
                }


                alterarEstadoLogin(
                    true
                );


                try {

                    const data =
                        await window.api.post(
                            '/auth/login',
                            {
                                email,
                                senha
                            }
                        );


                    window.auth.setSessao(
                        data
                    );


                    window.location.replace(
                        '/dashboard'
                    );

                } catch (erro) {

                    mostrarErro(
                        erro.message
                        ||
                        'Falha ao autenticar.'
                    );

                } finally {

                    alterarEstadoLogin(
                        false
                    );
                }
            }
        );


        /*
         * =====================================================
         * MOSTRAR / OCULTAR SENHA
         * =====================================================
         */

        btnToggleSenha?.addEventListener(
            'click',
            () => {

                if (!inputSenha) {
                    return;
                }


                const mostrando =
                    inputSenha.type === 'text';


                inputSenha.type =
                    mostrando
                        ? 'password'
                        : 'text';


                btnToggleSenha.setAttribute(
                    'aria-label',
                    mostrando
                        ? 'Mostrar senha'
                        : 'Ocultar senha'
                );


                btnToggleSenha.setAttribute(
                    'title',
                    mostrando
                        ? 'Mostrar senha'
                        : 'Ocultar senha'
                );
            }
        );


        /*
         * =====================================================
         * ESTADO DO BOTÃO
         * =====================================================
         */

        function alterarEstadoLogin(
            carregando
        ) {

            if (!btnLogin) {
                return;
            }


            btnLogin.disabled =
                carregando;


            if (btnLoginText) {

                btnLoginText.textContent =
                    carregando
                        ? 'Entrando...'
                        : 'Entrar no sistema';
            }
        }


        /*
         * =====================================================
         * ERROS
         * =====================================================
         */

        function mostrarErro(
            mensagem
        ) {

            if (!alertError) {
                return;
            }


            alertError.textContent =
                mensagem;


            alertError.style.display =
                'block';
        }


        function ocultarErro() {

            if (!alertError) {
                return;
            }


            alertError.textContent =
                '';


            alertError.style.display =
                'none';
        }
    }
);