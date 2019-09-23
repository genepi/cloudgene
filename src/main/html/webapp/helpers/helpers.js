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

function renderTreeItem(items, level) {
  var html = '<ul style="list-style-type:none;' + (level > 0 ? 'display:none;' : '') + '">';
  for (var i = 0; i < items.length; i++) {
    html += '<li>';
    if (items[i].folder == true) {
      html += '<i class="fas fa-folder folder-item text-muted" style="cursor:pointer"></i>&nbsp;&nbsp;';
      html += '<span class="folder-item-text" style="cursor:pointer">' + items[i].name + '</span>';
      html += renderTreeItem(items[i].childs, level + 1);
    } else {
      html += '<i class="fas fa-file text-muted"></i>&nbsp;&nbsp;'
      if (items[i].name.startsWith('s3://')) {
        html += '<a href="downloads/' + items[i].hash + '/' + items[i].name + '" target="_blank">' + items[i].name + '</a>';
      } else {
        html += '<a href="results/' + items[i].path + '" target="_blank">' + items[i].name + '</a>';
      }
      html += '&nbsp;&nbsp;&nbsp;&nbsp;<span class="text-muted">(' + items[i].size + ")</span>";
    }
    html += "</li>";
  }
  html += "</ul>";
  return html;
}

stache.registerHelper('renderTree', function(item) {
  return renderTreeItem(item, 0);
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
