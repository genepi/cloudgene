module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    cancompile: {
      options: {
        version: '2.2.6'
      },
      dist: {
        src: ['views/*.ejs', 'views/admin/*.ejs', 'views/components/*.ejs'],
        dest: 'tmp/cloudgene.views.js'
      }
    },

    concat: {
      options: {
        separator: ';',
      },
      app: {
        src: ['models/*.js', 'controllers/*.js', 'tmp/cloudgene.views.js'],
        dest: 'tmp/cloudgene.js',
      },
      vendor: {
        src: [
          'assets/js/lib/date.format.js',
          'assets/js/lib/jquery-3.3.1.js',
          'assets/js/lib/popper.js',
          'assets/js/lib/jquery.form.js',
          'assets/js/lib/bootstrap.js',
          'assets/js/lib/bootbox.min.js',
          'assets/js/lib/can.jquery.js',
          'assets/js/lib/md5.min.js',
          'assets/js/lib/raphael.min.js',
          'assets/js/lib/morris.min.js'
        ],
        dest: 'tmp/cloudgene.vendor.js',
      },
    },

    uglify: {
      options: {
        banner: '/*! <%= pkg.name %> <%= pkg.version %> (<%= grunt.template.today("yyyy-mm-dd") %>) */\n'
      },
      app: {
        src: 'tmp/cloudgene.js',
        dest: 'dist/cloudgene.min.js'
      },
      vendor: {
        src: 'tmp/cloudgene.vendor.js',
        dest: 'dist/cloudgene.vendor.min.js'
      }
    },

    cssmin: {
      dist: {
        options: {
          banner: '/*! <%= pkg.name %> <%= pkg.version %> (<%= grunt.template.today("yyyy-mm-dd") %>) */\n'
        },
        files: {
          'dist/cloudgene.vendor.min.css': [
            'assets/css/bootstrap.css',
            'assets/css/morris.css',
            'assets/css/cloudgene.css'
          ]
        }
      }
    },

    targethtml: {
      dist: {
        files: {
          'dist/index.html': 'index.html',
          'dist/start.html': 'start.html',
          'dist/admin.html': 'admin.html'
        }
      }
    },

    copy: {
      main: {
        files: [{
            expand: true,
            src: ['assets/images/**'],
            dest: 'dist'
          }, {
            src: ['assets/js/lib/fontawesome-5.0.6.js'],
            dest: 'dist/assets/fontawesome-5.0.6.js'
          },
          {
            expand: true,
            src: ['assets/img/**'],
            dest: 'dist'
          }, {
            expand: true,
            cwd: 'assets/img',
            src: ['glyph*'],
            dest: 'dist/img'
          }
        ]
      }
    }
  });

  // Load the plugin that provides the "uglify" task.
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('can-compile');
  grunt.loadNpmTasks('grunt-targethtml');

  // Default task(s).
  grunt.registerTask('default', ['cancompile', 'concat', 'uglify', 'cssmin', 'targethtml', 'copy']);

};
