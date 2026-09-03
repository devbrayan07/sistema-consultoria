document.addEventListener(
    'DOMContentLoaded',
    () => {

        const form =
            document.getElementById(
                'form-recuperar-senha'
            );

        const inputEmail =
            document.getElementById(
                'email'
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
                'btn-recuperar'
            );

        const btnText =
            document.getElementById(
                'btn-recuperar-text'
            );


        if (!form) {
            return;
        }


        form.addEventListener(
            'submit',
            async event => {

                event.preventDefault();

                ocultarMensagens();


                const email =
                    inputEmail
                        ?.value
                        .trim();


                if (!email) {

                    mostrarErro(
                        'Informe seu e-mail.'
                    );

                    return;
                }


                alterarCarregamento(
                    true
                );


                try {

                    await window.api.post(
                        '/auth/esqueci-senha',
                        {
                            email
                        }
                    );


                    mostrarSucesso(
                        'Se o e-mail estiver cadastrado, enviaremos as instruções para redefinir sua senha.'
                    );


                    if (inputEmail) {
                        inputEmail.value = '';
                    }

                } catch (erro) {

                    mostrarErro(
                        erro.message ||
                        'Não foi possível solicitar a recuperação da senha.'
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
                        ? 'Enviando...'
                        : 'Enviar instruções';
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