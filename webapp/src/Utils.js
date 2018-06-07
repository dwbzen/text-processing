
export function makeApiRequest(endpoint, options, onSuccess, onFailure) {
    if (!options.headers) {
       options.headers = {};
    }
    console.log('making api request to: ' + endpoint);
    options.headers['Accept'] = 'application/json';
    options.headers['Content-Type'] = 'application/json';
    let response = appFetch(endpoint, options, onSuccess, onFailure);
    return response;
}
