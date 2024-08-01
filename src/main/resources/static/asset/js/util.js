// Export functions
export { getAjax, postAjax, putAjax, deleteAjax, getParameterByName, getAjaxPromise, postAjaxPromise, putAjaxPromise, deleteAjaxPromise };

// Global util function (Callback-based)
function getAjax(url, success, error) {
    $.ajax({
        url: url,
        type: 'GET',
        success: success,
        error: error
    });
}

function postAjax(url, data, success, error) {
    $.ajax({
        url: url,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: success,
        error: error
    });
}

function putAjax(url, data, success, error) {
    $.ajax({
        url: url,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: success,
        error: error
    });
}

function deleteAjax(url, success, error) {
    $.ajax({
        url: url,
        type: 'DELETE',
        success: success,
        error: error
    });
}

// New Promise-based util functions
function getAjaxPromise(url) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: url,
            type: 'GET',
            success: function(response) {
                resolve(response);
            },
            error: function(error) {
                reject(error);
            }
        });
    });
}

function postAjaxPromise(url, data) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: url,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(response) {
                resolve(response);
            },
            error: function(error) {
                reject(error);
            }
        });
    });
}

function putAjaxPromise(url, data) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: url,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(response) {
                resolve(response);
            },
            error: function(error) {
                reject(error);
            }
        });
    });
}

function deleteAjaxPromise(url) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: url,
            type: 'DELETE',
            success: function(response) {
                resolve(response);
            },
            error: function(error) {
                reject(error);
            }
        });
    });
}

function getParameterByName(name) {
    var url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)");
    var results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
