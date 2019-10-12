import $ from 'jquery';
import Control from 'can-control';

import './results.css';


export default Control.extend({

  "init": function(element, options) {
    $('#btn-copy').tooltip();
  },

  '#btn-copy click': function(el) {
    var copyTest = document.queryCommandSupported('copy');
    var elOriginalText = $(el).attr('data-original-title');
    var copyTextArea = $('#curl');

    if (copyTest === true) {
      copyTextArea.select();
      try {
        var successful = document.execCommand('copy');
        var msg = successful ? 'Copied!' : 'Whoops, not copied!';
        $(el).attr('data-original-title', msg).tooltip('show');
      } catch (err) {
        console.log('Oops, unable to copy');
      }
      $(el).attr('data-original-title', elOriginalText);
    } else {
      // Fallback if browser doesn't support .execCommand('copy')
      window.prompt("Copy to clipboard: Ctrl+C or Command+C, Enter", copyTextArea.val());
    }
  }


});
