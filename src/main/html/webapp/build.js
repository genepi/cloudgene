var stealTools = require("steal-tools");

stealTools.build({
  main: ["cloudgene/index", "cloudgene/admin"],
}, {
  bundleAssets: true,
  bundleSteal: true
}).then(function(buildResult) {

});
