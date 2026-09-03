document.addEventListener('DOMContentLoaded', () => {

    const form =
        document.getElementById('form-cadastro');

    const nome =
        document.getElementById('cadastro-nome');

    const email =
        document.getElementById('cadastro-email');

    const senha =
        document.getElementById('cadastro-senha');

    const confirmarSenha =
        document.getElementById('cadastro-confirmar-senha');

    const termos =
        document.getElementById('cadastro-termos');


    const alertBox =
        document.getElementById('cadastro-alert');


    const nomeError =
        document.getElementById('cadastro-nome-error');

    const emailError =
        document.getElementById('cadastro-email-error');

    const confirmarError =
        document.getElementById('cadastro-confirmar-error');


    const passwordProgress =
        document.getElementById('cadastro-password-progress');

    const passwordStrength =
        document.getElementById('cadastro-password-strength');


    const submitButton =
        document.getElementById('btn-criar-conta');

    const submitText =
        document.getElementById('cadastro-submit-text');

    const submitLoading =
        document.getElementById('cadastro-submit-loading');


    /*
     * ============================================================
     * MOSTRAR / OCULTAR SENHA
     * ============================================================
     */

    document
        .querySelectorAll('.cadastro-password-toggle')
        .forEach(button => {

            button.addEventListener('click', () => {

                const targetId =
                    button.dataset.target;

                const input =
                    document.getElementById(targetId);

                if (!input) {
                    return;
                }


                if (input.type === 'password') {

                    input.type = 'text';

                    button.textContent = '🙈';

                    button.setAttribute(
                        'aria-label',
                        'Ocultar senha'
                    );

                } else {

                    input.type = 'password';

                    button.textContent = '👁';

                    button.setAttribute(
                        'aria-label',
                        'Mostrar senha'
                    );
                }
            });
        });


    /*
     * ============================================================
     * FORÇA DA SENHA
     * ============================================================
     */

    senha.addEventListener(
        'input',
        atualizarForcaSenha
    );


    function atualizarForcaSenha() {

        const valor =
            senha.value;

        let pontos = 0;


        if (valor.length >= 8) {
            pontos++;
        }

        if (/[A-Z]/.test(valor)) {
            pontos++;
        }

        if (/[a-z]/.test(valor)) {
            pontos++;
        }

        if (/[0-9]/.test(valor)) {
            pontos++;
        }

        if (/[^A-Za-z0-9]/.test(valor)) {
            pontos++;
        }


        let percentual = 0;
        let texto = 'Use pelo menos 8 caracteres.';

        if (valor.length === 0) {

            percentual = 0;

        } else if (pontos <= 1) {

            percentual = 20;
            texto = 'Senha muito fraca';

        } else if (pontos === 2) {

            percentual = 40;
            texto = 'Senha fraca';

        } else if (pontos === 3) {

            percentual = 60;
            texto = 'Senha razoável';

        } else if (pontos === 4) {

            percentual = 80;
            texto = 'Senha forte';

        } else {

            percentual = 100;
            texto = 'Senha muito forte';
        }


        passwordProgress.style.width =
            `${percentual}%`;

        passwordStrength.textContent =
            texto;
    }


    /*
     * ============================================================
     * SUBMIT
     * ============================================================
     */

    form.addEventListener(
        'submit',
        async event => {

            event.preventDefault();

            limparErros();


            if (!validarFormulario()) {
                return;
            }


            setLoading(true);


            try {

                const response =
                    await fetch(
                        '/api/auth/cadastro',
                        {
                            method: 'POST',

                            headers: {
                                'Content-Type':
                                    'application/json'
                            },

                            body: JSON.stringify({

                                nome:
                                    nome.value.trim(),

                                email:
                                    email.value
                                        .trim()
                                        .toLowerCase(),

                                senha:
                                senha.value
                            })
                        }
                    );


                let data = null;


                try {

                    data =
                        await response.json();

                } catch {

                    data = null;
                }


                if (!response.ok) {

                    const mensagem =
                        data?.message
                        ||
                        data?.mensagem
                        ||
                        data?.error
                        ||
                        'Não foi possível criar sua conta.';


                    throw new Error(
                        mensagem
                    );
                }


                mostrarAlerta(
                    'Conta criada com sucesso! Você já pode entrar no sistema.',
                    'success'
                );


                form.reset();

                atualizarForcaSenha();


                setTimeout(() => {

                    window.location.href =
                        '/';

                }, 1300);


            } catch (error) {

                console.error(
                    'Erro ao cadastrar usuário:',
                    error
                );


                mostrarAlerta(
                    error.message
                    ||
                    'Ocorreu um erro ao criar sua conta.',
                    'error'
                );


            } finally {

                setLoading(false);
            }
        }
    );


    /*
     * ============================================================
     * VALIDAÇÕES
     * ============================================================
     */

    function validarFormulario() {

        let valido = true;


        const nomeValor =
            nome.value.trim();

        const emailValor =
            email.value.trim();

        const senhaValor =
            senha.value;

        const confirmarValor =
            confirmarSenha.value;


        if (nomeValor.length < 3) {

            nomeError.textContent =
                'Informe seu nome completo.';

            marcarInvalido(nome);

            valido = false;
        }


        if (!emailValido(emailValor)) {

            emailError.textContent =
                'Informe um endereço de e-mail válido.';

            marcarInvalido(email);

            valido = false;
        }


        if (senhaValor.length < 8) {

            mostrarAlerta(
                'A senha deve possuir pelo menos 8 caracteres.',
                'error'
            );

            marcarInvalido(senha);

            valido = false;
        }


        if (senhaValor !== confirmarValor) {

            confirmarError.textContent =
                'As senhas não coincidem.';

            marcarInvalido(confirmarSenha);

            valido = false;
        }


        if (!termos.checked) {

            mostrarAlerta(
                'Você precisa aceitar os termos para criar sua conta.',
                'error'
            );

            valido = false;
        }


        return valido;
    }


    function emailValido(valor) {

        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/
            .test(valor);
    }


    /*
     * ============================================================
     * UTILIDADES
     * ============================================================
     */

    function marcarInvalido(input) {

        input.classList.add(
            'cadastro-input-invalid'
        );
    }


    function limparErros() {

        nomeError.textContent = '';
        emailError.textContent = '';
        confirmarError.textContent = '';


        [
            nome,
            email,
            senha,
            confirmarSenha
        ].forEach(input => {

            input.classList.remove(
                'cadastro-input-invalid'
            );
        });


        alertBox.classList.add(
            'cadastro-hidden'
        );
    }


    function mostrarAlerta(
        mensagem,
        tipo
    ) {

        alertBox.textContent =
            mensagem;


        alertBox.className =
            `cadastro-alert cadastro-alert-${tipo}`;


        alertBox.classList.remove(
            'cadastro-hidden'
        );


        alertBox.scrollIntoView({
            behavior: 'smooth',
            block: 'nearest'
        });
    }


    function setLoading(loading) {

        submitButton.disabled =
            loading;


        submitText.textContent =
            loading
                ? 'Criando conta...'
                : 'Criar minha conta';


        submitLoading.classList.toggle(
            'cadastro-hidden',
            !loading
        );
    }

});