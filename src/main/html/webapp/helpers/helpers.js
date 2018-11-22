import dateFormat from 'dateformat';
import stache from 'can-stache';


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

stache.registerHelper('isImage', function(str, options) {
  var image = str.endsWith('png') || str.endsWith('jpg') || str.endsWith('gif');
  if (image) {
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
