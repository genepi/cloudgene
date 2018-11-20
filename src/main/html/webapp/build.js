var stealTools = require("steal-tools");

stealTools.build({
  main: ["Cloudgene/index", "Cloudgene/admin"],
}, {
  bundleAssets: true,
  bundleSteal: true
}).then(function(buildResult) {

});
