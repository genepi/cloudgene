import bootbox from 'bootbox';

export default function(title, response) {

  var error;
  if (response.responseJSON) {
    error = {
      statusText: response.status,
      responseText: response.responseJSON.message
    };
  } else {
    error = {
      statusText: response.status,
      responseText: response.responseText
    };

  }

  bootbox.alert('<h5 class="text-danger">Error: ' + title + '</h5><p>' + error.responseText + '</p>');

}
