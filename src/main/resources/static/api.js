(function () {

    const API_BASE_URL = '/api';

    async function extrairMensagemErro(response) {

        try {

            const contentType =
                response.headers.get(
                    'content-type'
                ) || '';

            if (
                contentType.includes(
                    'application/json'
                )
            ) {

                const data =
                    await response.json();

                return (
                    data.mensagem ||
                    data.message ||
                    data.erro ||
                    data.error
                );
            }

            const texto =
                await response.text();

            return texto || null;

        } catch (erro) {

            return null;
        }
    }

    async function request(
        endpoint,
        options = {},
        responseType = 'json'
    ) {

        const token =
            window.auth?.getToken?.()
            || localStorage.getItem('token');

        const headers =
            new Headers(
                options.headers || {}
            );

        const isFormData =
            options.body instanceof FormData;

        if (
            options.body &&
            !isFormData &&
            !headers.has('Content-Type')
        ) {

            headers.set(
                'Content-Type',
                'application/json'
            );
        }

        if (
            token &&
            token !== 'null' &&
            token !== 'undefined'
        ) {

            headers.set(
                'Authorization',
                `Bearer ${token}`
            );
        }

        let response;

        try {

            response = await fetch(
                `${API_BASE_URL}${endpoint}`,
                {
                    ...options,
                    headers
                }
            );

        } catch (erro) {

            throw new Error(
                'Não foi possível conectar ao servidor.'
            );
        }

        if (response.status === 401) {

            window.auth?.limparSessao?.();

            const paginaAtual =
                window.location.pathname;

            if (
                !paginaAtual.endsWith(
                    '/index.html'
                ) &&
                paginaAtual !== '/'
            ) {

                window.location.replace(
                    'index.html'
                );
            }

            throw new Error(
                'Sessão expirada. Faça login novamente.'
            );
        }

        if (!response.ok) {

            const mensagem =
                await extrairMensagemErro(
                    response
                );

            if (response.status === 403) {

                throw new Error(
                    mensagem ||
                    'Você não possui permissão para realizar esta ação.'
                );
            }

            throw new Error(
                mensagem ||
                `Erro na requisição (${response.status}).`
            );
        }

        if (response.status === 204) {
            return null;
        }

        if (responseType === 'blob') {
            return response.blob();
        }

        if (responseType === 'text') {
            return response.text();
        }

        const texto =
            await response.text();

        if (!texto) {
            return null;
        }

        try {

            return JSON.parse(texto);

        } catch (erro) {

            return texto;
        }
    }

    window.api = {

        request,

        get(endpoint) {

            return request(
                endpoint,
                {
                    method: 'GET'
                }
            );
        },

        post(endpoint, body) {

            return request(
                endpoint,
                {
                    method: 'POST',
                    body: JSON.stringify(body)
                }
            );
        },

        put(endpoint, body) {

            return request(
                endpoint,
                {
                    method: 'PUT',
                    body: JSON.stringify(body)
                }
            );
        },

        patch(endpoint, body) {

            return request(
                endpoint,
                {
                    method: 'PATCH',
                    body: JSON.stringify(body)
                }
            );
        },

        delete(endpoint) {

            return request(
                endpoint,
                {
                    method: 'DELETE'
                }
            );
        },

        upload(endpoint, formData) {

            return request(
                endpoint,
                {
                    method: 'POST',
                    body: formData
                }
            );
        },

        getBlob(endpoint) {

            return request(
                endpoint,
                {
                    method: 'GET'
                },
                'blob'
            );
        }
    };

})();