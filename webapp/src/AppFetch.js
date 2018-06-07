import fetch from 'isomorphic-fetch';

export const appFetch = (endpoint, options = null, onSuccess = null, onFailure = null) => {
    return fetch(endpoint, options)
        .then((response) => {
            console.log('response ok: ' + response.ok);
            if (!response.ok){
                throw response;
            }
            if (response.status === 204) {
                return response;
            } else {
                return response.json();
            }
        })
        .then((responseJson) => {
        if (onSuccess) {
            onSuccess(responseJson);
        }
        return responseJson;
        } )
    .catch((error) => {
        console.log('failed to fetch: ' + endpoint);
        console.error(error);
        let status = error.status;
        if (!error.status && error.constructor === TypeError && !error.json && error.message === 'Failed to fetch') {
            console.log('assuming origin error');
            if (onFailure) {
                onFailure(null, 0);
            }
        }
        else if (error.json) {
            error.json()
                .then((json) => {
                    if (onFailure) {
                        onFailure(json, status);
                    }
            });
        }
        else {
            console.log('not sure what to do with this error');
            if (onFailure) {
                onFailure(error, status);
            }
        }
    });
};