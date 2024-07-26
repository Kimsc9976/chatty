export { getAjax, postAjax, putAjax, deleteAjax, getParameterByName };

// Global util function


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


function getParameterByName(name) {
    var url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)");
    var results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
