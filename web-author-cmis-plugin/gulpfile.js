var gulp = require('gulp');

var fs = require('fs');
var concat = require('gulp-concat');
var Synci18n = require('sync-i18n');
var iife = require("gulp-iife");
var uglify = require('gulp-uglify');
var uglifyOptions = {
  mangle: {
    properties: {
      regex: /_$/
    }
  }
};
var i18nOptions = {
  // use this flag only if sure that the plugin will be surrounded by IIFE.
  useLocalMsgs:true
};


gulp.task('i18n', function () {
  Synci18n(i18nOptions).generateTranslations();
  return Promise.resolve();
});

gulp.task('prepare-package', gulp.series('i18n', function() {
  return gulp.src(['web/*.js'])
    .pipe(concat('plugin.js'))
    .pipe(iife({useStrict: false, prependSemicolon: true}))
    .pipe(uglify(uglifyOptions))
    .pipe(gulp.dest('target/'));
}));

gulp.task('default', gulp.series('i18n'));
