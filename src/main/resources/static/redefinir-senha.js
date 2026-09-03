document.addEventListener(
    'DOMContentLoaded',
    () => {

        const form =
            document.getElementById(
                'form-redefinir-senha'
            );

        const inputNovaSenha =
            document.getElementById(
                'novaSenha'
            );

        const inputConfirmarSenha =
            document.getElementById(
                'confirmarSenha'
            );

        const alertError =
            document.getElementById(
                'error-alert'
            );

        const alertSuccess =
            document.getElementById(
                'success-alert'
            );

        const btn =
            document.getElementById(
                'btn-redefinir'
            );

        const btnText =
            document.getElementById(
                'btn-redefinir-text'
            );


        if (!form) {
            return;
        }


        const parametros =
            new URLSearchParams(
                window.location.search
            );

        const token =
            parametros.get('token');


        if (!token) {

            mostrarErro(
                'Link de recuperação inválido.'
            );

            btn.disabled = true;

            return;
        }


        form.addEventListener(
            'submit',
            async event => {

                event.preventDefault();

                ocultarMensagens();


                const novaSenha =
                    inputNovaSenha?.value || '';

                const confirmarSenha =
                    inputConfirmarSenha?.value || '';


                if (
                    novaSenha.length < 8
                ) {

                    mostrarErro(
                        'A senha deve possuir pelo menos 8 caracteres.'
                    );

                    return;
                }


                if (
                    novaSenha !==
                    confirmarSenha
                ) {

                    mostrarErro(
                        'As senhas não coincidem.'
                    );

                    return;
                }


                alterarCarregamento(
                    true
                );


                try {

                    await window.api.post(
                        '/auth/redefinir-senha',
                        {
                            token,
                            novaSenha
                        }
                    );


                    mostrarSucesso(
                        'Senha redefinida com sucesso. Você será redirecionado para o login.'
                    );


                    form.reset();


                    setTimeout(
                        () => {

                            window.location.replace(
                                '/index.html'
                            );

                        },
                        2000
                    );


                } catch (erro) {

                    mostrarErro(
                        erro.message ||
                        'Não foi possível redefinir sua senha.'
                    );

                } finally {

                    alterarCarregamento(
                        false
                    );
                }
            }
        );


        function alterarCarregamento(
            carregando
        ) {

            if (btn) {
                btn.disabled = carregando;
            }


            if (btnText) {

                btnText.textContent =
                    carregando
                        ? 'Salvando...'
                        : 'Redefinir senha';
            }
        }


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


        function mostrarSucesso(
            mensagem
        ) {

            if (!alertSuccess) {
                return;
            }

            alertSuccess.textContent =
                mensagem;

            alertSuccess.style.display =
                'block';
        }


        function ocultarMensagens() {

            if (alertError) {

                alertError.textContent = '';

                alertError.style.display =
                    'none';
            }


            if (alertSuccess) {

                alertSuccess.textContent = '';

                alertSuccess.style.display =
                    'none';
            }
        }

    }
);