import dateFormat from 'dateformat';
import stache from 'can-stache';
import AU from 'ansi_up';


stache.registerHelper('truncate', function(str, len) {
  if (str.length > len) {
    var new_str = str.substr(0, len + 1);

    while (new_str.length) {
      var ch = new_str.substr(-1);
      new_str = new_str.substr(0, -1);

      if (ch == ' ') {
        break;
      }
    }

    if (new_str == '') {
      new_str = str.substr(0, len);
    }

    return new_str + '...';
  }
  return str;
});

String.prototype.replaceAll = function(search, replacement) {
  var target = this;
  return target.replace(new RegExp(search, 'g'), replacement);
};

function renderTreeItem(jobId, items, level) {
  var html = '<ul class="folder ' + (level > 0 ? 'sub-folder' : 'root-folder') + '">';
  for (var i = 0; i < items.length; i++) {
    html += '<li>';
    if (items[i].folder == true) {
      html += '<i class="fas fa-angle-right folder-item text-muted fa-fw"></i>&nbsp;';
      html += '<span class="folder-item-text fa-fw"><i class="fas fa-folder text-muted"></i>&nbsp' + items[i].name + '</span>';
      html += renderTreeItem(jobId, items[i].childs, level + 1);
    } else {
      html += '<i class="far fa-file-alt text-muted fa-fw file-item-icon""></i>&nbsp;'
      html += '<a class="file-item" href="downloads/' + jobId + '/' + items[i].hash + '/' + items[i].name + '" target="_blank">' + items[i].name + '</a>';
      html += '&nbsp;&nbsp;&nbsp;&nbsp;<span class="text-muted">(' + items[i].size + ")</span>";
    }
    html += "</li>";
  }
  html += "</ul>";
  return html;
}

stache.registerHelper('renderTree', function(jobId, item) {
  return renderTreeItem(jobId, item, 0);
});

stache.registerHelper('replaceNL', function(value, total) {
  return value.replaceAll('\n', '<br>');
});


stache.registerHelper('percentage', function(value, total) {
  return (value / total) * 100;
});


stache.registerHelper('prettyTime', function(executionTime) {
  if (!executionTime || executionTime <= 0) {

    return '-';

  } else {

    var h = (Math.floor((executionTime / 1000) / 60 / 60));
    var m = ((Math.floor((executionTime / 1000) / 60)) % 60);

    return (h > 0 ? h + ' h ' : '') + (m > 0 ? m + ' min ' : '') +
      ((Math.floor(executionTime / 1000)) % 60) + ' sec';

  }

});

String.prototype.endsWith = function(s) {
  return this.length >= s.length && this.substr(this.length - s.length) == s;
};

stache.registerHelper('prettyDate', function(unixTimestamp) {
  if (unixTimestamp > 0) {
    var dt = new Date(unixTimestamp);
    return dateFormat(dt, "default");
  } else {
    return '-';
  }
});

stache.registerHelper('ansiToHtml', function(txt) {
  var ansi_up = new AU();
  ansi_up.use_classes = true;
  return ansi_up.ansi_to_html(txt);
});

stache.registerHelper('isImage', function(str, options) {
  var image = str.endsWith('png') || str.endsWith('jpg') || str.endsWith('gif');
  if (image) {
    return options.fn();
  } else {
    return options.inverse();
  }
});

stache.registerHelper('isS3', function(str, options) {
  var s3 = str.startsWith('s3://')
  if (s3) {
    return options.fn();
  } else {
    return options.inverse();
  }
});

stache.registerHelper('isParamChecked', function(param, options) {
  var value = param.attr('value');
  var result = options.inverse();
  param.attr('values').each(function(item) {
    if (item.attr('key') === 'true') {
      if (item.attr('value') === value) {
        result = options.fn();
        return;
      } else {
        result = options.inverse();
        return;
      }
    }
  });
  return result;
});

stache.registerHelper('getParamTrueValue', function(param, options) {
  var result = '??';
  param.attr('values').each(function(item) {
    if (item.attr('key') === 'true') {
      result = item.attr('value');
      return;
    }
  });
  return result;
});

stache.registerHelper('getParamFalseValue', function(param, options) {
  var result = '??';
  param.attr('values').each(function(item) {
    if (item.attr('key') === 'false') {
      result = item.attr('value');
      return;
    }
  });
  return result;
});


stache.registerHelper('div', function(a, b, options) {
  if (a) {
    return Math.round(a / b * 10) / 10;
  } else {
    return 0;
  }
});
